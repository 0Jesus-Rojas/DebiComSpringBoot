package com.debicomspringboot.service;

import com.debicomspringboot.dto.TiendaDTO;
import com.debicomspringboot.entity.Tiendas;
import com.debicomspringboot.repository.TiendasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TiendasService {

    private final TiendasRepository tiendaRepository;

    // 1. Reemplaza a consultarTienda(int idTienda)
    @Transactional(readOnly = true)
    public Optional<Tiendas> consultarTienda(Integer idTienda) {
        return tiendaRepository.findById(idTienda);
    }

    // 2. Reemplaza a listarTiendasDisponibles()
    @Transactional(readOnly = true)
    public List<TiendaDTO> listarTiendasDisponibles() {
        return tiendaRepository.findAllByOrderByNombreTiendaAsc()
                .stream()
                .map(tienda -> {
                    TiendaDTO dto = new TiendaDTO();
                    dto.setIdTienda(tienda.getIdTienda());
                    dto.setNombreTienda(tienda.getNombreTienda());
                    dto.setNit(tienda.getNit());
                    dto.setDireccion(tienda.getDireccion());
                    dto.setTelefono(tienda.getTelefono());
                    return dto;
                })
                .toList();
    }

    // 3. Reemplaza a insertarTienda(Tiendas tienda) y retorna el ID generado
    @Transactional
    public Integer insertarTienda(Tiendas tienda) {
        if (tienda == null) {
            throw new IllegalArgumentException("Los datos de la tienda son obligatorios.");
        }
        if (tienda.getFechaRegistro() == null) {
            tienda.setFechaRegistro(LocalDateTime.now());
        }
        Tiendas guardada = tiendaRepository.save(tienda);
        return guardada.getIdTienda();
    }

    // 4. Reemplaza a actualizarDatosTienda(Tiendas tienda) y actualizarTienda(Tiendas tienda)
    @Transactional
    public boolean actualizarDatosTienda(Tiendas tiendaEditada) {
        if (tiendaEditada == null || tiendaEditada.getIdTienda() == null) {
            return false;
        }

        return tiendaRepository.findById(tiendaEditada.getIdTienda())
                .map(tiendaActual -> {
                    // Actualiza solo los campos editables; mantiene intactos idVendedor y fechaRegistro
                    tiendaActual.setNombreTienda(tiendaEditada.getNombreTienda());
                    tiendaActual.setNit(tiendaEditada.getNit());
                    tiendaActual.setDireccion(tiendaEditada.getDireccion());
                    tiendaActual.setTelefono(tiendaEditada.getTelefono());
                    tiendaRepository.save(tiendaActual);
                    return true;
                })
                .orElse(false);
    }

    // 5. Reemplaza a perteneceAVendedor(int idTienda, int idVendedor)
    @Transactional(readOnly = true)
    public boolean perteneceAVendedor(Integer idTienda, Integer idVendedor) {
        if (idTienda == null || idVendedor == null) {
            return false;
        }
        return tiendaRepository.existsByIdTiendaAndIdVendedor(idTienda, idVendedor);
    }

    // 6. Reemplaza a eliminarTienda(int idTienda)
    @Transactional
    public boolean eliminarTienda(Integer idTienda) {
        if (idTienda != null && tiendaRepository.existsById(idTienda)) {
            tiendaRepository.deleteById(idTienda);
            return true;
        }
        return false;
    }

}