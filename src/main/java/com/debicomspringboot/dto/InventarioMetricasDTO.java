package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioMetricasDTO {
    private long productosActivos;
    private long productosDisponibles;
    private long productosBajoStock;
    private long productosAgotados;
}
