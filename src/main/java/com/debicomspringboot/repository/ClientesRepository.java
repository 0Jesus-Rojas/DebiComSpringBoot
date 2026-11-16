
package com.debicomspringboot.repository;

import com.debicomspringboot.entity.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientesRepository extends JpaRepository<Clientes, Integer> {

    Optional<Clientes> findByIdUsuario(Integer idUsuario);

    @Query("SELECT c FROM Clientes c JOIN Usuarios u ON u.idUsuario = c.idUsuario ORDER BY u.nombre ASC, u.apellido ASC")
    List<Clientes> listarOrdenadosPorNombre();
}
