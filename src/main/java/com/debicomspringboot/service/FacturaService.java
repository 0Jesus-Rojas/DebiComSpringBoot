package com.debicomspringboot.service;

import com.debicomspringboot.entity.Facturas;
import com.debicomspringboot.repository.FacturasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturasRepository facturaRepository;

    public Facturas consultarFactura(Integer idFactura) {
        return facturaRepository.findById(idFactura).orElse(null);
    }

    @Transactional
    public boolean insertarFactura(Facturas miFactura) {
        try {
            miFactura.setIdFactura(null);
            facturaRepository.save(miFactura);
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean actualizarFactura(Facturas miFactura) {
        if (miFactura.getIdFactura() != null && facturaRepository.existsById(miFactura.getIdFactura())) {
            facturaRepository.save(miFactura);
            return true;
        }
        System.out.println("No se encontró la factura");
        return false;
    }

    @Transactional
    public boolean eliminarFactura(Integer idFactura) {
        if (facturaRepository.existsById(idFactura)) {
            facturaRepository.deleteById(idFactura);
            return true;
        }
        return false;
    }
}