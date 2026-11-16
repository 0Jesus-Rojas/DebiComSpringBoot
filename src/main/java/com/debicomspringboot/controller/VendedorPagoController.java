package com.debicomspringboot.controller;

import com.debicomspringboot.service.PagoService;
import com.debicomspringboot.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Slf4j
@Controller
@RequestMapping("/vendedor/pagos")
@RequiredArgsConstructor
public class VendedorPagoController {

    private final PagoService pagoService;

    // Reemplaza a doPost de PagoPresencialServlet (/vendedor/pagos/registrar)
    @PostMapping("/registrar")
    public String registrarPagoPresencial(
            @RequestParam(name = "idSolicitud", defaultValue = "0") int idSolicitud,
            @RequestParam(name = "monto", required = false) String montoStr,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer idVendedor = AuthUtil.getIdUsuario(request);
        if (idVendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para registrar pagos.");
        }

        BigDecimal monto;
        try {
            if (montoStr == null || montoStr.isBlank()) {
                throw new IllegalArgumentException("Monto vacío");
            }
            monto = new BigDecimal(montoStr.trim());
        } catch (Exception e) {
            return redirigirError(redirectAttributes, idSolicitud, "El monto ingresado no es válido.");
        }

        try {
            pagoService.registrarPagoPresencial(idSolicitud, idVendedor, monto);

            redirectAttributes.addAttribute("id", idSolicitud);
            redirectAttributes.addAttribute("ok", "pago");
            return "redirect:/vendedor/creditos/detalle";
        } catch (IllegalArgumentException e) {
            return redirigirError(redirectAttributes, idSolicitud, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error registrando pago presencial para solicitud {}", idSolicitud, e);
            return redirigirError(redirectAttributes, idSolicitud, "No fue posible registrar el pago presencial.");
        }
    }

    // Reemplaza a redirigirError con URLEncoder.encode(...)
    private String redirigirError(RedirectAttributes redirectAttributes, int idSolicitud, String mensaje) {
        redirectAttributes.addAttribute("id", idSolicitud);
        redirectAttributes.addAttribute("error", mensaje == null ? "Error desconocido" : mensaje);
        return "redirect:/vendedor/creditos/detalle";
    }
}