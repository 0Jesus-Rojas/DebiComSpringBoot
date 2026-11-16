package com.debicomspringboot.service;

import com.debicomspringboot.entity.TipoIdentificaciones;
import com.debicomspringboot.repository.TipoIdentificacionesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TipoIdentificacionService {

    private final TipoIdentificacionesRepository tipoIdentificacionesRepository;

    // 1. Reemplaza a consultarTipoIdentificacion(int idTipoIdentificacion)
    @Transactional(readOnly = true)
    public Optional<TipoIdentificaciones> consultarTipoIdentificacion(Integer idTipoIdentificacion) {
        return tipoIdentificacionesRepository.findById(idTipoIdentificacion);
    }

    // 2. Reemplaza a consultarTiposIdentificacion()
    @Transactional(readOnly = true)
    public List<TipoIdentificaciones> consultarTiposIdentificacion() {
        return tipoIdentificacionesRepository.findAllByOrderByNombreTipoAsc();
    }

    // 3. Reemplaza a insertarTipoIdentificacion(TipoIdentificaciones miTipo)
    @Transactional
    public TipoIdentificaciones insertarTipoIdentificacion(TipoIdentificaciones miTipo) {
        if (miTipo == null || miTipo.getNombreTipo() == null || miTipo.getNombreTipo().isBlank()) {
            throw new IllegalArgumentException("El nombre del tipo de identificación es obligatorio.");
        }
        miTipo.setIdTipoIdentificacion(null); // Asegura que JPA ejecute un INSERT y no un UPDATE
        miTipo.setNombreTipo(miTipo.getNombreTipo().trim());
        return tipoIdentificacionesRepository.save(miTipo);
    }

    // 4. Reemplaza a actualizarTipoIdentificacion(TipoIdentificaciones miTipo)
    @Transactional
    public boolean actualizarTipoIdentificacion(TipoIdentificaciones miTipo) {
        if (miTipo == null || miTipo.getIdTipoIdentificacion() == null) {
            return false;
        }

        return tipoIdentificacionesRepository.findById(miTipo.getIdTipoIdentificacion())
                .map(tipoActual -> {
                    tipoActual.setNombreTipo(miTipo.getNombreTipo().trim());
                    tipoIdentificacionesRepository.save(tipoActual);
                    return true;
                })
                .orElse(false);
    }

    // 5. Reemplaza a eliminarTipoIdentificacion(int idTipoIdentificacion)
    @Transactional
    public boolean eliminarTipoIdentificacion(Integer idTipoIdentificacion) {
        if (idTipoIdentificacion != null && tipoIdentificacionesRepository.existsById(idTipoIdentificacion)) {
            tipoIdentificacionesRepository.deleteById(idTipoIdentificacion);
            return true;
        }
        return false;
    }
}