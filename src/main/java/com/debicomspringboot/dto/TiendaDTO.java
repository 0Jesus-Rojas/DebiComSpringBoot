package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiendaDTO {
    private int idTienda;
    private String nombreTienda;
    private String nit;
    private String direccion;
    private String telefono;
    private int idVendedor;
    private String vendedor;
    private BigDecimal ventasTotales = BigDecimal.ZERO;
    private long creditosOtorgados;
    private long clientesActivos;
    private long totalProductos;
}
