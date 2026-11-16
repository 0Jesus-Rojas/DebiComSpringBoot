package com.debicomspringboot.service;

import com.debicomspringboot.entity.RolPermisos;
import com.debicomspringboot.repository.RolPermisosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RolPermisoService {

    private final RolPermisosRepository rolPermisoRepository;

    public RolPermisos consultarRolPermiso(Integer idRolPermiso) {
        return rolPermisoRepository.findById(idRolPermiso).orElse(null);
    }

    @Transactional
    public boolean insertarRolPermiso(RolPermisos miRolPermiso) {
        try {
            miRolPermiso.setIdRolPermiso(null);
            rolPermisoRepository.save(miRolPermiso);
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean actualizarRolPermiso(RolPermisos miRolPermiso) {
        if (miRolPermiso.getIdRolPermiso() != null && rolPermisoRepository.existsById(miRolPermiso.getIdRolPermiso())) {
            rolPermisoRepository.save(miRolPermiso);
            return true;
        }
        System.out.println("No se encontró el rol permiso");
        return false;
    }

    @Transactional
    public boolean eliminarRolPermiso(Integer idRolPermiso) {
        if (rolPermisoRepository.existsById(idRolPermiso)) {
            rolPermisoRepository.deleteById(idRolPermiso);
            return true;
        }
        return false;
    }
}