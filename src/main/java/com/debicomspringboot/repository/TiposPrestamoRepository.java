package com.debicomspringboot.repository;

import com.debicomspringboot.entity.TiposPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TiposPrestamoRepository extends JpaRepository<TiposPrestamo, Integer> {

    Optional<TiposPrestamo> findByNombreTipoIgnoreCase(String nombreTipo);
}