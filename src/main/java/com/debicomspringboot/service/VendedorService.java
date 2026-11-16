package com.debicomspringboot.service;

import com.debicomspringboot.dto.*;
import com.debicomspringboot.entity.*;
import com.debicomspringboot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendedorService {

    private static final int STOCK_BAJO_UMBRAL = 5;

    private final TiendasRepository tiendaRepository;
    private final SolicitudesCreditoRepository solicitudCreditoRepository;
    private final DetalleSolicitudCreditoRepository detalleSolicitudCreditoRepository;
    private final FacturasRepository facturaRepository;
    private final PagosRepository pagoRepository;
    private final PagosPresencialesRepository pagosPresencialesRepository;
    private final ProductosRepository productoRepository;
    private final ClientesRepository clienteRepository;
    private final UsuariosRepository usuarioRepository;
    private final EstadosSolicitudRepository estadosSolicitudRepository;
    private final TiposPrestamoRepository tiposPrestamoRepository;
    private final TiposMovimientoRepository tiposMovimientoRepository;
    private final MovimientosInventarioRepository movimientoInventarioRepository;
    private final UnidadesRepository unidadesRepository;
    private final EstadosProductoRepository estadosProductoRepository;

    // 1. listarTiendasDelVendedor
    @Transactional(readOnly = true)
    public List<TiendaDTO> listarTiendasDelVendedor(Integer idVendedor) {
        return tiendaRepository.findByIdVendedorOrderByNombreTiendaAsc(idVendedor)
                .stream()
                .map(this::mapTienda)
                .toList();
    }

    // 2. obtenerDashboard

    @Transactional(readOnly = true)
    public DashboardVendedorDTO obtenerDashboard(Integer idVendedor) {

        DashboardVendedorDTO dto = new DashboardVendedorDTO();
        long inicio;

        log.info("[DASHBOARD] Iniciando carga para vendedor {}", idVendedor);

        inicio = System.nanoTime();
        log.info("[DASHBOARD] Consultando clientes...");

        dto.setTotalClientes(
                solicitudCreditoRepository.contarClientesUnicos(idVendedor)
        );

        log.info("[DASHBOARD] Clientes consultados en {} ms",
                (System.nanoTime() - inicio) / 1_000_000);

        inicio = System.nanoTime();
        log.info("[DASHBOARD] Consultando solicitudes nuevas...");

        long nuevas = solicitudCreditoRepository.contarSolicitudesNuevas(
                idVendedor, LocalDateTime.now().minusDays(7));

        dto.setSolicitudesNuevas(nuevas);
        dto.setSolicitudesPendientes(nuevas);

        log.info("[DASHBOARD] Solicitudes nuevas consultadas en {} ms",
                (System.nanoTime() - inicio) / 1_000_000);

        inicio = System.nanoTime();
        log.info("[DASHBOARD] Consultando solicitudes aprobadas...");

        dto.setSolicitudesAprobadas(
                solicitudCreditoRepository.contarSolicitudesAprobadas(idVendedor)
        );

        log.info("[DASHBOARD] Solicitudes aprobadas consultadas en {} ms",
                (System.nanoTime() - inicio) / 1_000_000);

        inicio = System.nanoTime();
        log.info("[DASHBOARD] Sumando pagos de facturas...");

        BigDecimal pagosDirectos =
                pagoRepository.sumarPagosPorVendedor(idVendedor);

        log.info("[DASHBOARD] Pagos de facturas consultados en {} ms",
                (System.nanoTime() - inicio) / 1_000_000);

        inicio = System.nanoTime();
        log.info("[DASHBOARD] Sumando pagos presenciales...");

        BigDecimal pagosPresenciales =
                pagosPresencialesRepository.sumarPagosPorVendedor(idVendedor);

        log.info("[DASHBOARD] Pagos presenciales consultados en {} ms",
                (System.nanoTime() - inicio) / 1_000_000);

        dto.setPagosRecibidos(pagosDirectos.add(pagosPresenciales));

        inicio = System.nanoTime();
        log.info("[DASHBOARD] Calculando deuda pendiente...");

        dto.setDeudaPendiente(
                solicitudCreditoRepository.sumarDeudaPendiente(idVendedor)
        );

        log.info("[DASHBOARD] Deuda consultada en {} ms",
                (System.nanoTime() - inicio) / 1_000_000);

        log.info("[DASHBOARD] Carga completada para vendedor {}", idVendedor);

        return dto;
    }


    // 3. obtenerMetricasTienda
    @Transactional(readOnly = true)
    public TiendaDTO obtenerMetricasTienda(Integer idTienda, Integer idVendedor) {
        Tiendas t = tiendaRepository.findByIdTiendaAndIdVendedor(idTienda, idVendedor).orElse(null);
        if (t == null) return null;

        TiendaDTO dto = mapTienda(t);
        usuarioRepository.findById(t.getIdVendedor())
                .ifPresent(u -> dto.setVendedor(u.getNombre() + " " + u.getApellido()));

        dto.setVentasTotales(facturaRepository.sumarVentasTotalesPorTienda(idTienda));
        dto.setCreditosOtorgados(solicitudCreditoRepository.contarCreditosOtorgadosPorTienda(idTienda));
        dto.setClientesActivos(solicitudCreditoRepository.contarClientesActivosPorTienda(idTienda));
        dto.setTotalProductos(productoRepository.countByIdTienda(idTienda));
        return dto;
    }

    // 4. listarSolicitudesPendientes
    @Transactional(readOnly = true)
    public List<SolicitudCreditoDTO> listarSolicitudesPendientes(Integer idVendedor) {
        return solicitudCreditoRepository.listarPendientesVendedor(idVendedor)
                .stream()
                .map(this::mapSolicitud)
                .toList();
    }

    // 5. listarHistorialCreditos
    @Transactional(readOnly = true)
    public List<SolicitudCreditoDTO> listarHistorialCreditos(Integer idVendedor) {
        return solicitudCreditoRepository.listarHistorialCreditosVendedor(idVendedor)
                .stream()
                .map(this::mapSolicitud)
                .toList();
    }

    // 6. listarHistorialPagos
    @Transactional(readOnly = true)
    public List<PagoVendedorHistorialDTO> listarHistorialPagos(Integer idVendedor) {
        return pagosPresencialesRepository.listarHistorialPorVendedor(idVendedor)
                .stream()
                .map(pp -> {
                    PagoVendedorHistorialDTO dto = new PagoVendedorHistorialDTO();
                    dto.setIdPago(pp.getIdPagoPresencial());
                    dto.setIdSolicitud(pp.getIdSolicitud());
                    dto.setIdCliente(pp.getIdCliente());
                    dto.setMontoPagado(pp.getMontoPagado());
                    dto.setFechaPago(pp.getFechaPago());
                    dto.setNumeroReferencia(pp.getNumeroReferencia());
                    dto.setObservaciones(pp.getObservaciones());

                    clienteRepository.findById(pp.getIdCliente())
                            .flatMap(c -> usuarioRepository.findById(c.getIdUsuario()))
                            .ifPresent(u -> dto.setCliente(u.getNombre() + " " + u.getApellido()));

                    solicitudCreditoRepository.findById(pp.getIdSolicitud()).ifPresent(sc -> {
                        tiendaRepository.findById(sc.getIdTienda())
                                .ifPresent(t -> dto.setTienda(t.getNombreTienda()));
                        estadosSolicitudRepository.findById(sc.getIdEstadoSolicitud())
                                .ifPresent(es -> dto.setEstadoCredito(es.getNombreEstado()));
                    });
                    return dto;
                })
                .toList();
    }

    // 7. listarSolicitudes (con filtro opcional de estado)
    @Transactional(readOnly = true)
    public List<SolicitudCreditoDTO> listarSolicitudes(Integer idVendedor, String estado) {
        String filtro = (estado == null || estado.isBlank()) ? null : estado.trim();
        return solicitudCreditoRepository.listarPorVendedorYEstado(idVendedor, filtro)
                .stream()
                .map(this::mapSolicitud)
                .toList();
    }

    // 8. obtenerDetalleCredito
    @Transactional(readOnly = true)
    public DetalleCreditoDTO obtenerDetalleCredito(Integer idSolicitud, Integer idVendedor) {
        SolicitudesCredito sc = solicitudCreditoRepository
                .buscarDetalleCreditoVendedor(idSolicitud, idVendedor)
                .orElse(null);
        if (sc == null) return null;

        DetalleCreditoDTO detalle = new DetalleCreditoDTO();
        detalle.setSolicitud(mapSolicitud(sc));

        List<ProductoDTO> productos = detalleSolicitudCreditoRepository.findByIdSolicitud(idSolicitud)
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
                            unidadesRepository.findById(p.getIdUnidad())
                                    .ifPresent(u -> dto.setUnidad(u.getNombreUnidad()));
                        }
                        if (p.getIdEstadoProducto() != null) {
                            estadosProductoRepository.findById(p.getIdEstadoProducto())
                                    .ifPresent(ep -> dto.setEstado(ep.getNombreEstado()));
                        }
                    });
                    return dto;
                })
                .sorted(Comparator.comparing(ProductoDTO::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        detalle.setProductos(productos);
        return detalle;
    }

    // 9. actualizarEstadoCredito (APROBADO o RECHAZADO)
    @Transactional
    public boolean actualizarEstadoCredito(Integer idSolicitud, Integer idVendedor, String nuevoEstado) {
        if (idSolicitud == null || idSolicitud <= 0 || idVendedor == null || idVendedor <= 0) {
            throw new IllegalArgumentException("La solicitud o el vendedor no son válidos.");
        }
        if (nuevoEstado == null
                || (!"APROBADO".equalsIgnoreCase(nuevoEstado) && !"RECHAZADO".equalsIgnoreCase(nuevoEstado))) {
            throw new IllegalArgumentException("Estado no permitido. Use APROBADO o RECHAZADO.");
        }

        String estadoNormalizado = nuevoEstado.trim().toUpperCase();
        EstadosSolicitud estadoDestino = estadosSolicitudRepository.findByNombreEstadoIgnoreCase(estadoNormalizado)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el estado " + estadoNormalizado + " en la tabla estados_solicitud."));

        SolicitudesCredito sc = solicitudCreditoRepository.findById(idSolicitud).orElse(null);
        if (sc == null) return false;

        boolean esTiendaDelVendedor = tiendaRepository
                .findByIdTiendaAndIdVendedor(sc.getIdTienda(), idVendedor)
                .isPresent();
        boolean esCredito = tiposPrestamoRepository.findById(sc.getIdTipoPrestamo())
                .map(tp -> "CREDITO".equalsIgnoreCase(tp.getNombreTipo()))
                .orElse(false);
        boolean estaPendiente = estadosSolicitudRepository.findById(sc.getIdEstadoSolicitud())
                .map(es -> "PENDIENTE".equalsIgnoreCase(es.getNombreEstado()))
                .orElse(false);

        if (!esTiendaDelVendedor || !esCredito || !estaPendiente) {
            return false;
        }

        sc.setIdEstadoSolicitud(estadoDestino.getIdEstadoSolicitud());
        sc.setFechaAprobacion(LocalDateTime.now());
        solicitudCreditoRepository.save(sc);
        return true;
    }

    // 10. obtenerCreditosPendientes
    @Transactional(readOnly = true)
    public CreditosPendientesDTO obtenerCreditosPendientes(Integer idVendedor) {
        List<SolicitudesCredito> activos = solicitudCreditoRepository.listarCreditosActivosConSaldo(idVendedor);
        List<CreditoPendienteDTO> list = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Set<Integer> clientes = new HashSet<>();

        for (SolicitudesCredito sc : activos) {
            CreditoPendienteDTO dto = new CreditoPendienteDTO();
            dto.setIdSolicitud(sc.getIdSolicitud());
            dto.setIdCliente(sc.getIdCliente());
            dto.setIdTienda(sc.getIdTienda());
            dto.setMontoOriginal(sc.getMontoTotal());
            dto.setSaldoPendiente(sc.getSaldoPendiente());
            dto.setFechaSolicitud(sc.getFechaSolicitud());

            clienteRepository.findById(sc.getIdCliente())
                    .flatMap(c -> usuarioRepository.findById(c.getIdUsuario()))
                    .ifPresent(u -> dto.setCliente(u.getNombre() + " " + u.getApellido()));

            tiendaRepository.findById(sc.getIdTienda())
                    .ifPresent(t -> dto.setTienda(t.getNombreTienda()));

            estadosSolicitudRepository.findById(sc.getIdEstadoSolicitud())
                    .ifPresent(es -> dto.setEstado(es.getNombreEstado()));

            if (sc.getFechaVencimiento() != null) {
                LocalDate vencimiento = sc.getFechaVencimiento();
                dto.setFechaVencimiento(vencimiento);
                if (vencimiento.isBefore(LocalDate.now())) {
                    dto.setDiasMora(ChronoUnit.DAYS.between(vencimiento, LocalDate.now()));
                } else {
                    dto.setDiasMora(0);
                }
            } else {
                dto.setDiasMora(0);
            }

            list.add(dto);
            total = total.add(sc.getSaldoPendiente());
            clientes.add(sc.getIdCliente());
        }

        CreditosPendientesDTO result = new CreditosPendientesDTO();
        result.setCreditos(list);
        result.setCreditosActivos(list.size());
        result.setDineroTotalPorCobrar(total);
        result.setClientesConDeuda(clientes.size());
        return result;
    }

    // 11. listarCompradores
    @Transactional(readOnly = true)
    public List<CompradorDTO> listarCompradores() {
        return clienteRepository.listarOrdenadosPorNombre().stream().map(c -> {
            CompradorDTO dto = new CompradorDTO();
            dto.setIdCliente(c.getIdCliente());
            usuarioRepository.findById(c.getIdUsuario()).ifPresent(u -> {
                dto.setNombreCompleto(u.getNombre() + " " + u.getApellido());
                dto.setIdentificacion(u.getIdentificacion());
            });
            return dto;
        }).toList();
    }

    // 12. listarProductosParaCredito
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductosParaCredito(Integer idTienda, Integer idVendedor) {
        if (idTienda == null || idTienda <= 0 || idVendedor == null || idVendedor <= 0) {
            throw new IllegalArgumentException("Tienda o vendedor no válidos.");
        }
        return productoRepository.listarActivosDisponibles(idTienda, idVendedor)
                .stream()
                .map(this::mapProducto)
                .toList();
    }

    // 13. asignarCreditoCupo
    @Transactional
    public Integer asignarCreditoCupo(Integer idVendedor, Integer idTienda, Integer idCliente,
                                      BigDecimal cupo, LocalDate fechaVencimiento, String observaciones) {
        if (idVendedor == null || idVendedor <= 0 || idTienda == null || idTienda <= 0 || idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("Vendedor, tienda y comprador son obligatorios.");
        }
        if (cupo == null || cupo.signum() <= 0) {
            throw new IllegalArgumentException("El cupo debe ser mayor que cero.");
        }
        LocalDate hoy = LocalDate.now();
        if (fechaVencimiento == null || fechaVencimiento.isBefore(hoy.plusDays(15))
                || fechaVencimiento.isAfter(hoy.plusMonths(1))) {
            throw new IllegalArgumentException("El plazo del crédito debe estar entre 15 días y 1 mes.");
        }
        if (tiendaRepository.findByIdTiendaAndIdVendedor(idTienda, idVendedor).isEmpty()) {
            throw new SecurityException("La tienda no pertenece al vendedor autenticado.");
        }
        if (!clienteRepository.existsById(idCliente)) {
            throw new IllegalArgumentException("El comprador no existe.");
        }

        EstadosSolicitud estadoAprobado = estadosSolicitudRepository.findByNombreEstadoIgnoreCase("APROBADO")
                .orElseThrow(() -> new IllegalStateException("No existe el estado APROBADO."));
        TiposPrestamo tipoCredito = tiposPrestamoRepository.findByNombreTipoIgnoreCase("CREDITO")
                .orElseThrow(() -> new IllegalStateException("No existe el tipo CREDITO."));

        SolicitudesCredito sc = new SolicitudesCredito();
        sc.setIdCliente(idCliente);
        sc.setIdTienda(idTienda);
        sc.setMontoTotal(BigDecimal.ZERO);
        sc.setSaldoPendiente(BigDecimal.ZERO);
        sc.setCupoAprobado(cupo);
        sc.setIdTipoPrestamo(tipoCredito.getIdTipoPrestamo());
        sc.setIdEstadoSolicitud(estadoAprobado.getIdEstadoSolicitud());
        sc.setFechaSolicitud(LocalDateTime.now());
        sc.setFechaAprobacion(LocalDateTime.now());
        sc.setFechaVencimiento(fechaVencimiento);
        sc.setObservaciones(observaciones == null || observaciones.isBlank() ? null : observaciones.trim());

        return solicitudCreditoRepository.save(sc).getIdSolicitud();
    }

    // 14. consumirCredito
    @Transactional
    public void consumirCredito(Integer idVendedor, Integer idSolicitud, List<ItemCreditoDTO> items) {
        if (idVendedor == null || idVendedor <= 0 || idSolicitud == null || idSolicitud <= 0) {
            throw new IllegalArgumentException("El crédito o vendedor no son válidos.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un producto.");
        }

        LinkedHashMap<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (ItemCreditoDTO item : items) {
            if (item == null || item.getIdProducto() <= 0 || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada producto debe tener una cantidad mayor que cero.");
            }
            cantidades.merge(item.getIdProducto(), item.getCantidad(), Math::addExact);
        }

        SolicitudesCredito sc = solicitudCreditoRepository.buscarParaActualizar(idSolicitud, idVendedor)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El crédito no existe o no pertenece a la tienda del vendedor."));

        String tipoNombre = tiposPrestamoRepository.findById(sc.getIdTipoPrestamo())
                .map(TiposPrestamo::getNombreTipo).orElse("");
        if (!"CREDITO".equalsIgnoreCase(tipoNombre)) {
            throw new IllegalArgumentException("Solo se pueden consumir productos en créditos.");
        }

        String estadoNombre = estadosSolicitudRepository.findById(sc.getIdEstadoSolicitud())
                .map(EstadosSolicitud::getNombreEstado).orElse("");
        if (!"APROBADO".equalsIgnoreCase(estadoNombre)) {
            throw new IllegalArgumentException("El crédito debe estar aprobado para agregar productos.");
        }

        BigDecimal cupo = sc.getCupoAprobado() != null ? sc.getCupoAprobado() : BigDecimal.ZERO;
        BigDecimal saldoActual = sc.getSaldoPendiente() != null ? sc.getSaldoPendiente() : BigDecimal.ZERO;
        BigDecimal disponible = cupo.subtract(saldoActual);
        if (disponible.signum() <= 0) {
            throw new IllegalArgumentException("El crédito ya no tiene cupo disponible.");
        }

        TiposMovimiento tipoSalida = tiposMovimientoRepository.findByNombreTipoIgnoreCase("SALIDA")
                .orElseThrow(() -> new IllegalStateException("No existe el tipo de movimiento SALIDA."));

        record ItemValido(Productos producto, int cantidad, BigDecimal precio, BigDecimal subtotal) {}
        List<ItemValido> validos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (var entry : cantidades.entrySet()) {
            Productos prod = productoRepository.buscarActivoParaActualizar(entry.getKey(), sc.getIdTienda())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El producto #" + entry.getKey() + " no está disponible en la tienda."));

            int cantidad = entry.getValue();
            if (cantidad > prod.getStock()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para el producto #" + entry.getKey() + ". Disponible: " + prod.getStock() + ".");
            }
            BigDecimal precio = prod.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal).setScale(2, RoundingMode.HALF_UP);
            validos.add(new ItemValido(prod, cantidad, precio, subtotal));
        }

        if (total.compareTo(disponible) > 0) {
            throw new IllegalArgumentException("El total seleccionado supera el cupo disponible de $" + disponible.setScale(2));
        }

        for (ItemValido item : validos) {
            DetalleSolicitudCredito det = new DetalleSolicitudCredito();
            det.setIdSolicitud(idSolicitud);
            det.setIdProducto(item.producto().getIdProducto());
            det.setCantidad(item.cantidad());
            det.setPrecioUnitario(item.precio());
            det.setSubtotal(item.subtotal());
            detalleSolicitudCreditoRepository.save(det);

            // Descuento de inventario (Hibernate actualiza MySQL automáticamente al cerrar la transacción)
            item.producto().setStock(item.producto().getStock() - item.cantidad());

            MovimientosInventario mov = new MovimientosInventario();
            mov.setIdProducto(item.producto().getIdProducto());
            mov.setIdTipoMovimiento(tipoSalida.getIdTipoMovimiento());
            mov.setCantidad(item.cantidad());
            mov.setMotivo("Consumo crédito #" + idSolicitud);
            mov.setFechaMovimiento(LocalDateTime.now());
            movimientoInventarioRepository.save(mov);
        }

        sc.setMontoTotal(sc.getMontoTotal().add(total));
        sc.setSaldoPendiente(sc.getSaldoPendiente().add(total));
        solicitudCreditoRepository.saveAndFlush(sc);

        // Actualiza credito_actual del cliente sumando todos sus saldos pendientes
        clienteRepository.findById(sc.getIdCliente()).ifPresent(cliente -> {
            BigDecimal nuevaDeuda = solicitudCreditoRepository.sumarSaldoPendientePorCliente(cliente.getIdCliente());
            cliente.setCreditoActual(nuevaDeuda);
            clienteRepository.save(cliente);
        });
    }

    // 15. obtenerInventario
    @Transactional(readOnly = true)
    public InventarioData obtenerInventario(Integer idTienda, Integer idVendedor, String search) {
        String filtro = (search == null || search.isBlank()) ? null : search.trim();
        List<Productos> lista = productoRepository.buscarInventario(idTienda, idVendedor, filtro);

        InventarioData data = new InventarioData();
        for (Productos p : lista) {
            ProductoDTO dto = mapProducto(p);
            String estado = dto.getEstado() != null ? dto.getEstado().toUpperCase() : "";
            int stock = p.getStock() != null ? p.getStock() : 0;

            if ("ACTIVO".equals(estado)) data.activos++;
            if ("ACTIVO".equals(estado) && stock > 0) data.disponibles++;
            if (stock > 0 && stock <= STOCK_BAJO_UMBRAL) data.bajoStock++;
            if (stock <= 0 || "AGOTADO".equals(estado)) data.agotados++;

            data.productos.add(dto);
        }
        return data;
    }

    // --- Métodos privados de mapeo ---
    private TiendaDTO mapTienda(Tiendas t) {
        TiendaDTO dto = new TiendaDTO();
        dto.setIdTienda(t.getIdTienda());
        dto.setNombreTienda(t.getNombreTienda());
        dto.setNit(t.getNit());
        dto.setDireccion(t.getDireccion());
        dto.setTelefono(t.getTelefono());
        dto.setIdVendedor(t.getIdVendedor());
        return dto;
    }

    private ProductoDTO mapProducto(Productos p) {
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
    }

    private SolicitudCreditoDTO mapSolicitud(SolicitudesCredito sc) {
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
        tiposPrestamoRepository.findById(sc.getIdTipoPrestamo())
                .ifPresent(tp -> dto.setTipoPrestamo(tp.getNombreTipo()));
        estadosSolicitudRepository.findById(sc.getIdEstadoSolicitud())
                .ifPresent(es -> dto.setEstado(es.getNombreEstado()));
        return dto;
    }

    public static class InventarioData {
        public long activos, disponibles, bajoStock, agotados;
        public List<ProductoDTO> productos = new ArrayList<>();
    }
}