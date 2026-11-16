package com.debicomspringboot.service;

import com.debicomspringboot.entity.TiposMovimiento;
import com.debicomspringboot.repository.TiposMovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TipoMovimientoService {

    private final TiposMovimientoRepository tiposMovimientoRepository;

    // 1. Reemplaza a consultarTipoMovimiento(int idTipoMovimiento)
    @Transactional(readOnly = true)
    public Optional<TiposMovimiento> consultarTipoMovimiento(Integer idTipoMovimiento) {
        return tiposMovimientoRepository.findById(idTipoMovimiento);
    }

    // Método de apoyo para listar todos los tipos en vistas Thymeleaf
    @Transactional(readOnly = true)
    public List<TiposMovimiento> listarTiposMovimiento() {
        return tiposMovimientoRepository.findAllByOrderByNombreTipoAsc();
    }

    // 2. Reemplaza a insertarTipoMovimiento(TiposMovimiento tipoMovimiento)
    @Transactional
    public TiposMovimiento insertarTipoMovimiento(TiposMovimiento tipoMovimiento) {
        if (tipoMovimiento == null || tipoMovimiento.getNombreTipo() == null || tipoMovimiento.getNombreTipo().isBlank()) {
            throw new IllegalArgumentException("El nombre del tipo de movimiento es obligatorio.");
        }
        tipoMovimiento.setIdTipoMovimiento(null); // Fuerza INSERT con AUTO_INCREMENT en MySQL
        tipoMovimiento.setNombreTipo(tipoMovimiento.getNombreTipo().trim());
        return tiposMovimientoRepository.save(tipoMovimiento);
    }

    // 3. Reemplaza a actualizarTipoMovimiento(TiposMovimiento tipoMovimiento)
    @Transactional
    public boolean actualizarTipoMovimiento(TiposMovimiento tipoMovimiento) {
        if (tipoMovimiento == null || tipoMovimiento.getIdTipoMovimiento() == null) {
            return false;
        }

        return tiposMovimientoRepository.findById(tipoMovimiento.getIdTipoMovimiento())
                .map(actual -> {
                    actual.setNombreTipo(tipoMovimiento.getNombreTipo().trim());
                    tiposMovimientoRepository.save(actual);
                    return true;
                })
                .orElse(false);
    }

    // 4. Reemplaza a eliminarTipoMovimiento(int idTipoMovimiento)
    @Transactional
    public boolean eliminarTipoMovimiento(Integer idTipoMovimiento) {
        if (idTipoMovimiento != null && tiposMovimientoRepository.existsById(idTipoMovimiento)) {
            tiposMovimientoRepository.deleteById(idTipoMovimiento);
            return true;
        }
        return false;
    }
}