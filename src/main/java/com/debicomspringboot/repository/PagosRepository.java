package com.debicomspringboot.repository;

import com.debicomspringboot.entity.Pagos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagosRepository extends JpaRepository<Pagos, Integer> {

    @Query(value = """
            SELECT sc.id_cliente, sc.saldo_pendiente
            FROM solicitudes_credito sc
            INNER JOIN tiendas t ON t.id_tienda = sc.id_tienda
            WHERE sc.id_solicitud = :idSolicitud
              AND t.id_vendedor = :idVendedor
              AND sc.saldo_pendiente > 0
            FOR UPDATE
            """, nativeQuery = true)
    List<Object[]> bloquearCreditoParaPago(@Param("idSolicitud") int idSolicitud,
                                           @Param("idVendedor") int idVendedor);

    @Query(value = "SELECT id_estado_solicitud FROM estados_solicitud WHERE UPPER(nombre_estado) = 'PAGADO' LIMIT 1", nativeQuery = true)
    Integer obtenerIdEstadoPagado();

    @Modifying
    @Query(value = """
            UPDATE solicitudes_credito
            SET saldo_pendiente = saldo_pendiente - :monto,
                id_estado_solicitud = CASE
                    WHEN saldo_pendiente <= :monto THEN :idPagado
                    ELSE id_estado_solicitud
                END
            WHERE id_solicitud = :idSolicitud
              AND saldo_pendiente >= :monto
            """, nativeQuery = true)
    int descontarSaldoCredito(@Param("monto") BigDecimal monto,
                              @Param("idPagado") int idPagado,
                              @Param("idSolicitud") int idSolicitud);

    @Query(value = "SELECT COALESCE(SUM(saldo_pendiente), 0) FROM solicitudes_credito WHERE id_cliente = :idCliente AND saldo_pendiente > 0", nativeQuery = true)
    BigDecimal calcularSaldoTotalCliente(@Param("idCliente") int idCliente);

    @Modifying
    @Query(value = "UPDATE clientes SET credito_actual = :saldoTotal WHERE id_cliente = :idCliente", nativeQuery = true)
    int actualizarCreditoCliente(@Param("saldoTotal") BigDecimal saldoTotal,
                                 @Param("idCliente") int idCliente);

    @Modifying
    @Query(value = """
            INSERT INTO pagos_presenciales (id_solicitud, id_cliente, id_vendedor, monto_pagado, numero_referencia, observaciones)
            VALUES (:idSolicitud, :idCliente, :idVendedor, :monto, :referencia, :observaciones)
            """, nativeQuery = true)
    void insertarHistorialPagoPresencial(@Param("idSolicitud") int idSolicitud,
                                         @Param("idCliente") int idCliente,
                                         @Param("idVendedor") int idVendedor,
                                         @Param("monto") BigDecimal monto,
                                         @Param("referencia") String referencia,
                                         @Param("observaciones") String observaciones);

    @Query("SELECT COALESCE(SUM(p.montoPagado), 0) FROM Pagos p " +
            "JOIN Facturas f ON f.idFactura = p.idFactura " +
            "JOIN Tiendas t ON t.idTienda = f.idTienda WHERE t.idVendedor = :idVendedor")
    BigDecimal sumarPagosPorVendedor(@Param("idVendedor") Integer idVendedor);

    // Obtiene el pago asociado a la factura para mostrar su referencia y método de pago en el PDF
    Optional<Pagos> findFirstByIdFactura(Integer idFactura);
}