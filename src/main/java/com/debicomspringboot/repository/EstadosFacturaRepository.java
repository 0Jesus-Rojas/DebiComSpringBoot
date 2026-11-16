package com.debicomspringboot.repository;

import com.debicomspringboot.entity.EstadosFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadosFacturaRepository extends JpaRepository<EstadosFactura, Integer> {

    // Reemplaza: SELECT id_estado_factura FROM estados_factura WHERE UPPER(TRIM(nombre_estado)) IN ('PAGADA','PAGADO')
    @Query("SELECT ef FROM EstadosFactura ef WHERE UPPER(TRIM(ef.nombreEstado)) IN ('PAGADA', 'PAGADO')")
    Optional<EstadosFactura> buscarEstadoPagada();
}