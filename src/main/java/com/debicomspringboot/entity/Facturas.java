package com.debicomspringboot.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "facturas")
@Getter
@Setter
public class Facturas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Integer idFactura;

    @Column(name = "numero_factura")
    private String numeroFactura;

    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "id_tienda")
    private Integer idTienda;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "impuestos")
    private BigDecimal impuestos;

    @Column(name = "total")
    private BigDecimal total;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(name = "id_estado_factura")
    private Integer idEstadoFactura;
}