package com.debicomspringboot.service;

import com.debicomspringboot.entity.Pagos;
import com.debicomspringboot.repository.PagosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagosRepository pagoRepository;

    public Pagos consultarPago(Integer idPago) {
        return pagoRepository.findById(idPago).orElse(null);
    }

    @Transactional
    public boolean insertarPago(Pagos miPago) {
        try {
            miPago.setIdPago(null);
            pagoRepository.save(miPago);
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean actualizarPago(Pagos miPago) {
        if (miPago.getIdPago() != null && pagoRepository.existsById(miPago.getIdPago())) {
            pagoRepository.save(miPago);
            return true;
        }
        System.out.println("No se encontró el pago");
        return false;
    }

    @Transactional
    public boolean eliminarPago(Integer idPago) {
        if (pagoRepository.existsById(idPago)) {
            pagoRepository.deleteById(idPago);
            return true;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean registrarPagoPresencial(int idSolicitud, int idVendedor, BigDecimal monto) {
        if (idSolicitud <= 0) {
            throw new IllegalArgumentException("El crédito no es válido.");
        }
        if (idVendedor <= 0) {
            throw new IllegalArgumentException("El vendedor no es válido.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }

        List<Object[]> filas = pagoRepository.bloquearCreditoParaPago(idSolicitud, idVendedor);
        if (filas.isEmpty()) {
            throw new IllegalArgumentException(
                    "El crédito no existe, no pertenece al vendedor o no tiene saldo pendiente.");
        }

        int idCliente = ((Number) filas.get(0)[0]).intValue();
        BigDecimal saldoActual = new BigDecimal(filas.get(0)[1].toString());

        if (monto.compareTo(saldoActual) > 0) {
            throw new IllegalArgumentException(
                    "El monto no puede superar el saldo pendiente del crédito ($" + saldoActual + ").");
        }

        Integer idPagado = pagoRepository.obtenerIdEstadoPagado();
        if (idPagado == null) {
            throw new IllegalStateException("No existe el estado PAGADO.");
        }

        int actualizados = pagoRepository.descontarSaldoCredito(monto, idPagado, idSolicitud);
        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No se pudo actualizar el saldo del crédito. El saldo pudo haber cambiado antes de registrar el pago.");
        }

        BigDecimal saldoTotalCliente = pagoRepository.calcularSaldoTotalCliente(idCliente);
        if (saldoTotalCliente == null) {
            saldoTotalCliente = BigDecimal.ZERO;
        }

        if (pagoRepository.actualizarCreditoCliente(saldoTotalCliente, idCliente) != 1) {
            throw new IllegalStateException("No se pudo sincronizar el crédito acumulado del comprador.");
        }

        // Registrar el historial dentro de la misma transacción.
        // Si el INSERT falla, también se revierten los cambios del saldo.
        String referencia = "PRES-CRED-" + idSolicitud + "-" + System.currentTimeMillis();

        pagoRepository.insertarHistorialPagoPresencial(
                idSolicitud,
                idCliente,
                idVendedor,
                monto,
                referencia,
                "Pago presencial aplicado al crédito #" + idSolicitud
        );

        return true;
    }
}