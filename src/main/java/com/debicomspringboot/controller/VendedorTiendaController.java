package com.debicomspringboot.controller;

import com.debicomspringboot.dto.TiendaDTO;
import com.debicomspringboot.service.TiendaService;
import com.debicomspringboot.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/vendedor/tienda")
@RequiredArgsConstructor
public class VendedorTiendaController {

    private final TiendaService tiendaService;

    // Reemplaza a doGet de TiendaServlet (/vendedor/tienda)
    @GetMapping
    public String verTienda(
            @RequestParam(name = "idTienda", defaultValue = "0") int idTienda,
            @RequestParam(name = "ok", required = false) String ok,
            HttpServletRequest request,
            Model model) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (vendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para administrar tiendas.");
        }

        try {
            cargarVista(model, vendedor, idTienda, ok);
            return "gestionar-tienda";
        } catch (RuntimeException e) {
            log.error("Error gestionando tiendas para el vendedor {}", vendedor, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible cargar las tiendas.", e);
        }
    }

    // Reemplaza a doPost de TiendaServlet (/vendedor/tienda)
    @PostMapping
    public String procesarTienda(
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "idTienda", defaultValue = "0") int idTienda,
            @RequestParam(name = "nombreTienda", required = false) String nombreTienda,
            @RequestParam(name = "nit", required = false) String nit,
            @RequestParam(name = "direccion", required = false) String direccion,
            @RequestParam(name = "telefono", required = false) String telefono,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (vendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para administrar tiendas.");
        }

        try {
            if ("crear".equalsIgnoreCase(action)) {
                int nuevaTiendaId = tiendaService.crearTienda(
                        vendedor, nombreTienda, nit, direccion, telefono);

                if (nuevaTiendaId <= 0) {
                    throw new IllegalStateException("No se generó el identificador de la tienda.");
                }

                redirectAttributes.addAttribute("idTienda", nuevaTiendaId);
                redirectAttributes.addAttribute("ok", "creada");
                return "redirect:/vendedor/tienda";
            }

            if (idTienda <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tienda inválida.");
            }

            tiendaService.actualizarTienda(
                    vendedor, idTienda, nombreTienda, nit, direccion, telefono);

            redirectAttributes.addAttribute("idTienda", idTienda);
            redirectAttributes.addAttribute("ok", "actualizada");
            return "redirect:/vendedor/tienda";

        } catch (ResponseStatusException e) {
            throw e;
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            cargarVista(model, vendedor, idTienda, null);
            return "gestionar-tienda";
        } catch (RuntimeException e) {
            log.error("Error procesando tienda para el vendedor {}", vendedor, e);
            model.addAttribute("error",
                    "No fue posible guardar la tienda. Verifica que el NIT no esté registrado.");
            cargarVista(model, vendedor, idTienda, null);
            return "gestionar-tienda";
        }
    }

    // Reemplaza al método privado cargarVista y el forward("/gestionar-tienda.html")
    private void cargarVista(Model model, int vendedor, int idTiendaParam, String ok) {
        List<TiendaDTO> tiendas = tiendaService.listarTiendas(vendedor);
        int idTienda = idTiendaParam;

        if (idTienda <= 0 && !tiendas.isEmpty()) {
            idTienda = tiendas.get(0).getIdTienda();
        }

        TiendaDTO tienda = idTienda > 0
                ? tiendaService.obtenerTienda(idTienda, vendedor)
                : null;

        model.addAttribute("tiendas", tiendas);
        model.addAttribute("tienda", tienda);
        model.addAttribute("idTiendaSeleccionada", idTienda);
        model.addAttribute("ok", ok);
    }
}