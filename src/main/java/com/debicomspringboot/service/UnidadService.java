package com.debicomspringboot.service;

import com.debicomspringboot.entity.Unidades;
import com.debicomspringboot.repository.UnidadesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnidadService {

    private final UnidadesRepository unidadesRepository;

    // 1. Reemplaza a listarUnidades()
    @Transactional(readOnly = true)
    public List<Unidades> listarUnidades() {
        return unidadesRepository.findAllByOrderByNombreUnidadAsc();
    }

    // 2. Reemplaza a consultarUnidad(int idUnidad)
    @Transactional(readOnly = true)
    public Optional<Unidades> consultarUnidad(Integer idUnidad) {
        return unidadesRepository.findById(idUnidad);
    }

    // 3. Reemplaza a insertarUnidad(Unidades miUnidad)
    @Transactional
    public Unidades insertarUnidad(Unidades miUnidad) {
        if (miUnidad == null || miUnidad.getNombreUnidad() == null || miUnidad.getNombreUnidad().isBlank()) {
            throw new IllegalArgumentException("El nombre de la unidad es obligatorio.");
        }
        miUnidad.setIdUnidad(null); // Fuerza INSERT con AUTO_INCREMENT en MySQL
        miUnidad.setNombreUnidad(miUnidad.getNombreUnidad().trim());
        return unidadesRepository.save(miUnidad);
    }

    // 4. Reemplaza a actualizarUnidad(Unidades miUnidad)
    @Transactional
    public boolean actualizarUnidad(Unidades miUnidad) {
        if (miUnidad == null || miUnidad.getIdUnidad() == null) {
            return false;
        }

        return unidadesRepository.findById(miUnidad.getIdUnidad())
                .map(unidadActual -> {
                    unidadActual.setNombreUnidad(miUnidad.getNombreUnidad().trim());
                    unidadesRepository.save(unidadActual);
                    return true;
                })
                .orElse(false);
    }

    // 5. Reemplaza a eliminarUnidad(int idUnidad)
    @Transactional
    public boolean eliminarUnidad(Integer idUnidad) {
        if (idUnidad != null && unidadesRepository.existsById(idUnidad)) {
            unidadesRepository.deleteById(idUnidad);
            return true;
        }
        return false;
    }
}