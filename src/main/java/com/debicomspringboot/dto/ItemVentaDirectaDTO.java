package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Producto y cantidad solicitados para una venta directa de contado. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemVentaDirectaDTO {
    private int idProducto;
    private int cantidad;
}