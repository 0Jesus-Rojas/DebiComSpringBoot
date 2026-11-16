package com.debicomspringboot.service;

import com.debicomspringboot.entity.DetalleFacturas;
import com.debicomspringboot.repository.DetalleFacturasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DetalleFacturaService {

    private final DetalleFacturasRepository detalleFacturaRepository;

    // Reemplaza consultarDetalleFactura(int idDetalleFactura)
    public DetalleFacturas consultarDetalleFactura(Integer idDetalleFactura) {
        return detalleFacturaRepository.findById(idDetalleFactura).orElse(null);
    }

    // Reemplaza insertarDetalleFactura(DetalleFacturas miDetalle)
    @Transactional
    public boolean insertarDetalleFactura(DetalleFacturas miDetalle) {
        try {
            miDetalle.setIdDetalleFactura(null);
            detalleFacturaRepository.save(miDetalle);
            return true;
        } catch (Exception e) {
            System.out.println("Error al insertar detalle de factura: " + e.getMessage());
            return false;
        }
    }

    // Reemplaza actualizarDetalleFactura(DetalleFacturas miDetalle)
    @Transactional
    public boolean actualizarDetalleFactura(DetalleFacturas miDetalle) {
        if (miDetalle.getIdDetalleFactura() != null && detalleFacturaRepository.existsById(miDetalle.getIdDetalleFactura())) {
            detalleFacturaRepository.save(miDetalle);
            return true;
        }
        System.out.println("No se encontró el detalle de la factura");
        return false;
    }

    // Reemplaza eliminarDetalleFactura(int idDetalleFactura)
    @Transactional
    public boolean eliminarDetalleFactura(Integer idDetalleFactura) {
        if (detalleFacturaRepository.existsById(idDetalleFactura)) {
            detalleFacturaRepository.deleteById(idDetalleFactura);
            return true;
        }
        return false;
    }
}