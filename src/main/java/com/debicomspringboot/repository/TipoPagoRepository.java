package com.debicomspringboot.repository;

import com.debicomspringboot.entity.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoPagoRepository extends JpaRepository<TipoPago, Integer> {
    List<TipoPago> findAllByOrderByNombrePagoAsc();
    Optional<TipoPago> findByNombrePagoIgnoreCase(String nombrePago);
    @Query("SELECT tp FROM TipoPago tp WHERE UPPER(TRIM(tp.nombrePago)) NOT LIKE '%PRESENCIAL%' ORDER BY tp.nombrePago ASC")
    List<TipoPago> listarNoPresenciales();

    // Valida que el método de pago exista y no sea presencial
    @Query("SELECT tp FROM TipoPago tp WHERE tp.idTipoPago = :idTipoPago " +
            "AND UPPER(TRIM(tp.nombrePago)) NOT LIKE '%PRESENCIAL%'")
    Optional<TipoPago> buscarNoPresencialPorId(@Param("idTipoPago") Integer idTipoPago);
}