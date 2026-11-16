package com.debicomspringboot.service;

import com.debicomspringboot.entity.EstadosProducto;
import com.debicomspringboot.repository.EstadosProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadoProductoService {

    private final EstadosProductoRepository estadoProductoRepository;

    // Reemplaza listarEstadosProducto() manteniendo el ORDER BY id_estado_producto
    public List<EstadosProducto> listarEstadosProducto() {
        return estadoProductoRepository.findAll(Sort.by(Sort.Direction.ASC, "idEstadoProducto"));
    }

    // Reemplaza consultarEstadoProducto(int idEstadoProducto)
    public EstadosProducto consultarEstadoProducto(Integer idEstadoProducto) {
        return estadoProductoRepository.findById(idEstadoProducto).orElse(null);
    }

    // Reemplaza insertarEstadoProducto(EstadosProducto miEstado)
    @Transactional
    public boolean insertarEstadoProducto(EstadosProducto miEstado) {
        try {
            miEstado.setIdEstadoProducto(null);
            estadoProductoRepository.save(miEstado);
            return true;
        } catch (Exception e) {
            System.out.println("Error al insertar estado de producto: " + e.getMessage());
            return false;
        }
    }

    // Reemplaza actualizarEstadoProducto(EstadosProducto miEstado)
    @Transactional
    public boolean actualizarEstadoProducto(EstadosProducto miEstado) {
        if (miEstado.getIdEstadoProducto() != null && estadoProductoRepository.existsById(miEstado.getIdEstadoProducto())) {
            estadoProductoRepository.save(miEstado);
            return true;
        }
        System.out.println("No se encontró el estado del producto");
        return false;
    }

    // Reemplaza eliminarEstadoProducto(int idEstadoProducto)
    @Transactional
    public boolean eliminarEstadoProducto(Integer idEstadoProducto) {
        if (estadoProductoRepository.existsById(idEstadoProducto)) {
            estadoProductoRepository.deleteById(idEstadoProducto);
            return true;
        }
        return false;
    }
}