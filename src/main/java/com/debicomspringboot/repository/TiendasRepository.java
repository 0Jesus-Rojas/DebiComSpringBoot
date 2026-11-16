package com.debicomspringboot.repository;

import com.debicomspringboot.entity.Tiendas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TiendasRepository extends JpaRepository<Tiendas, Integer> {

    List<Tiendas> findAllByOrderByNombreTiendaAsc();
    boolean existsByIdTiendaAndIdVendedor(Integer idTienda, Integer idVendedor);
    List<Tiendas> findByIdVendedorOrderByNombreTiendaAsc(Integer idVendedor);
    Optional<Tiendas> findByIdTiendaAndIdVendedor(Integer idTienda, Integer idVendedor);
}