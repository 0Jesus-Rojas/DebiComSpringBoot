package com.debicomspringboot.repository;

import com.debicomspringboot.entity.EstadosProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadosProductoRepository extends JpaRepository<EstadosProducto, Integer> {
}