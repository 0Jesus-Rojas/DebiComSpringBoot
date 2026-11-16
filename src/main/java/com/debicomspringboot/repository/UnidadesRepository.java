package com.debicomspringboot.repository;

import com.debicomspringboot.entity.Unidades;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadesRepository extends JpaRepository<Unidades, Integer> {

    List<Unidades> findAllByOrderByNombreUnidadAsc();
}