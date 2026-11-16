package com.debicomspringboot.service;

import com.debicomspringboot.dto.TiendaDTO;
import com.debicomspringboot.entity.Tiendas;
import com.debicomspringboot.repository.TiendasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio para la administración de tiendas del vendedor.
 */
@Service
@RequiredArgsConstructor
public class TiendaService {

    private final TiendasRepository tiendaRepository;
    private final VendedorService vendedorService;

    @Transactional(readOnly = true)
    public List<TiendaDTO> listarTiendas(Integer idVendedor) {
        validarVendedor(idVendedor);
        return vendedorService.listarTiendasDelVendedor(idVendedor);
    }

    @Transactional(readOnly = true)
    public TiendaDTO obtenerTienda(Integer idTienda, Integer idVendedor) {
        validarVendedor(idVendedor);
        if (idTienda == null || idTienda <= 0
                || tiendaRepository.findByIdTiendaAndIdVendedor(idTienda, idVendedor).isEmpty()) {
            return null;
        }
        return vendedorService.obtenerMetricasTienda(idTienda, idVendedor);
    }

    @Transactional
    public Integer crearTienda(Integer idVendedor, String nombre, String nit, String direccion, String telefono) {
        validarVendedor(idVendedor);
        validarDatos(nombre, nit, direccion, telefono);

        Tiendas tienda = new Tiendas();
        tienda.setNombreTienda(nombre.trim());
        tienda.setNit(nit.trim());
        tienda.setDireccion(direccion.trim());
        tienda.setTelefono(telefono.trim());
        tienda.setIdVendedor(idVendedor);

        Tiendas guardada = tiendaRepository.save(tienda);
        return guardada.getIdTienda();
    }

    @Transactional
    public boolean actualizarTienda(Integer idVendedor, Integer idTienda,
                                    String nombre, String nit, String direccion, String telefono) {
        validarVendedor(idVendedor);
        if (idTienda == null || idTienda <= 0) {
            throw new SecurityException("La tienda no pertenece al vendedor autenticado.");
        }

        Tiendas tienda = tiendaRepository.findByIdTiendaAndIdVendedor(idTienda, idVendedor)
                .orElseThrow(() -> new SecurityException("La tienda no pertenece al vendedor autenticado."));

        validarDatos(nombre, nit, direccion, telefono);

        tienda.setNombreTienda(nombre.trim());
        tienda.setNit(nit.trim());
        tienda.setDireccion(direccion.trim());
        tienda.setTelefono(telefono.trim());

        tiendaRepository.save(tienda);
        return true;
    }

    private void validarVendedor(Integer idVendedor) {
        if (idVendedor == null || idVendedor <= 0) {
            throw new IllegalArgumentException("Usuario vendedor inválido.");
        }
    }

    private void validarDatos(String nombre, String nit, String direccion, String telefono) {
        if (blank(nombre) || blank(nit) || blank(direccion) || blank(telefono)) {
            throw new IllegalArgumentException("Todos los datos de la tienda son obligatorios.");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
    @Transactional(readOnly = true)
    public List<TiendaDTO> listarTiendasDisponibles() {
        return tiendaRepository.findAllByOrderByNombreTiendaAsc()
                .stream()
                .map(t -> {
                    TiendaDTO dto = new TiendaDTO();
                    dto.setIdTienda(t.getIdTienda());
                    dto.setNombreTienda(t.getNombreTienda());
                    dto.setNit(t.getNit());
                    dto.setDireccion(t.getDireccion());
                    dto.setTelefono(t.getTelefono());
                    dto.setIdVendedor(t.getIdVendedor());
                    return dto;
                })
                .toList();
    }
}