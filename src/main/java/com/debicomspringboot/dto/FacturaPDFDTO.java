package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Datos de una factura directa, preparados para la generación del PDF. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaPDFDTO {
    private int idFactura;
    private String numeroFactura;
    private String comprador;
    private String identificacionComprador;
    private String tienda;
    private String nitTienda;
    private String direccionTienda;
    private String metodoPago;
    private String referenciaPago;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal impuestos = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private LocalDateTime fechaEmision;
    private final List<Item> items = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String nombre;
        private int cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}