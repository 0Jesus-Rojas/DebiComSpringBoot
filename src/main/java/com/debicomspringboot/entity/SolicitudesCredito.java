package com.debicomspringboot.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "solicitudes_credito")
@Getter
@Setter
public class SolicitudesCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "id_tienda")
    private Integer idTienda;

    @Column(name = "monto_total")
    private BigDecimal montoTotal;

    @Column(name = "saldo_pendiente")
    private BigDecimal saldoPendiente;

    @Column(name = "cupo_aprobado")
    private BigDecimal cupoAprobado;

    @Column(name = "id_tipo_prestamo")
    private Integer idTipoPrestamo;

    @Column(name = "id_estado_solicitud")
    private Integer idEstadoSolicitud;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "observaciones")
    private String observaciones;
}