package com.debicomspringboot.service;

import com.debicomspringboot.entity.EstadosSolicitud;
import com.debicomspringboot.repository.EstadosSolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadoSolicitudService {

    private final EstadosSolicitudRepository estadoSolicitudRepository;

    public List<EstadosSolicitud> listarEstadosSolicitud() {
        return estadoSolicitudRepository.findAll();
    }

    public EstadosSolicitud consultarEstadoSolicitud(Integer idEstadoSolicitud) {
        return estadoSolicitudRepository.findById(idEstadoSolicitud).orElse(null);
    }

    @Transactional
    public boolean insertarEstadoSolicitud(EstadosSolicitud miEstado) {
        try {
            miEstado.setIdEstadoSolicitud(null);
            estadoSolicitudRepository.save(miEstado);
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean actualizarEstadoSolicitud(EstadosSolicitud miEstado) {
        if (miEstado.getIdEstadoSolicitud() != null && estadoSolicitudRepository.existsById(miEstado.getIdEstadoSolicitud())) {
            estadoSolicitudRepository.save(miEstado);
            return true;
        }
        System.out.println("No se encontró el estado de solicitud");
        return false;
    }

    @Transactional
    public boolean eliminarEstadoSolicitud(Integer idEstadoSolicitud) {
        if (estadoSolicitudRepository.existsById(idEstadoSolicitud)) {
            estadoSolicitudRepository.deleteById(idEstadoSolicitud);
            return true;
        }
        return false;
    }
}