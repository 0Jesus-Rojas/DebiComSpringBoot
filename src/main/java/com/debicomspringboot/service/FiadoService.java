package com.debicomspringboot.service;

import com.debicomspringboot.dto.ItemCreditoDTO;
import com.debicomspringboot.dto.ProductoDTO;
import com.debicomspringboot.dto.SolicitudCreditoDTO;
import com.debicomspringboot.repository.FiadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FiadoService {

    private final FiadoRepository fiadoRepository;

    public List<ProductoDTO> listarProductos(int idTienda, int idVendedor) {
        if (idTienda <= 0 || idVendedor <= 0) {
            throw new IllegalArgumentException("Tienda o vendedor no válidos.");
        }

        List<Object[]> filas = fiadoRepository.listarProductosActivos(idTienda, idVendedor);
        List<ProductoDTO> result = new ArrayList<>();

        for (Object[] row : filas) {
            ProductoDTO dto = new ProductoDTO();
            dto.setIdProducto(((Number) row[0]).intValue());
            dto.setNombre((String) row[1]);
            dto.setDescripcion((String) row[2]);
            dto.setPrecioUnitario(row[3] == null ? BigDecimal.ZERO : new BigDecimal(row[3].toString()));
            dto.setStock(((Number) row[4]).intValue());
            dto.setIdTienda(((Number) row[5]).intValue());
            dto.setUnidad((String) row[6]);
            dto.setEstado((String) row[7]);
            result.add(dto);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public int asignarFiado(int idVendedor, int idTienda, int idCliente,
                            LocalDate fechaVencimiento, String observaciones,
                            List<ItemCreditoDTO> items) {
        if (idVendedor <= 0 || idTienda <= 0 || idCliente <= 0) {
            throw new IllegalArgumentException("Vendedor, tienda y comprador son obligatorios.");
        }
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria.");
        }
        LocalDate hoy = LocalDate.now();
        if (fechaVencimiento.isBefore(hoy) || fechaVencimiento.isAfter(hoy.plusDays(7))) {
            throw new IllegalArgumentException("El plazo del fiado debe ser de máximo 7 días.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un producto para el fiado.");
        }

        LinkedHashMap<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (ItemCreditoDTO item : items) {
            if (item == null || item.getIdProducto() <= 0 || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada producto debe tener una cantidad mayor que cero.");
            }
            cantidades.merge(item.getIdProducto(), item.getCantidad(), Math::addExact);
        }

        if (fiadoRepository.contarTiendaDeVendedor(idTienda, idVendedor) == 0) {
            throw new SecurityException("La tienda no pertenece al vendedor autenticado.");
        }
        if (fiadoRepository.contarCliente(idCliente) == 0) {
            throw new IllegalArgumentException("El comprador no existe.");
        }

        Integer idEstado = fiadoRepository.obtenerIdEstadoAprobado();
        if (idEstado == null) {
            throw new IllegalStateException("No existe el estado APROBADO.");
        }
        Integer idTipoFiado = fiadoRepository.obtenerIdTipoFiado();
        if (idTipoFiado == null) {
            throw new IllegalStateException("No existe el tipo FIADO.");
        }
        Integer idSalida = fiadoRepository.obtenerIdTipoMovimientoSalida();
        if (idSalida == null) {
            throw new IllegalStateException("No existe el tipo de movimiento SALIDA.");
        }

        record ItemValido(int id, int cantidad, int stock, BigDecimal precio, BigDecimal subtotal) {}

        List<ItemValido> validos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (var entry : cantidades.entrySet()) {
            List<Object[]> prodRows = fiadoRepository.bloquearProductoActivo(entry.getKey(), idTienda);
            if (prodRows.isEmpty()) {
                throw new IllegalArgumentException("El producto #" + entry.getKey() + " no está disponible en la tienda.");
            }
            Object[] prod = prodRows.get(0);
            int cantidad = entry.getValue();
            int stock = ((Number) prod[3]).intValue();
            if (cantidad > stock) {
                throw new IllegalArgumentException("Stock insuficiente para el producto #" + entry.getKey() + ".");
            }

            BigDecimal precio = new BigDecimal(prod[2].toString()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
            validos.add(new ItemValido(entry.getKey(), cantidad, stock, precio, subtotal));
            total = total.add(subtotal).setScale(2, RoundingMode.HALF_UP);
        }

        if (total.signum() <= 0) {
            throw new IllegalArgumentException("El total del fiado debe ser mayor que cero.");
        }

        String obsLimpia = (observaciones == null || observaciones.isBlank()) ? null : observaciones.trim();
        fiadoRepository.insertarSolicitudFiado(
                idCliente, idTienda, total, idTipoFiado, idEstado, Date.valueOf(fechaVencimiento), obsLimpia);

        int idSolicitud = fiadoRepository.obtenerUltimoIdGenerado();
        if (idSolicitud <= 0) {
            throw new IllegalStateException("No se generó el ID del fiado.");
        }

        for (ItemValido item : validos) {
            if (fiadoRepository.insertarDetalleFiado(idSolicitud, item.id(), item.cantidad(), item.precio(), item.subtotal()) != 1) {
                throw new IllegalStateException("No se pudo guardar el detalle del fiado.");
            }
            if (fiadoRepository.actualizarStockProducto(item.stock() - item.cantidad(), item.id()) != 1) {
                throw new IllegalStateException("No se pudo actualizar inventario.");
            }
            fiadoRepository.insertarMovimientoSalida(item.id(), idSalida, item.cantidad(), "Fiado #" + idSolicitud);
        }

        fiadoRepository.recalcularCreditoCliente(idCliente);
        return idSolicitud;
    }

    public List<SolicitudCreditoDTO> listarFiadosPendientes(int idVendedor) {
        List<Object[]> filas = fiadoRepository.listarFiadosPendientesCrudo(idVendedor);
        List<SolicitudCreditoDTO> result = new ArrayList<>();

        for (Object[] row : filas) {
            SolicitudCreditoDTO dto = new SolicitudCreditoDTO();
            dto.setIdSolicitud(((Number) row[0]).intValue());
            dto.setIdCliente(((Number) row[1]).intValue());
            dto.setIdTienda(((Number) row[2]).intValue());
            dto.setTienda((String) row[3]);
            dto.setCliente((String) row[4]);
            dto.setMontoTotal(row[5] == null ? BigDecimal.ZERO : new BigDecimal(row[5].toString()));
            dto.setSaldoPendiente(row[6] == null ? BigDecimal.ZERO : new BigDecimal(row[6].toString()));
            dto.setCupoAprobado(row[7] == null ? BigDecimal.ZERO : new BigDecimal(row[7].toString()));
            dto.setIdTipoPrestamo(((Number) row[8]).intValue());
            dto.setTipoPrestamo((String) row[9]);
            dto.setEstado((String) row[10]);
            dto.setFechaSolicitud(toLocalDateTime(row[11]));
            dto.setFechaAprobacion(toLocalDateTime(row[12]));
            LocalDate fechaVenc = toLocalDate(row[13]);
            dto.setFechaVencimiento(fechaVenc != null ? fechaVenc.atStartOfDay() : null);
            dto.setObservaciones((String) row[14]);
            result.add(dto);
        }
        return result;
    }

    private LocalDateTime toLocalDateTime(Object val) {
        if (val == null) return null;
        if (val instanceof Timestamp ts) return ts.toLocalDateTime();
        if (val instanceof LocalDateTime ldt) return ldt;
        return Timestamp.valueOf(val.toString()).toLocalDateTime();
    }

    private LocalDate toLocalDate(Object val) {
        if (val == null) return null;
        if (val instanceof Date d) return d.toLocalDate();
        if (val instanceof LocalDate ld) return ld;
        return Date.valueOf(val.toString()).toLocalDate();
    }
}