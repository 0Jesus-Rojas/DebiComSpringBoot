package com.debicomspringboot.repository;

import com.debicomspringboot.entity.TiposMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TiposMovimientoRepository extends JpaRepository<TiposMovimiento, Integer> {
    Optional<TiposMovimiento> findByNombreTipoIgnoreCase(String nombreTipo);
    List<TiposMovimiento> findAllByOrderByNombreTipoAsc();
}