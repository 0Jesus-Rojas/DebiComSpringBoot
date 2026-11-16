package com.debicomspringboot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estados_producto")
@Getter
@Setter
public class EstadosProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_producto")
    private Integer idEstadoProducto;

    @Column(name = "nombre_estado")
    private String nombreEstado;
}