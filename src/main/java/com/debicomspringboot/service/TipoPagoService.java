package com.debicomspringboot.service;

import com.debicomspringboot.entity.TipoPago;
import com.debicomspringboot.repository.TipoPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TipoPagoService {

    private final TipoPagoRepository tipoPagoRepository;

    // 1. Consulta un tipo de pago por su ID
    @Transactional(readOnly = true)
    public Optional<TipoPago> consultarTipoPago(Integer idTipoPago) {
        return tipoPagoRepository.findById(idTipoPago);
    }

    // 2. Lista todos los tipos de pago registrados
    @Transactional(readOnly = true)
    public List<TipoPago> listarTiposPago() {
        return tipoPagoRepository.findAllByOrderByNombrePagoAsc();
    }

    // 3. Lista los métodos de pago habilitados para venta directa (excluye 'PRESENCIAL')
    @Transactional(readOnly = true)
    public List<TipoPago> listarTiposPagoNoPresenciales() {
        return tipoPagoRepository.listarNoPresenciales();
    }

    // 4. Registra un nuevo tipo de pago
    @Transactional
    public TipoPago insertarTipoPago(TipoPago tipoPago) {
        if (tipoPago == null || tipoPago.getNombrePago() == null || tipoPago.getNombrePago().isBlank()) {
            throw new IllegalArgumentException("El nombre del tipo de pago es obligatorio.");
        }
        tipoPago.setIdTipoPago(null); // Fuerza INSERT con AUTO_INCREMENT en MySQL
        tipoPago.setNombrePago(tipoPago.getNombrePago().trim());
        return tipoPagoRepository.save(tipoPago);
    }

    // 5. Actualiza el nombre de un tipo de pago existente
    @Transactional
    public boolean actualizarTipoPago(TipoPago tipoPago) {
        if (tipoPago == null || tipoPago.getIdTipoPago() == null) {
            return false;
        }

        return tipoPagoRepository.findById(tipoPago.getIdTipoPago())
                .map(actual -> {
                    actual.setNombrePago(tipoPago.getNombrePago().trim());
                    tipoPagoRepository.save(actual);
                    return true;
                })
                .orElse(false);
    }

    // 6. Elimina un tipo de pago por su ID
    @Transactional
    public boolean eliminarTipoPago(Integer idTipoPago) {
        if (idTipoPago != null && tipoPagoRepository.existsById(idTipoPago)) {
            tipoPagoRepository.deleteById(idTipoPago);
            return true;
        }
        return false;
    }
}