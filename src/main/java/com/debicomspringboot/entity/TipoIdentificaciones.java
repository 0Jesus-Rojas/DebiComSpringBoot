package com.debicomspringboot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tipo_identificaciones")
@Getter
@Setter
public class TipoIdentificaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_identificacion")
    private Integer idTipoIdentificacion;

    @Column(name = "nombre_tipo")
    private String nombreTipo;
}