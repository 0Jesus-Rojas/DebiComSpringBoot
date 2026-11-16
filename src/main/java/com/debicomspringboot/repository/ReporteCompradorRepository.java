package com.debicomspringboot.repository;

import com.debicomspringboot.entity.Pagos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteCompradorRepository extends JpaRepository<Pagos, Integer> {

    @Query(value = """
            SELECT p.id_pago, p.id_factura, f.numero_factura,
                   p.numero_referencia_pago, p.monto_pagado, p.fecha_pago,
                   tp.nombre_pago AS tipo_pago, t.nombre_tienda, p.observaciones,
                   CASE WHEN f.id_solicitud IS NULL THEN 'Venta directa' ELSE 'Pago de crédito' END AS origen
            FROM pagos p
            INNER JOIN facturas f ON f.id_factura = p.id_factura
            INNER JOIN clientes c ON c.id_cliente = f.id_cliente
            INNER JOIN tiendas t ON t.id_tienda = f.id_tienda
            LEFT JOIN tipo_pago tp ON tp.id_tipo_pago = p.id_tipo_pago
            WHERE c.id_usuario = :idUsuario
              AND (tp.nombre_pago IS NULL OR UPPER(TRIM(tp.nombre_pago)) NOT LIKE '%PRESENCIAL%')
            ORDER BY p.fecha_pago DESC, p.id_pago DESC
            """, nativeQuery = true)
    List<Object[]> listarPagosPorUsuarioCrudo(@Param("idUsuario") int idUsuario);

    @Query(value = """
            SELECT sc.id_solicitud, COALESCE(f.id_factura, 0) AS id_factura,
                   t.nombre_tienda,
                   CASE WHEN UPPER(tp.nombre_tipo) = 'CREDITO' THEN sc.cupo_aprobado ELSE sc.monto_total END AS monto_mostrar,
                   sc.saldo_pendiente, sc.fecha_vencimiento, es.nombre_estado, tp.nombre_tipo AS tipo_prestamo
            FROM solicitudes_credito sc
            INNER JOIN clientes c ON c.id_cliente = sc.id_cliente
            INNER JOIN tiendas t ON t.id_tienda = sc.id_tienda
            INNER JOIN estados_solicitud es ON es.id_estado_solicitud = sc.id_estado_solicitud
            INNER JOIN tipos_prestamo tp ON tp.id_tipo_prestamo = sc.id_tipo_prestamo
            LEFT JOIN facturas f ON f.id_solicitud = sc.id_solicitud
            WHERE c.id_usuario = :idUsuario
              AND sc.saldo_pendiente > 0
              AND UPPER(es.nombre_estado) = 'APROBADO'
            ORDER BY sc.fecha_vencimiento ASC, sc.id_solicitud ASC
            """, nativeQuery = true)
    List<Object[]> listarDeudasPorUsuarioCrudo(@Param("idUsuario") int idUsuario);
}