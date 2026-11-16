package com.debicomspringboot.service;

import com.debicomspringboot.dto.FacturaPDFDTO;
import com.debicomspringboot.dto.ItemVentaDirectaDTO;
import com.debicomspringboot.dto.ProductoDTO;
import com.debicomspringboot.entity.*;
import com.debicomspringboot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VentaDirectaService {

    private static final DateTimeFormatter FACTURA_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ProductosRepository productosRepository;
    private final TipoPagoRepository tipoPagoRepository;
    private final TiendasRepository tiendaRepository;
    private final ClientesRepository clientesRepository;
    private final UsuariosRepository usuariosRepository;
    private final FacturasRepository facturasRepository;
    private final DetalleFacturasRepository detalleFacturasRepository;
    private final EstadosFacturaRepository estadosFacturaRepository;
    private final PagosRepository pagosRepository;
    private final TiposMovimientoRepository tiposMovimientoRepository;
    private final MovimientosInventarioRepository movimientosInventarioRepository;
    private final UnidadesRepository unidadesRepository;
    private final EstadosProductoRepository estadosProductoRepository;

    // 1. Reemplaza a listarProductosParaVenta(int idTienda, int idVendedor)[cite: 24]
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductosParaVenta(Integer idTienda, Integer idVendedor) {
        if (idTienda == null || idTienda <= 0 || idVendedor == null || idVendedor <= 0) {
            throw new IllegalArgumentException("Tienda o vendedor no válidos.");
        }

        return productosRepository.listarActivosDisponibles(idTienda, idVendedor)
                .stream()
                .map(p -> {
                    ProductoDTO dto = new ProductoDTO();
                    dto.setIdProducto(p.getIdProducto());
                    dto.setNombre(p.getNombre());
                    dto.setDescripcion(p.getDescripcion());
                    dto.setPrecioUnitario(p.getPrecioUnitario());
                    dto.setStock(p.getStock());
                    dto.setIdTienda(p.getIdTienda());

                    if (p.getIdUnidad() != null) {
                        unidadesRepository.findById(p.getIdUnidad())
                                .ifPresent(u -> dto.setUnidad(u.getNombreUnidad()));
                    }
                    if (p.getIdEstadoProducto() != null) {
                        estadosProductoRepository.findById(p.getIdEstadoProducto())
                                .ifPresent(ep -> dto.setEstado(ep.getNombreEstado()));
                    }
                    return dto;
                })
                .toList();
    }

    // 2. Reemplaza a listarTiposPago()[cite: 24]
    @Transactional(readOnly = true)
    public List<TipoPago> listarTiposPago() {
        return tipoPagoRepository.listarNoPresenciales();
    }

    // 3. Reemplaza a registrarVentaDirecta(...)[cite: 24]
    @Transactional
    public Integer registrarVentaDirecta(Integer idVendedor, Integer idTienda, Integer idCliente,
                                         Integer idTipoPago, String observaciones,
                                         List<ItemVentaDirectaDTO> items) {

        if (idVendedor == null || idVendedor <= 0 || idTienda == null || idTienda <= 0
                || idCliente == null || idCliente <= 0 || idTipoPago == null || idTipoPago <= 0) {
            throw new IllegalArgumentException("Vendedor, tienda, comprador y método de pago son obligatorios.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un producto.");
        }

        // Agrupa cantidades si el mismo producto viene repetido en la lista[cite: 24]
        LinkedHashMap<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (ItemVentaDirectaDTO item : items) {
            if (item == null || item.getIdProducto() <= 0 || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada producto debe tener una cantidad mayor que cero.");
            }
            cantidades.merge(item.getIdProducto(), item.getCantidad(), Math::addExact);
        }

        if (tiendaRepository.findByIdTiendaAndIdVendedor(idTienda, idVendedor).isEmpty()) {
            throw new SecurityException("La tienda no pertenece al vendedor autenticado.");
        }
        if (!clientesRepository.existsById(idCliente)) {
            throw new IllegalArgumentException("El comprador no existe.");
        }
        if (tipoPagoRepository.buscarNoPresencialPorId(idTipoPago).isEmpty()) {
            throw new IllegalArgumentException("El método de pago seleccionado no es válido.");
        }

        EstadosFactura estadoPagada = estadosFacturaRepository.buscarEstadoPagada()
                .orElseThrow(() -> new IllegalStateException("No existe el estado de factura PAGADA/PAGADO."));

        TiposMovimiento tipoSalida = tiposMovimientoRepository.findByNombreTipoIgnoreCase("SALIDA")
                .orElseThrow(() -> new IllegalStateException("No existe el tipo de movimiento SALIDA."));

        record ItemValido(Productos producto, int cantidad, BigDecimal precio, BigDecimal subtotal) {}
        List<ItemValido> validos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // Bloquea cada producto (FOR UPDATE) y valida stock disponible[cite: 24]
        for (var entry : cantidades.entrySet()) {
            Productos prod = productosRepository.buscarActivoParaActualizar(entry.getKey(), idTienda)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El producto #" + entry.getKey() + " no está disponible en la tienda seleccionada."));

            int cantidad = entry.getValue();
            if (cantidad > prod.getStock()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para el producto #" + entry.getKey() + ". Disponible: " + prod.getStock() + ".");
            }

            BigDecimal precio = prod.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
            validos.add(new ItemValido(prod, cantidad, precio, subtotal));
            total = total.add(subtotal).setScale(2, RoundingMode.HALF_UP);
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total de la venta debe ser mayor que cero.");
        }

        // Genera número único de factura de venta directa (idSolicitud = null)[cite: 24]
        String numeroFactura = "VD-" + LocalDateTime.now().format(FACTURA_FMT) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Facturas factura = new Facturas();
        factura.setNumeroFactura(numeroFactura);
        factura.setIdSolicitud(null);
        factura.setIdCliente(idCliente);
        factura.setIdTienda(idTienda);
        factura.setSubtotal(total);
        factura.setImpuestos(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        factura.setTotal(total);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setIdEstadoFactura(estadoPagada.getIdEstadoFactura());

        Facturas facturaGuardada = facturasRepository.save(factura);
        Integer idFactura = facturaGuardada.getIdFactura();

        // Guarda detalle de factura, descuenta stock y registra salida de inventario[cite: 24]
        for (ItemValido item : validos) {
            DetalleFacturas detalle = new DetalleFacturas();
            detalle.setIdFactura(idFactura);
            detalle.setIdProducto(item.producto().getIdProducto());
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(item.precio());
            detalle.setSubtotal(item.subtotal());
            detalleFacturasRepository.save(detalle);

            item.producto().setStock(item.producto().getStock() - item.cantidad());

            MovimientosInventario movimiento = new MovimientosInventario();
            movimiento.setIdProducto(item.producto().getIdProducto());
            movimiento.setIdTipoMovimiento(tipoSalida.getIdTipoMovimiento());
            movimiento.setCantidad(item.cantidad());
            movimiento.setMotivo("Venta directa " + numeroFactura);
            movimiento.setFechaMovimiento(LocalDateTime.now());
            movimientosInventarioRepository.save(movimiento);
        }

        // Registra el pago de contado asociado a la factura[cite: 24]
        String referencia = "VD-PAGO-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
        Pagos pago = new Pagos();
        pago.setIdFactura(idFactura);
        pago.setNumeroReferenciaPago(referencia);
        pago.setMontoPagado(total);
        pago.setFechaPago(LocalDateTime.now());
        pago.setIdTipoPago(idTipoPago);
        pago.setObservaciones(observaciones == null || observaciones.isBlank() ? null : observaciones.trim());
        pagosRepository.save(pago);

        return idFactura;
    }

    // 4. Reemplaza a obtenerFacturaDirectaParaPDF(int idFactura, int idVendedor)[cite: 24]
    @Transactional(readOnly = true)
    public FacturaPDFDTO obtenerFacturaDirectaParaPDF(Integer idFactura, Integer idVendedor) {
        if (idFactura == null || idFactura <= 0 || idVendedor == null || idVendedor <= 0) {
            return null;
        }

        Facturas factura = facturasRepository.buscarVentaDirectaPorIdYVendedor(idFactura, idVendedor)
                .orElse(null);
        if (factura == null) {
            return null;
        }

        Pagos pago = pagosRepository.findFirstByIdFactura(idFactura).orElse(null);
        if (pago == null) {
            return null;
        }

        FacturaPDFDTO dto = new FacturaPDFDTO();
        dto.setIdFactura(factura.getIdFactura());
        dto.setNumeroFactura(factura.getNumeroFactura());
        dto.setSubtotal(factura.getSubtotal());
        dto.setImpuestos(factura.getImpuestos());
        dto.setTotal(factura.getTotal());
        dto.setFechaEmision(factura.getFechaEmision());
        dto.setReferenciaPago(pago.getNumeroReferenciaPago());

        clientesRepository.findById(factura.getIdCliente())
                .flatMap(c -> usuariosRepository.findById(c.getIdUsuario()))
                .ifPresent(u -> {
                    dto.setComprador(u.getNombre() + " " + u.getApellido());
                    dto.setIdentificacionComprador(u.getIdentificacion());
                });

        tiendaRepository.findById(factura.getIdTienda()).ifPresent(t -> {
            dto.setTienda(t.getNombreTienda());
            dto.setNitTienda(t.getNit());
            dto.setDireccionTienda(t.getDireccion());
        });

        if (pago.getIdTipoPago() != null) {
            tipoPagoRepository.findById(pago.getIdTipoPago())
                    .ifPresent(tp -> dto.setMetodoPago(tp.getNombrePago()));
        }

        detalleFacturasRepository.findByIdFacturaOrderByIdDetalleFacturaAsc(idFactura)
                .forEach(df -> {
                    FacturaPDFDTO.Item item = new FacturaPDFDTO.Item();
                    item.setCantidad(df.getCantidad());
                    item.setPrecioUnitario(df.getPrecioUnitario());
                    item.setSubtotal(df.getSubtotal());
                    productosRepository.findById(df.getIdProducto())
                            .ifPresent(p -> item.setNombre(p.getNombre()));
                    dto.getItems().add(item);
                });

        return dto;
    }
}