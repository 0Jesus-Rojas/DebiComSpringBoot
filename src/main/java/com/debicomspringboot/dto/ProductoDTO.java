package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    private int idProducto;
    private String nombre;
    private String descripcion;
    private BigDecimal precioUnitario;
    private int stock;
    private int idTienda;
    private String unidad;
    private String estado;
    private int cantidad;
    private BigDecimal subtotal;
}
