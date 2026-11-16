package com.debicomspringboot.repository;

import com.debicomspringboot.entity.DetalleFacturas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleFacturasRepository extends JpaRepository<DetalleFacturas, Integer> {

    // Reemplaza: SELECT ... FROM detalle_facturas WHERE id_factura = ? ORDER BY id_detalle_factura
    List<DetalleFacturas> findByIdFacturaOrderByIdDetalleFacturaAsc(Integer idFactura);
}