package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoVendedorHistorialDTO {
    private int idPago;
    private int idSolicitud;
    private int idCliente;
    private String cliente;
    private String tienda;
    private BigDecimal montoPagado;
    private LocalDateTime fechaPago;
    private String numeroReferencia;
    private String observaciones;
    private String estadoCredito;
}
