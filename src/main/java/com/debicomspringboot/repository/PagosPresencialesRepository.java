package com.debicomspringboot.repository;

import com.debicomspringboot.entity.PagosPresenciales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PagosPresencialesRepository extends JpaRepository<PagosPresenciales, Integer> {

    @Query("SELECT COALESCE(SUM(pp.montoPagado), 0) FROM PagosPresenciales pp WHERE pp.idVendedor = :idVendedor")
    BigDecimal sumarPagosPorVendedor(@Param("idVendedor") Integer idVendedor);

    @Query("SELECT pp FROM PagosPresenciales pp " +
            "JOIN SolicitudesCredito sc ON sc.idSolicitud = pp.idSolicitud " +
            "JOIN Tiendas t ON t.idTienda = sc.idTienda " +
            "WHERE pp.idVendedor = :idVendedor AND t.idVendedor = :idVendedor " +
            "ORDER BY pp.fechaPago DESC, pp.idPagoPresencial DESC")
    List<PagosPresenciales> listarHistorialPorVendedor(@Param("idVendedor") Integer idVendedor);
}