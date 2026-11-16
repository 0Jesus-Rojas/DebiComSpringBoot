package com.debicomspringboot.repository;

import com.debicomspringboot.entity.TipoIdentificaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TipoIdentificacionesRepository extends JpaRepository<TipoIdentificaciones, Integer> {
    List<TipoIdentificaciones> findAllByOrderByNombreTipoAsc();
}