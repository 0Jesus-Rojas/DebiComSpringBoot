package com.debicomspringboot.repository;

import com.debicomspringboot.entity.RolesYPermisos;
import com.debicomspringboot.entity.RolesYPermisosId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolesYPermisosRepository extends JpaRepository<RolesYPermisos, RolesYPermisosId> {

    // Trae todos los registros asociados a un rol específico
    List<RolesYPermisos> findByIdIdRol(Integer idRol);

    // Elimina todas las asignaciones de un rol (útil si necesitas limpiar permisos)
    void deleteByIdIdRol(Integer idRol);
}