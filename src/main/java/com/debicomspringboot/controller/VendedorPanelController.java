package com.debicomspringboot.controller;

import com.debicomspringboot.dto.DashboardVendedorDTO;
import com.debicomspringboot.dto.PagoVendedorHistorialDTO;
import com.debicomspringboot.dto.SolicitudCreditoDTO;
import com.debicomspringboot.service.VendedorService;
import com.debicomspringboot.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/vendedor")
@RequiredArgsConstructor
public class VendedorPanelController {

    private final VendedorService vendedorService;

    // Reemplaza a doGet de CreditosPendientesServlet (/vendedor/creditos-pendientes)
    @GetMapping("/creditos-pendientes")
    public String verCreditosPendientes(
            @RequestParam(name = "ok", required = false) String ok,
            @RequestParam(name = "error", required = false) String error,
            HttpServletRequest request,
            Model model) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para acceder a la administración.");
        }
        if (vendedor == null) {
            return "redirect:/";
        }

        try {
            model.addAttribute("creditosPendientes", vendedorService.obtenerCreditosPendientes(vendedor));
            model.addAttribute("ok", ok);
            model.addAttribute("error", error);
            return "creditos-pendientes";
        } catch (RuntimeException e) {
            log.error("Error consultando créditos pendientes para el vendedor {}", vendedor, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible consultar los créditos pendientes.", e);
        }
    }
    // Reemplaza a doGet de DashboardVendedorServlet (/vendedor/dashboard)

    @GetMapping("/dashboard")
    public String verDashboard(HttpServletRequest request, Model model) {

        Integer idVendedor = AuthUtil.getIdUsuario(request);

        if (idVendedor == null) {
            return "redirect:/";
        }

        try {

            log.info("[PANEL] Iniciando carga del panel del vendedor {}", idVendedor);

            DashboardVendedorDTO dto =
                    vendedorService.obtenerDashboard(idVendedor);

            model.addAttribute("dashboard", dto);

            log.info("[PANEL] Dashboard cargado. Consultando solicitudes...");

            model.addAttribute(
                    "solicitudesNuevas",
                    vendedorService.listarSolicitudes(idVendedor, "PENDIENTE")
            );

            log.info("[PANEL] Solicitudes cargadas. Consultando tiendas...");

            model.addAttribute(
                    "tiendas",
                    vendedorService.listarTiendasDelVendedor(idVendedor)
            );

            log.info("[PANEL] Información preparada. Renderizando panel.");

            return "panel";

        } catch (RuntimeException e) {

            log.error(
                    "Error consultando dashboard para el vendedor {}",
                    idVendedor,
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible cargar el dashboard.",
                    e
            );
        }
    }

    // Reemplaza a doGet de HistorialCreditosVendedorServlet (/vendedor/historial-creditos)
    @GetMapping("/historial-creditos")
    public String verHistorialCreditos(HttpServletRequest request, Model model) {
        Integer idVendedor = AuthUtil.getIdUsuario(request);
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para acceder a la administración.");
        }
        if (idVendedor == null) {
            return "redirect:/";
        }

        try {
            List<SolicitudCreditoDTO> historial = vendedorService.listarHistorialCreditos(idVendedor);
            model.addAttribute("historialCreditos", historial);
            return "historial-creditos";
        } catch (RuntimeException e) {
            log.error("Error consultando historial de créditos para el vendedor {}", idVendedor, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible consultar el historial de créditos.", e);
        }
    }
    // Reemplaza a doGet de HistorialPagosVendedorServlet (/vendedor/historial-pagos)
    @GetMapping("/historial-pagos")
    public String verHistorialPagosVendedor(HttpServletRequest request, Model model) {
        Integer idVendedor = AuthUtil.getIdUsuario(request);
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para acceder a la administración.");
        }
        if (idVendedor == null) {
            return "redirect:/";
        }

        try {
            List<PagoVendedorHistorialDTO> pagos = vendedorService.listarHistorialPagos(idVendedor);

            BigDecimal total = pagos.stream()
                    .map(PagoVendedorHistorialDTO::getMontoPagado)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("pagosVendedor", pagos);
            model.addAttribute("totalPagado", total);

            return "historial-pagos-vendedor";
        } catch (RuntimeException e) {
            log.error("Error consultando historial de pagos del vendedor {}", idVendedor, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible consultar el historial de pagos.", e);
        }
    }
}