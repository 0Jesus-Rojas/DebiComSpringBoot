package com.debicomspringboot.service;

import com.debicomspringboot.entity.Roles;
import com.debicomspringboot.repository.RolesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolesRepository rolRepository;

    public Roles consultarRol(Integer idRol) {
        return rolRepository.findById(idRol).orElse(null);
    }

    @Transactional
    public boolean insertarRol(Roles miRol) {
        try {
            miRol.setIdRol(null);
            rolRepository.save(miRol);
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean actualizarRol(Roles miRol) {
        if (miRol.getIdRol() != null && rolRepository.existsById(miRol.getIdRol())) {
            rolRepository.save(miRol);
            return true;
        }
        System.out.println("No se encontró el rol");
        return false;
    }

    @Transactional
    public boolean eliminarRol(Integer idRol) {
        if (rolRepository.existsById(idRol)) {
            rolRepository.deleteById(idRol);
            return true;
        }
        return false;
    }
}