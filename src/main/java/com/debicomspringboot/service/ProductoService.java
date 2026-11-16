package com.debicomspringboot.service;

import com.debicomspringboot.entity.Productos;
import com.debicomspringboot.repository.ProductosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductosRepository productoRepository;

    public Productos consultarProducto(Integer idProducto) {
        return productoRepository.findById(idProducto).orElse(null);
    }

    @Transactional
    public boolean insertarProducto(Productos miProducto) {
        try {
            miProducto.setIdProducto(null);
            productoRepository.save(miProducto);
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean actualizarProducto(Productos miProducto) {
        if (miProducto.getIdProducto() != null && productoRepository.existsById(miProducto.getIdProducto())) {
            productoRepository.save(miProducto);
            return true;
        }
        System.out.println("No se encontró el producto");
        return false;
    }

    @Transactional
    public boolean eliminarProducto(Integer idProducto) {
        if (productoRepository.existsById(idProducto)) {
            productoRepository.deleteById(idProducto);
            return true;
        }
        return false;
    }
}