package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeudaPendienteDTO {
    private int idSolicitud;
    private int idFactura;
    private String tienda;
    private BigDecimal montoOriginal;
    private BigDecimal saldoPendiente;
    private LocalDate fechaVencimiento;
    private String estado;
    private String tipoPrestamo;
}