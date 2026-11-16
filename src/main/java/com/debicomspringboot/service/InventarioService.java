package com.debicomspringboot.service;

import com.debicomspringboot.dto.TiendaDTO;
import com.debicomspringboot.entity.EstadosProducto;
import com.debicomspringboot.entity.Productos;
import com.debicomspringboot.entity.Unidades;
import com.debicomspringboot.repository.EstadosProductoRepository;
import com.debicomspringboot.repository.ProductosRepository;
import com.debicomspringboot.repository.TiendasRepository;
import com.debicomspringboot.repository.UnidadesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final VendedorService vendedorService;
    private final TiendasRepository tiendaRepository;
    private final ProductosRepository productosRepository;
    private final UnidadesRepository unidadesRepository;
    private final EstadosProductoRepository estadosProductoRepository;

    @Transactional(readOnly = true)
    public List<TiendaDTO> listarTiendas(Integer idVendedor) {
        return vendedorService.listarTiendasDelVendedor(idVendedor);
    }

    @Transactional(readOnly = true)
    public VendedorService.InventarioData obtenerInventario(Integer idTienda, Integer idVendedor, String search) {
        asegurarPropiedadTienda(idTienda, idVendedor);
        return vendedorService.obtenerInventario(idTienda, idVendedor, search);
    }

    @Transactional(readOnly = true)
    public List<Unidades> listarUnidades() {
        return unidadesRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<EstadosProducto> listarEstadosProducto() {
        return estadosProductoRepository.findAll();
    }

    @Transactional
    public Integer crearProducto(Integer idVendedor, Integer idTienda, String nombre, String descripcion,
                                 BigDecimal precio, int stock, Integer idUnidad, Integer idEstadoProducto) {
        asegurarPropiedadTienda(idTienda, idVendedor);
        validarCamposProducto(nombre, precio, stock, idUnidad, idEstadoProducto);

        Productos producto = new Productos();
        producto.setNombre(nombre.trim());
        producto.setDescripcion(descripcion == null || descripcion.isBlank() ? null : descripcion.trim());
        producto.setPrecioUnitario(precio.setScale(2, RoundingMode.HALF_UP));
        producto.setStock(stock);
        producto.setIdTienda(idTienda);
        producto.setIdUnidad(idUnidad);
        producto.setIdEstadoProducto(idEstadoProducto);

        Productos guardado = productosRepository.save(producto);
        return guardado.getIdProducto();
    }

    @Transactional
    public boolean actualizarProducto(Integer idVendedor, Integer idProducto, Integer idTienda,
                                      String nombre, String descripcion, BigDecimal precio,
                                      int stock, Integer idUnidad, Integer idEstadoProducto) {
        asegurarPropiedadTienda(idTienda, idVendedor);

        if (idProducto == null || idProducto <= 0) {
            throw new IllegalArgumentException("El producto seleccionado no es válido.");
        }
        Productos producto = productosRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        if (!idTienda.equals(producto.getIdTienda())) {
            throw new SecurityException("El producto no pertenece a la tienda seleccionada.");
        }
        validarCamposProducto(nombre, precio, stock, idUnidad, idEstadoProducto);

        producto.setNombre(nombre.trim());
        producto.setDescripcion(descripcion == null || descripcion.isBlank() ? null : descripcion.trim());
        producto.setPrecioUnitario(precio.setScale(2, RoundingMode.HALF_UP));
        producto.setStock(stock);
        producto.setIdTienda(idTienda);
        producto.setIdUnidad(idUnidad);
        producto.setIdEstadoProducto(idEstadoProducto);

        productosRepository.save(producto);
        return true;
    }

    private void validarCamposProducto(String nombre, BigDecimal precio, int stock,
                                       Integer idUnidad, Integer idEstadoProducto) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        if (idUnidad == null || !unidadesRepository.existsById(idUnidad)) {
            throw new IllegalArgumentException("La unidad seleccionada no existe.");
        }
        if (idEstadoProducto == null || !estadosProductoRepository.existsById(idEstadoProducto)) {
            throw new IllegalArgumentException("El estado seleccionado no existe.");
        }
    }

    private void asegurarPropiedadTienda(Integer idTienda, Integer idVendedor) {
        if (idTienda == null || idTienda <= 0 || idVendedor == null || idVendedor <= 0
                || tiendaRepository.findByIdTiendaAndIdVendedor(idTienda, idVendedor).isEmpty()) {
            throw new SecurityException("La tienda no pertenece al vendedor autenticado.");
        }
    }
}