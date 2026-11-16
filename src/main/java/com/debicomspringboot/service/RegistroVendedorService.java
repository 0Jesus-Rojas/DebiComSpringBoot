package com.debicomspringboot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso relacionados con la conversión de comprador a vendedor.
 */
@Service
@RequiredArgsConstructor
public class RegistroVendedorService {

    private final UsuariosService usuarioService;

    @Transactional(readOnly = true)
    public boolean puedeRegistrarseComoVendedor(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            return false;
        }
        return usuarioService.puedeRegistrarseComoVendedor(idUsuario);
    }

    /**
     * @return id del rol Vendedor asignado; 0 si no se actualizó.
     */
    @Transactional
    public int registrarComoVendedor(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Usuario inválido.");
        }
        if (!usuarioService.puedeRegistrarseComoVendedor(idUsuario)) {
            return 0;
        }
        return usuarioService.registrarComoVendedor(idUsuario);
    }
}