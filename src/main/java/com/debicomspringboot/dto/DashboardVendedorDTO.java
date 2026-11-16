package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVendedorDTO {
    private long totalClientes;
    private long solicitudesNuevas;
    private long solicitudesPendientes;
    private long solicitudesAprobadas;
    private BigDecimal pagosRecibidos = BigDecimal.ZERO;
    private BigDecimal deudaPendiente = BigDecimal.ZERO;
}