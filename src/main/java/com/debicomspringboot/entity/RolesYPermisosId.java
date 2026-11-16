package com.debicomspringboot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RolesYPermisosId implements Serializable {

    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "id_rol_permiso")
    private Integer idRolPermiso;
}