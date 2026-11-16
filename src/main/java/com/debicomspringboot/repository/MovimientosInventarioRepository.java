package com.debicomspringboot.repository;

import com.debicomspringboot.entity.MovimientosInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientosInventarioRepository extends JpaRepository<MovimientosInventario, Integer> {
}