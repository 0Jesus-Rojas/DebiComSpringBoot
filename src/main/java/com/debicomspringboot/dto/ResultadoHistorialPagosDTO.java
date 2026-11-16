package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoHistorialPagosDTO {
    private BigDecimal totalPagado = BigDecimal.ZERO;
    private List<PagoHistorialDTO> pagos = new ArrayList<>();
}
