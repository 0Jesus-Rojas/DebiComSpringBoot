package com.debicomspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilDTO {
    private int idUsuario;
    private int idCliente;
    private String nombre;
    private String apellido;
    private String identificacion;
    private String tipoIdentificacion;
    private Date fechaNacimiento;
    private String correo;
    private String telefono;
    private String direccion;
    private Byte autorizaDatos;
    private String rol;
}
