package com.debicomspringboot.repository;

import com.debicomspringboot.entity.Facturas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface FacturasRepository extends JpaRepository<Facturas, Integer> {

    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Facturas f JOIN EstadosFactura ef ON ef.idEstadoFactura = f.idEstadoFactura " +
            "WHERE f.idTienda = :idTienda AND UPPER(ef.nombreEstado) <> 'ANULADA'")
    BigDecimal sumarVentasTotalesPorTienda(@Param("idTienda") Integer idTienda);

    // Consulta una factura de venta directa (idSolicitud IS NULL) verificando que la tienda sea del vendedor
    @Query("SELECT f FROM Facturas f JOIN Tiendas t ON t.idTienda = f.idTienda " +
            "WHERE f.idFactura = :idFactura AND f.idSolicitud IS NULL AND t.idVendedor = :idVendedor")
    Optional<Facturas> buscarVentaDirectaPorIdYVendedor(@Param("idFactura") Integer idFactura, @Param("idVendedor") Integer idVendedor);
}