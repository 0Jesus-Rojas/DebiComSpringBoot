package com.debicomspringboot.service;

import com.debicomspringboot.entity.RolPermisos;
import com.debicomspringboot.entity.Roles;
import com.debicomspringboot.entity.RolesYPermisos;
import com.debicomspringboot.entity.RolesYPermisosId;
import com.debicomspringboot.repository.RolPermisosRepository;
import com.debicomspringboot.repository.RolesRepository;
import com.debicomspringboot.repository.RolesYPermisosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RolesYPermisosService {

    private final RolesYPermisosRepository rolesYPermisosRepository;
    private final RolesRepository rolesRepository;
    private final RolPermisosRepository rolPermisosRepository;

    // 1. Reemplaza a consultarRolYPermiso(int idRol, int idRolPermiso)
    @Transactional(readOnly = true)
    public Optional<RolesYPermisos> consultarRolYPermiso(Integer idRol, Integer idRolPermiso) {
        RolesYPermisosId id = new RolesYPermisosId(idRol, idRolPermiso);
        return rolesYPermisosRepository.findById(id);
    }

    // Método de apoyo para listar todos los permisos de un rol en las vistas Thymeleaf
    @Transactional(readOnly = true)
    public List<RolesYPermisos> listarPorRol(Integer idRol) {
        return rolesYPermisosRepository.findByIdIdRol(idRol);
    }

    // 2. Reemplaza a insertarRolYPermiso(RolesYPermisos miRolYPermiso)
    @Transactional
    public RolesYPermisos insertarRolYPermiso(Integer idRol, Integer idRolPermiso) {
        RolesYPermisosId id = new RolesYPermisosId(idRol, idRolPermiso);

        if (rolesYPermisosRepository.existsById(id)) {
            throw new IllegalArgumentException("Este permiso ya está asignado al rol.");
        }

        Roles rol = rolesRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el rol con ID: " + idRol));

        RolPermisos permiso = rolPermisosRepository.findById(idRolPermiso)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el permiso con ID: " + idRolPermiso));

        RolesYPermisos nuevaAsignacion = new RolesYPermisos();
        nuevaAsignacion.setId(id);
        nuevaAsignacion.setRol(rol);
        nuevaAsignacion.setRolPermiso(permiso);

        return rolesYPermisosRepository.save(nuevaAsignacion);
    }

    // 3. Reemplaza a actualizarRolYPermiso(RolesYPermisos miRolYPermiso)
    @Transactional
    public RolesYPermisos actualizarRolYPermiso(Integer idRol, Integer idPermisoAnterior, Integer idPermisoNuevo) {
        RolesYPermisosId idAnterior = new RolesYPermisosId(idRol, idPermisoAnterior);

        if (!rolesYPermisosRepository.existsById(idAnterior)) {
            throw new IllegalArgumentException("No se encontró el registro original para actualizar.");
        }

        // Tal como indicaba la nota de tu DAO, en una PK compuesta se maneja por borrado e inserción
        rolesYPermisosRepository.deleteById(idAnterior);
        return insertarRolYPermiso(idRol, idPermisoNuevo);
    }

    // 4. Reemplaza a eliminarRolYPermiso(int idRol, int idRolPermiso)
    @Transactional
    public boolean eliminarRolYPermiso(Integer idRol, Integer idRolPermiso) {
        RolesYPermisosId id = new RolesYPermisosId(idRol, idRolPermiso);

        if (rolesYPermisosRepository.existsById(id)) {
            rolesYPermisosRepository.deleteById(id);
            return true;
        }
        return false;
    }
}