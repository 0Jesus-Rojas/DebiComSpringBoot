package com.debicomspringboot.repository;

import com.debicomspringboot.entity.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductosRepository extends JpaRepository<Productos, Integer> {

    @Query("SELECT p FROM Productos p JOIN Tiendas t ON t.idTienda = p.idTienda " +
            "JOIN EstadosProducto ep ON ep.idEstadoProducto = p.idEstadoProducto " +
            "WHERE p.idTienda = :idTienda AND t.idVendedor = :idVendedor AND p.stock > 0 " +
            "AND UPPER(COALESCE(ep.nombreEstado, '')) = 'ACTIVO' ORDER BY p.nombre ASC")
    List<Productos> listarActivosDisponibles(@Param("idTienda") Integer idTienda,
                                             @Param("idVendedor") Integer idVendedor);

    // Bloqueo del producto para evitar consumos simultáneos del mismo inventario.
    @Query(value = """
        SELECT p.*
        FROM productos p
        INNER JOIN estados_producto ep
            ON ep.id_estado_producto = p.id_estado_producto
        WHERE p.id_producto = :idProducto
          AND p.id_tienda = :idTienda
          AND UPPER(TRIM(ep.nombre_estado)) = 'ACTIVO'
        FOR UPDATE
        """, nativeQuery = true)
    Optional<Productos> buscarActivoParaActualizar(
            @Param("idProducto") Integer idProducto,
            @Param("idTienda") Integer idTienda);

    @Query("SELECT p FROM Productos p JOIN Tiendas t ON t.idTienda = p.idTienda " +
            "WHERE p.idTienda = :idTienda AND t.idVendedor = :idVendedor " +
            "AND (:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY p.nombre ASC")
    List<Productos> buscarInventario(@Param("idTienda") Integer idTienda,
                                     @Param("idVendedor") Integer idVendedor,
                                     @Param("search") String search);

    long countByIdTienda(Integer idTienda);
}