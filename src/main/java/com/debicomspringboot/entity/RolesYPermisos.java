package com.debicomspringboot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles_y_permisos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolesYPermisos {

    @EmbeddedId
    private RolesYPermisosId id;

    @ManyToOne
    @MapsId("idRol")
    @JoinColumn(name = "id_rol")
    private Roles rol; // Si tu entidad se llama Rol, cambia Roles por Rol

    @ManyToOne
    @MapsId("idRolPermiso")
    @JoinColumn(name = "id_rol_permiso")
    private RolPermisos rolPermiso; // Si tu entidad se llama RolPermiso, cámbialo aquí
}