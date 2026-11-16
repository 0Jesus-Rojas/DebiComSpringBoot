package com.debicomspringboot.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pagos_presenciales")
@Getter
@Setter
public class PagosPresenciales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago_presencial")
    private Integer idPagoPresencial;

    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "id_vendedor")
    private Integer idVendedor;

    @Column(name = "monto_pagado")
    private BigDecimal montoPagado;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "numero_referencia")
    private String numeroReferencia;
    
    @Column(name = "observaciones")
    private String observaciones;
}