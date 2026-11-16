package com.debicomspringboot.repository;

import com.debicomspringboot.entity.EstadosSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadosSolicitudRepository extends JpaRepository<EstadosSolicitud, Integer> {

    // Genera: SELECT * FROM estados_solicitud WHERE UPPER(nombre_estado) = UPPER(?)
    Optional<EstadosSolicitud> findByNombreEstadoIgnoreCase(String nombreEstado);
}