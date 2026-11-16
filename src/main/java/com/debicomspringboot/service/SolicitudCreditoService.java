package com.debicomspringboot.service;

import com.debicomspringboot.dto.ProductoDTO;
import com.debicomspringboot.dto.SolicitudCreditoDTO;
import com.debicomspringboot.entity.*;
import com.debicomspringboot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SolicitudCreditoService {

    private final SolicitudesCreditoRepository solicitudCreditoRepository;
    private final DetalleSolicitudCreditoRepository detalleSolicitudCreditoRepository;
    private final ClientesRepository clienteRepository;
    private final UsuariosRepository usuarioRepository;
    private final TiendasRepository tiendaRepository;
    private final EstadosSolicitudRepository estadoSolicitudRepository;
    private final TiposPrestamoRepository tipoPrestamoRepository;
    private final ProductosRepository productoRepository;
    private final UnidadesRepository unidadRepository;
    private final EstadosProductoRepository estadoProductoRepository;

    // 1. Reemplaza a registrarSolicitud(...)
    @Transactional
    public Integer registrarSolicitud(Integer idCliente, Integer idTienda, BigDecimal cupo,
                                      LocalDate fechaVencimiento, String observaciones) {

        if (cupo == null || cupo.signum() <= 0) {
            throw new IllegalArgumentException("El cupo solicitado debe ser mayor que cero.");
        }
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria.");
        }
        if (!clienteRepository.existsById(idCliente)) {
            throw new IllegalArgumentException("El cliente no existe.");
        }
        if (!tiendaRepository.existsById(idTienda)) {
            throw new IllegalArgumentException("La tienda no existe.");
        }

        EstadosSolicitud estadoPendiente = estadoSolicitudRepository.findByNombreEstadoIgnoreCase("PENDIENTE")
                .orElseThrow(() -> new IllegalStateException("No existe el estado PENDIENTE en estados_solicitud."));

        TiposPrestamo tipoCredito = tipoPrestamoRepository.findByNombreTipoIgnoreCase("CREDITO")
                .orElseThrow(() -> new IllegalStateException("No existe el tipo de préstamo CREDITO."));

        SolicitudesCredito nueva = new SolicitudesCredito();
        nueva.setIdCliente(idCliente);
        nueva.setIdTienda(idTienda);
        nueva.setMontoTotal(BigDecimal.ZERO);
        nueva.setSaldoPendiente(BigDecimal.ZERO);
        nueva.setCupoAprobado(cupo);
        nueva.setIdTipoPrestamo(tipoCredito.getIdTipoPrestamo());
        nueva.setIdEstadoSolicitud(estadoPendiente.getIdEstadoSolicitud());
        nueva.setFechaSolicitud(LocalDateTime.now());
        nueva.setFechaAprobacion(null);
        nueva.setFechaVencimiento(fechaVencimiento);
        nueva.setObservaciones(observaciones == null || observaciones.isBlank() ? null : observaciones.trim());

        SolicitudesCredito guardada = solicitudCreditoRepository.save(nueva);
        return guardada.getIdSolicitud();
    }

    // 2. Reemplaza a listarPorCliente(int idCliente)
    @Transactional(readOnly = true)
    public List<SolicitudCreditoDTO> listarPorCliente(Integer idCliente) {
        return solicitudCreditoRepository.listarPorCliente(idCliente)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // 3. Reemplaza a consultarPorIdYCliente(int idSolicitud, int idCliente)
    @Transactional(readOnly = true)
    public Optional<SolicitudCreditoDTO> consultarPorIdYCliente(Integer idSolicitud, Integer idCliente) {
        return solicitudCreditoRepository.consultarPorIdYCliente(idSolicitud, idCliente)
                .map(this::mapToDTO);
    }

    // 4. Reemplaza a listarProductosSolicitud(int idSolicitud)[cite: 16]
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductosSolicitud(Integer idSolicitud) {
        return detalleSolicitudCreditoRepository.findByIdSolicitud(idSolicitud)
                .stream()
                .map(dsc -> {
                    ProductoDTO dto = new ProductoDTO();
                    dto.setIdProducto(dsc.getIdProducto());
                    dto.setPrecioUnitario(dsc.getPrecioUnitario());
                    dto.setCantidad(dsc.getCantidad());
                    dto.setSubtotal(dsc.getSubtotal());

                    productoRepository.findById(dsc.getIdProducto()).ifPresent(p -> {
                        dto.setNombre(p.getNombre());
                        dto.setDescripcion(p.getDescripcion());
                        dto.setStock(p.getStock());
                        dto.setIdTienda(p.getIdTienda());

                        if (p.getIdUnidad() != null) {
                            unidadRepository.findById(p.getIdUnidad())
                                    .ifPresent(u -> dto.setUnidad(u.getNombreUnidad()));
                        }
                        if (p.getIdEstadoProducto() != null) {
                            estadoProductoRepository.findById(p.getIdEstadoProducto())
                                    .ifPresent(ep -> dto.setEstado(ep.getNombreEstado()));
                        }
                    });
                    return dto;
                })
                .sorted(Comparator.comparing(ProductoDTO::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    // 5. Métodos equivalentes al CRUD básico legacy[cite: 16]
    @Transactional(readOnly = true)
    public Optional<SolicitudesCredito> consultarSolicitudCredito(Integer idSolicitud) {
        return solicitudCreditoRepository.findById(idSolicitud);
    }

    @Transactional
    public SolicitudesCredito guardarOActualizarSolicitud(SolicitudesCredito solicitud) {
        if (solicitud.getCupoAprobado() == null || solicitud.getCupoAprobado().signum() <= 0) {
            solicitud.setCupoAprobado(solicitud.getMontoTotal());
        }
        return solicitudCreditoRepository.save(solicitud);
    }

    @Transactional
    public boolean eliminarSolicitudCredito(Integer idSolicitud) {
        if (solicitudCreditoRepository.existsById(idSolicitud)) {
            solicitudCreditoRepository.deleteById(idSolicitud);
            return true;
        }
        return false;
    }

    // Reemplaza los INNER JOIN y mapDTO(ResultSet rs) del DAO original[cite: 16]
    private SolicitudCreditoDTO mapToDTO(SolicitudesCredito sc) {
        SolicitudCreditoDTO dto = new SolicitudCreditoDTO();
        dto.setIdSolicitud(sc.getIdSolicitud());
        dto.setIdCliente(sc.getIdCliente());
        dto.setIdTienda(sc.getIdTienda());
        dto.setMontoTotal(sc.getMontoTotal());
        dto.setSaldoPendiente(sc.getSaldoPendiente());
        dto.setCupoAprobado(sc.getCupoAprobado());
        dto.setIdTipoPrestamo(sc.getIdTipoPrestamo());
        dto.setFechaSolicitud(sc.getFechaSolicitud());
        dto.setFechaAprobacion(sc.getFechaAprobacion());
        dto.setFechaVencimiento(sc.getFechaVencimiento() != null? sc.getFechaVencimiento().atStartOfDay(): null);
        dto.setObservaciones(sc.getObservaciones());

        tiendaRepository.findById(sc.getIdTienda())
                .ifPresent(t -> dto.setTienda(t.getNombreTienda()));

        clienteRepository.findById(sc.getIdCliente())
                .flatMap(c -> usuarioRepository.findById(c.getIdUsuario()))
                .ifPresent(u -> dto.setCliente(u.getNombre() + " " + u.getApellido()));

        tipoPrestamoRepository.findById(sc.getIdTipoPrestamo())
                .ifPresent(tp -> dto.setTipoPrestamo(tp.getNombreTipo()));

        estadoSolicitudRepository.findById(sc.getIdEstadoSolicitud())
                .ifPresent(es -> dto.setEstado(es.getNombreEstado()));

        return dto;
    }
}