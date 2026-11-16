package com.debicomspringboot.service;

import com.debicomspringboot.entity.DetalleSolicitudCredito;
import com.debicomspringboot.repository.DetalleSolicitudCreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DetalleSolicitudCreditoService {

    private final DetalleSolicitudCreditoRepository detalleSolicitudCreditoRepository;

    // Reemplaza consultarDetalle(int idDetalleSolicitud)
    public DetalleSolicitudCredito consultarDetalle(Integer idDetalleSolicitud) {
        return detalleSolicitudCreditoRepository.findById(idDetalleSolicitud).orElse(null);
    }

    // Reemplaza insertarDetalle(DetalleSolicitudCredito miDetalle)
    @Transactional
    public boolean insertarDetalle(DetalleSolicitudCredito miDetalle) {
        try {
            miDetalle.setIdDetalleSolicitud(null);
            detalleSolicitudCreditoRepository.save(miDetalle);
            return true;
        } catch (Exception e) {
            System.out.println("Error al insertar detalle de solicitud: " + e.getMessage());
            return false;
        }
    }

    // Reemplaza actualizarDetalle(DetalleSolicitudCredito miDetalle)
    @Transactional
    public boolean actualizarDetalle(DetalleSolicitudCredito miDetalle) {
        if (miDetalle.getIdDetalleSolicitud() != null && detalleSolicitudCreditoRepository.existsById(miDetalle.getIdDetalleSolicitud())) {
            detalleSolicitudCreditoRepository.save(miDetalle);
            return true;
        }
        System.out.println("No se encontró el detalle de la solicitud");
        return false;
    }

    // Reemplaza eliminarDetalle(int idDetalleSolicitud)
    @Transactional
    public boolean eliminarDetalle(Integer idDetalleSolicitud) {
        if (detalleSolicitudCreditoRepository.existsById(idDetalleSolicitud)) {
            detalleSolicitudCreditoRepository.deleteById(idDetalleSolicitud);
            return true;
        }
        return false;
    }
}