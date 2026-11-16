package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompradorDTO {
    private int idCliente;
    private String nombreCompleto;
    private String identificacion;
}