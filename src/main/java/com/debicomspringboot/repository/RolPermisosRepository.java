package com.debicomspringboot.repository;

import com.debicomspringboot.entity.RolPermisos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolPermisosRepository extends JpaRepository<RolPermisos, Integer> {
}