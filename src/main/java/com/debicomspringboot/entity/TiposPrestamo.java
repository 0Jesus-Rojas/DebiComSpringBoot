package com.debicomspringboot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tipos_prestamo")
@Getter
@Setter
public class TiposPrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_prestamo")
    private Integer idTipoPrestamo;

    @Column(name = "nombre_tipo")
    private String nombreTipo;
}