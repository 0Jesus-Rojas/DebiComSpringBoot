package com.debicomspringboot.repository;

import com.debicomspringboot.entity.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuariosRepository extends JpaRepository<Usuarios, Integer> {

    @Query("SELECT u FROM Usuarios u WHERE LOWER(TRIM(u.correo)) = LOWER(TRIM(:correo))")
    Optional<Usuarios> buscarPorCorreo(@Param("correo") String correo);

    @Query("SELECT COUNT(u) > 0 FROM Usuarios u WHERE LOWER(TRIM(u.correo)) = LOWER(TRIM(:correo))")
    boolean existeCorreo(@Param("correo") String correo);

    @Query("SELECT COUNT(u) > 0 FROM Usuarios u WHERE TRIM(u.identificacion) = TRIM(:identificacion)")
    boolean existeIdentificacion(@Param("identificacion") String identificacion);

    @Query(value = "SELECT r.nombre_rol FROM usuarios u LEFT JOIN roles r ON r.id_rol = u.id_rol WHERE u.id_usuario = :idUsuario", nativeQuery = true)
    String obtenerNombreRolPorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT id_rol FROM roles WHERE UPPER(nombre_rol) = UPPER(:nombreRol) LIMIT 1", nativeQuery = true)
    Integer obtenerIdRolPorNombre(@Param("nombreRol") String nombreRol);

    @Query(value = """
            SELECT u.id_usuario AS idUsuario,
                   COALESCE(c.id_cliente, 0) AS idCliente,
                   u.nombre AS nombre,
                   u.apellido AS apellido,
                   u.identificacion AS identificacion,
                   ti.nombre_tipo AS tipoIdentificacion,
                   u.fecha_nacimiento AS fechaNacimiento,
                   u.correo AS correo,
                   u.telefono AS telefono,
                   u.direccion AS direccion,
                   u.autoriza_datos AS autorizaDatos,
                   r.nombre_rol AS rol
            FROM usuarios u
            LEFT JOIN clientes c ON c.id_usuario = u.id_usuario
            INNER JOIN tipo_identificaciones ti ON ti.id_tipo_identificacion = u.id_tipo_identificacion
            LEFT JOIN roles r ON r.id_rol = u.id_rol
            WHERE u.id_usuario = :idUsuario
            """, nativeQuery = true)
    Object[] consultarPerfilCrudo(@Param("idUsuario") Integer idUsuario);
}