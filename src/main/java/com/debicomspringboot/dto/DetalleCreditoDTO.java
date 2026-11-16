package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCreditoDTO {
    private SolicitudCreditoDTO solicitud;
    private List<ProductoDTO> productos = new ArrayList<>();
}