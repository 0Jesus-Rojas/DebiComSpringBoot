package com.debicomspringboot.service;

import com.debicomspringboot.entity.MovimientosInventario;
import com.debicomspringboot.repository.MovimientosInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovimientoInventarioService {

    private final MovimientosInventarioRepository movimientoInventarioRepository;

    public MovimientosInventario consultarMovimiento(Integer idMovimiento) {
        return movimientoInventarioRepository.findById(idMovimiento).orElse(null);
    }

    @Transactional
    public boolean insertarMovimiento(MovimientosInventario miMovimiento) {
        try {
            miMovimiento.setIdMovimiento(null);
            movimientoInventarioRepository.save(miMovimiento);
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean actualizarMovimiento(MovimientosInventario miMovimiento) {
        if (miMovimiento.getIdMovimiento() != null && movimientoInventarioRepository.existsById(miMovimiento.getIdMovimiento())) {
            movimientoInventarioRepository.save(miMovimiento);
            return true;
        }
        System.out.println("No se encontró el movimiento para actualizar");
        return false;
    }

    @Transactional
    public boolean eliminarMovimiento(Integer idMovimiento) {
        if (movimientoInventarioRepository.existsById(idMovimiento)) {
            movimientoInventarioRepository.deleteById(idMovimiento);
            return true;
        }
        System.out.println("No se encontró el movimiento para eliminar");
        return false;
    }
}