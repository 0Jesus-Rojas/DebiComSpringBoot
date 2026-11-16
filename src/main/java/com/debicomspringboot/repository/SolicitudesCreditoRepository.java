package com.debicomspringboot.repository;

import com.debicomspringboot.entity.SolicitudesCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudesCreditoRepository extends JpaRepository<SolicitudesCredito, Integer> {

    @Query("SELECT sc FROM SolicitudesCredito sc " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE sc.idCliente = :idCliente " +
            "AND UPPER(tp.nombreTipo) IN ('CREDITO', 'FIADO') " +
            "ORDER BY sc.fechaSolicitud DESC, sc.idSolicitud DESC")
    List<SolicitudesCredito> listarPorCliente(@Param("idCliente") Integer idCliente);

    @Query("SELECT sc FROM SolicitudesCredito sc " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE sc.idSolicitud = :idSolicitud " +
            "AND sc.idCliente = :idCliente " +
            "AND UPPER(tp.nombreTipo) IN ('CREDITO', 'FIADO')")
    Optional<SolicitudesCredito> consultarPorIdYCliente(
            @Param("idSolicitud") Integer idSolicitud,
            @Param("idCliente") Integer idCliente);
    // Bloqueo FOR UPDATE para consumir cupo de forma segura ante peticiones simultáneas
    // Bloquea la solicitud durante el consumo del crédito.
// Se utiliza SQL nativo para generar la sintaxis compatible con MariaDB.
    @Query(value = """
        SELECT sc.*
        FROM solicitudes_credito sc
        INNER JOIN tiendas t
            ON t.id_tienda = sc.id_tienda
        WHERE sc.id_solicitud = :idSolicitud
          AND t.id_vendedor = :idVendedor
        FOR UPDATE
        """, nativeQuery = true)
    Optional<SolicitudesCredito> buscarParaActualizar(
            @Param("idSolicitud") Integer idSolicitud,
            @Param("idVendedor") Integer idVendedor);

    // Métricas del Dashboard
    @Query("SELECT COUNT(DISTINCT sc.idCliente) FROM SolicitudesCredito sc " +
            "JOIN Tiendas t ON t.idTienda = sc.idTienda " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE t.idVendedor = :idVendedor AND UPPER(tp.nombreTipo) = 'CREDITO'")
    long contarClientesUnicos(@Param("idVendedor") Integer idVendedor);

    @Query("SELECT COUNT(sc) FROM SolicitudesCredito sc " +
            "JOIN Tiendas t ON t.idTienda = sc.idTienda " +
            "JOIN EstadosSolicitud es ON es.idEstadoSolicitud = sc.idEstadoSolicitud " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE t.idVendedor = :idVendedor AND UPPER(tp.nombreTipo) = 'CREDITO' " +
            "AND UPPER(es.nombreEstado) = 'PENDIENTE' AND sc.fechaSolicitud >= :desde")
    long contarSolicitudesNuevas(@Param("idVendedor") Integer idVendedor, @Param("desde") LocalDateTime desde);

    @Query("SELECT COUNT(sc) FROM SolicitudesCredito sc " +
            "JOIN Tiendas t ON t.idTienda = sc.idTienda " +
            "JOIN EstadosSolicitud es ON es.idEstadoSolicitud = sc.idEstadoSolicitud " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE t.idVendedor = :idVendedor AND UPPER(tp.nombreTipo) = 'CREDITO' " +
            "AND UPPER(es.nombreEstado) = 'APROBADO'")
    long contarSolicitudesAprobadas(@Param("idVendedor") Integer idVendedor);

    @Query("SELECT COALESCE(SUM(sc.saldoPendiente), 0) FROM SolicitudesCredito sc " +
            "JOIN Tiendas t ON t.idTienda = sc.idTienda " +
            "JOIN EstadosSolicitud es ON es.idEstadoSolicitud = sc.idEstadoSolicitud " +
            "WHERE t.idVendedor = :idVendedor AND sc.saldoPendiente > 0 " +
            "AND UPPER(es.nombreEstado) IN ('APROBADO', 'VENCIDO')")
    BigDecimal sumarDeudaPendiente(@Param("idVendedor") Integer idVendedor);

    // Métricas por tienda[cite: 23]
    @Query("SELECT COUNT(sc) FROM SolicitudesCredito sc " +
            "JOIN EstadosSolicitud es ON es.idEstadoSolicitud = sc.idEstadoSolicitud " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE sc.idTienda = :idTienda AND UPPER(tp.nombreTipo) = 'CREDITO' " +
            "AND UPPER(es.nombreEstado) = 'APROBADO'")
    long contarCreditosOtorgadosPorTienda(@Param("idTienda") Integer idTienda);

    @Query("SELECT COUNT(DISTINCT sc.idCliente) FROM SolicitudesCredito sc " +
            "JOIN EstadosSolicitud es ON es.idEstadoSolicitud = sc.idEstadoSolicitud " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE sc.idTienda = :idTienda AND UPPER(tp.nombreTipo) = 'CREDITO' " +
            "AND UPPER(es.nombreEstado) = 'APROBADO' AND sc.saldoPendiente > 0")
    long contarClientesActivosPorTienda(@Param("idTienda") Integer idTienda);

    // Listados y consultas del vendedor[cite: 23]
    @Query("SELECT sc FROM SolicitudesCredito sc " +
            "JOIN Tiendas t ON t.idTienda = sc.idTienda " +
            "JOIN EstadosSolicitud es ON es.idEstadoSolicitud = sc.idEstadoSolicitud " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE t.idVendedor = :idVendedor AND UPPER(tp.nombreTipo) = 'CREDITO' " +
            "AND UPPER(es.nombreEstado) = 'PENDIENTE' ORDER BY sc.fechaSolicitud ASC, sc.idSolicitud ASC")
    List<SolicitudesCredito> listarPendientesVendedor(@Param("idVendedor") Integer idVendedor);

    @Query("SELECT sc FROM SolicitudesCredito sc " +
            "JOIN Tiendas t ON t.idTienda = sc.idTienda " +
            "JOIN EstadosSolicitud es ON es.idEstadoSolicitud = sc.idEstadoSolicitud " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE t.idVendedor = :idVendedor AND UPPER(tp.nombreTipo) IN ('CREDITO', 'FIADO') " +
            "AND UPPER(es.nombreEstado) IN ('APROBADO', 'PAGADO', 'VENCIDO') ORDER BY sc.idSolicitud DESC")
    List<SolicitudesCredito> listarHistorialCreditosVendedor(@Param("idVendedor") Integer idVendedor);

    @Query("SELECT sc FROM SolicitudesCredito sc " +
            "JOIN Tiendas t ON t.idTienda = sc.idTienda " +
            "JOIN EstadosSolicitud es ON es.idEstadoSolicitud = sc.idEstadoSolicitud " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE t.idVendedor = :idVendedor AND UPPER(tp.nombreTipo) = 'CREDITO' " +
            "AND (:estado IS NULL OR UPPER(es.nombreEstado) = UPPER(:estado)) " +
            "ORDER BY sc.fechaSolicitud DESC, sc.idSolicitud DESC")
    List<SolicitudesCredito> listarPorVendedorYEstado(@Param("idVendedor") Integer idVendedor,
                                                      @Param("estado") String estado);

    @Query("SELECT sc FROM SolicitudesCredito sc " +
            "JOIN Tiendas t ON t.idTienda = sc.idTienda " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE sc.idSolicitud = :idSolicitud AND t.idVendedor = :idVendedor " +
            "AND UPPER(tp.nombreTipo) IN ('CREDITO', 'FIADO')")
    Optional<SolicitudesCredito> buscarDetalleCreditoVendedor(@Param("idSolicitud") Integer idSolicitud,
                                                              @Param("idVendedor") Integer idVendedor);

    @Query("SELECT sc FROM SolicitudesCredito sc " +
            "JOIN Tiendas t ON t.idTienda = sc.idTienda " +
            "JOIN EstadosSolicitud es ON es.idEstadoSolicitud = sc.idEstadoSolicitud " +
            "JOIN TiposPrestamo tp ON tp.idTipoPrestamo = sc.idTipoPrestamo " +
            "WHERE t.idVendedor = :idVendedor AND UPPER(tp.nombreTipo) = 'CREDITO' " +
            "AND UPPER(es.nombreEstado) IN ('APROBADO', 'VENCIDO') AND sc.saldoPendiente > 0 " +
            "ORDER BY sc.fechaVencimiento ASC, sc.idSolicitud ASC")
    List<SolicitudesCredito> listarCreditosActivosConSaldo(@Param("idVendedor") Integer idVendedor);

    // Recálculo de credito_actual del cliente[cite: 23]
    @Query("SELECT COALESCE(SUM(sc.saldoPendiente), 0) FROM SolicitudesCredito sc " +
            "WHERE sc.idCliente = :idCliente AND sc.saldoPendiente > 0")
    BigDecimal sumarSaldoPendientePorCliente(@Param("idCliente") Integer idCliente);
}