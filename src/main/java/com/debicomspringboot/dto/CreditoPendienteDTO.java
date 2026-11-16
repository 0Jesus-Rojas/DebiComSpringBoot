package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditoPendienteDTO {
    private int idSolicitud;
    private int idCliente;
    private String cliente;
    private int idTienda;
    private String tienda;
    private BigDecimal montoOriginal;
    private BigDecimal saldoPendiente;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDate fechaVencimiento;
    private long diasMora;
}