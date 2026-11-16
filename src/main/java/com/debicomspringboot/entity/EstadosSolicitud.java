package com.debicomspringboot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estados_solicitud")
@Getter
@Setter
public class EstadosSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_solicitud")
    private Integer idEstadoSolicitud;

    @Column(name = "nombre_estado")
    private String nombreEstado;
}