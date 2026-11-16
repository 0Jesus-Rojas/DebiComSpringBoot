package com.debicomspringboot.service;

import com.debicomspringboot.entity.EstadosFactura;
import com.debicomspringboot.repository.EstadosFacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadoFacturaService {

    private final EstadosFacturaRepository estadoFacturaRepository;

    // Extra útil para llenar selectores (<select>) en Thymeleaf
    public List<EstadosFactura> listarEstadosFactura() {
        return estadoFacturaRepository.findAll();
    }

    // Reemplaza consultarEstadoFactura(int idEstadoFactura)
    public EstadosFactura consultarEstadoFactura(Integer idEstadoFactura) {
        return estadoFacturaRepository.findById(idEstadoFactura).orElse(null);
    }

    // Reemplaza insertarEstadoFactura(EstadosFactura miEstado)
    @Transactional
    public boolean insertarEstadoFactura(EstadosFactura miEstado) {
        try {
            miEstado.setIdEstadoFactura(null);
            estadoFacturaRepository.save(miEstado);
            return true;
        } catch (Exception e) {
            System.out.println("Error al insertar estado de factura: " + e.getMessage());
            return false;
        }
    }

    // Reemplaza actualizarEstadoFactura(EstadosFactura miEstado)
    @Transactional
    public boolean actualizarEstadoFactura(EstadosFactura miEstado) {
        if (miEstado.getIdEstadoFactura() != null && estadoFacturaRepository.existsById(miEstado.getIdEstadoFactura())) {
            estadoFacturaRepository.save(miEstado);
            return true;
        }
        System.out.println("No se encontró el estado de factura");
        return false;
    }

    // Reemplaza eliminarEstadoFactura(int idEstadoFactura)
    @Transactional
    public boolean eliminarEstadoFactura(Integer idEstadoFactura) {
        if (estadoFacturaRepository.existsById(idEstadoFactura)) {
            estadoFacturaRepository.deleteById(idEstadoFactura);
            return true;
        }
        return false;
    }
}