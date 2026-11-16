package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoHistorialDTO {
    private int idPago;
    private int idFactura;
    private String numeroFactura;
    private String numeroReferenciaPago;
    private BigDecimal montoPagado;
    private LocalDateTime fechaPago;
    private String tipoPago;
    private String origen;
    private String tienda;
    private String observaciones;
}
