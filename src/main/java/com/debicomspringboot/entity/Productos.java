package com.debicomspringboot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Getter
@Setter
public class Productos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "id_tienda")
    private Integer idTienda;

    @Column(name = "id_unidad")
    private Integer idUnidad;

    @Column(name = "id_estado_producto")
    private Integer idEstadoProducto;
}