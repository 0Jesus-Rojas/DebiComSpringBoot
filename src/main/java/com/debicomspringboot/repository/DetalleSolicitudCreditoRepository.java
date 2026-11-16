package com.debicomspringboot.repository;

import com.debicomspringboot.entity.DetalleSolicitudCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleSolicitudCreditoRepository extends JpaRepository<DetalleSolicitudCredito, Integer> {

    List<DetalleSolicitudCredito> findByIdSolicitud(Integer idSolicitud);
}