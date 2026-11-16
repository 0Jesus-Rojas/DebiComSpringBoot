package com.debicomspringboot.repository;

import com.debicomspringboot.entity.SolicitudesCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Repository
public interface FiadoRepository extends JpaRepository<SolicitudesCredito, Integer> {

    @Query(value = """
            SELECT p.id_producto, p.nombre, p.descripcion, p.precio_unitario, p.stock, p.id_tienda,
                   u.nombre_unidad AS unidad, ep.nombre_estado
            FROM productos p
            INNER JOIN tiendas t ON t.id_tienda = p.id_tienda
            LEFT JOIN unidades u ON u.id_unidad = p.id_unidad
            LEFT JOIN estados_producto ep ON ep.id_estado_producto = p.id_estado_producto
            WHERE p.id_tienda = :idTienda
              AND t.id_vendedor = :idVendedor
              AND p.stock > 0
              AND UPPER(COALESCE(ep.nombre_estado, '')) = 'ACTIVO'
            ORDER BY p.nombre
            """, nativeQuery = true)
    List<Object[]> listarProductosActivos(@Param("idTienda") int idTienda,
                                          @Param("idVendedor") int idVendedor);

    @Query(value = "SELECT COUNT(*) FROM tiendas WHERE id_tienda = :idTienda AND id_vendedor = :idVendedor", nativeQuery = true)
    int contarTiendaDeVendedor(@Param("idTienda") int idTienda, @Param("idVendedor") int idVendedor);

    @Query(value = "SELECT COUNT(*) FROM clientes WHERE id_cliente = :idCliente", nativeQuery = true)
    int contarCliente(@Param("idCliente") int idCliente);

    @Query(value = "SELECT id_estado_solicitud FROM estados_solicitud WHERE UPPER(nombre_estado) = 'APROBADO' LIMIT 1", nativeQuery = true)
    Integer obtenerIdEstadoAprobado();

    @Query(value = "SELECT id_tipo_prestamo FROM tipos_prestamo WHERE UPPER(nombre_tipo) = 'FIADO' LIMIT 1", nativeQuery = true)
    Integer obtenerIdTipoFiado();

    @Query(value = "SELECT id_tipo_movimiento FROM tipos_movimiento WHERE UPPER(nombre_tipo) = 'SALIDA' LIMIT 1", nativeQuery = true)
    Integer obtenerIdTipoMovimientoSalida();

    @Query(value = """
            SELECT p.id_producto, p.nombre, p.precio_unitario, p.stock
            FROM productos p
            INNER JOIN estados_producto ep ON ep.id_estado_producto = p.id_estado_producto
            WHERE p.id_producto = :idProducto
              AND p.id_tienda = :idTienda
              AND UPPER(ep.nombre_estado) = 'ACTIVO'
            FOR UPDATE
            """, nativeQuery = true)
    List<Object[]> bloquearProductoActivo(@Param("idProducto") int idProducto,
                                          @Param("idTienda") int idTienda);

    @Modifying
    @Query(value = """
            INSERT INTO solicitudes_credito
                (id_cliente, id_tienda, monto_total, saldo_pendiente, cupo_aprobado, id_tipo_prestamo,
                 id_estado_solicitud, fecha_solicitud, fecha_aprobacion, fecha_vencimiento, observaciones)
            VALUES (:idCliente, :idTienda, :total, :total, 0, :idTipoFiado,
                    :idEstado, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :fechaVencimiento, :observaciones)
            """, nativeQuery = true)
    void insertarSolicitudFiado(@Param("idCliente") int idCliente,
                                @Param("idTienda") int idTienda,
                                @Param("total") BigDecimal total,
                                @Param("idTipoFiado") int idTipoFiado,
                                @Param("idEstado") int idEstado,
                                @Param("fechaVencimiento") Date fechaVencimiento,
                                @Param("observaciones") String observaciones);

    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    int obtenerUltimoIdGenerado();

    @Modifying
    @Query(value = """
            INSERT INTO detalle_solicitud_credito (id_solicitud, id_producto, cantidad, precio_unitario, subtotal)
            VALUES (:idSolicitud, :idProducto, :cantidad, :precio, :subtotal)
            """, nativeQuery = true)
    int insertarDetalleFiado(@Param("idSolicitud") int idSolicitud,
                             @Param("idProducto") int idProducto,
                             @Param("cantidad") int cantidad,
                             @Param("precio") BigDecimal precio,
                             @Param("subtotal") BigDecimal subtotal);

    @Modifying
    @Query(value = "UPDATE productos SET stock = :nuevoStock WHERE id_producto = :idProducto", nativeQuery = true)
    int actualizarStockProducto(@Param("nuevoStock") int nuevoStock, @Param("idProducto") int idProducto);

    @Modifying
    @Query(value = """
            INSERT INTO movimientos_inventario (id_producto, id_tipo_movimiento, cantidad, motivo, fecha_movimiento)
            VALUES (:idProducto, :idSalida, :cantidad, :motivo, CURRENT_TIMESTAMP)
            """, nativeQuery = true)
    void insertarMovimientoSalida(@Param("idProducto") int idProducto,
                                  @Param("idSalida") int idSalida,
                                  @Param("cantidad") int cantidad,
                                  @Param("motivo") String motivo);

    @Modifying
    @Query(value = """
            UPDATE clientes
            SET credito_actual = (
                SELECT COALESCE(SUM(saldo_pendiente), 0)
                FROM solicitudes_credito
                WHERE id_cliente = :idCliente AND saldo_pendiente > 0
            )
            WHERE id_cliente = :idCliente
            """, nativeQuery = true)
    void recalcularCreditoCliente(@Param("idCliente") int idCliente);

    @Query(value = """
            SELECT sc.id_solicitud, sc.id_cliente, sc.id_tienda, t.nombre_tienda,
                   CONCAT(u.nombre, ' ', u.apellido) AS cliente, sc.monto_total, sc.saldo_pendiente,
                   sc.cupo_aprobado, sc.id_tipo_prestamo, tp.nombre_tipo AS tipo_prestamo,
                   es.nombre_estado, sc.fecha_solicitud, sc.fecha_aprobacion, sc.fecha_vencimiento, sc.observaciones
            FROM solicitudes_credito sc
            INNER JOIN tiendas t ON t.id_tienda = sc.id_tienda
            INNER JOIN clientes c ON c.id_cliente = sc.id_cliente
            INNER JOIN usuarios u ON u.id_usuario = c.id_usuario
            INNER JOIN estados_solicitud es ON es.id_estado_solicitud = sc.id_estado_solicitud
            INNER JOIN tipos_prestamo tp ON tp.id_tipo_prestamo = sc.id_tipo_prestamo
            WHERE t.id_vendedor = :idVendedor
              AND UPPER(tp.nombre_tipo) = 'FIADO'
              AND UPPER(es.nombre_estado) IN ('APROBADO', 'VENCIDO')
              AND sc.saldo_pendiente > 0
            ORDER BY sc.fecha_vencimiento ASC, sc.id_solicitud ASC
            """, nativeQuery = true)
    List<Object[]> listarFiadosPendientesCrudo(@Param("idVendedor") int idVendedor);
}