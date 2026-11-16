package com.debicomspringboot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estados_factura")
@Getter
@Setter
public class EstadosFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_factura")
    private Integer idEstadoFactura;

    @Column(name = "nombre_estado")
    private String nombreEstado;
}