package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCreditoDTO {
    private int idSolicitud;
    private int idCliente;
    private int idTienda;
    private String tienda;
    private String cliente;
    private BigDecimal montoTotal;
    private BigDecimal saldoPendiente;
    private BigDecimal cupoAprobado;
    private int idTipoPrestamo;
    private String tipoPrestamo;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaAprobacion;
    private LocalDateTime fechaVencimiento;
    private String observaciones;
}
