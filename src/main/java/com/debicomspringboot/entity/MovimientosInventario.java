package com.debicomspringboot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movimientos_inventario")
@Getter
@Setter
public class MovimientosInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer idMovimiento;

    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "id_tipo_movimiento")
    private Integer idTipoMovimiento;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;
}