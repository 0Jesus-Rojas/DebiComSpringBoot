package com.debicomspringboot.controller;

import com.debicomspringboot.dto.TiendaDTO;
import org.springframework.ui.Model;
import com.debicomspringboot.dto.ItemCreditoDTO;
import com.debicomspringboot.service.VendedorService;
import com.debicomspringboot.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/vendedor/creditos")
@RequiredArgsConstructor
public class VendedorCreditoController {

    private final VendedorService vendedorService;

    // Reemplaza a doPost de ActualizarEstadoCreditoServlet (/vendedor/creditos/estado)
    @PostMapping("/estado")
    public String actualizarEstadoCredito(
            @RequestParam(name = "idSolicitud", defaultValue = "0") int idSolicitud,
            @RequestParam(name = "estado", required = false) String estado,
            HttpServletRequest request) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para acceder a la administración.");
        }
        if (vendedor == null) {
            return "redirect:/";
        }

        if (idSolicitud <= 0 || estado == null || estado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parámetros inválidos.");
        }

        try {
            boolean ok = vendedorService.actualizarEstadoCredito(idSolicitud, vendedor, estado);
            if (!ok) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La solicitud no existe, no pertenece a la tienda o ya fue procesada.");
            }
            if ("APROBADO".equalsIgnoreCase(estado.trim())) {
                return "redirect:/vendedor/historial-creditos";
            }

            return "redirect:/vendedor/creditos";
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error actualizando crédito {}", idSolicitud, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible actualizar el crédito.", e);
        }
    }

    // Reemplaza a doPut del servlet original
    @PutMapping("/estado")
    public String actualizarEstadoCreditoPut(
            @RequestParam(name = "idSolicitud", defaultValue = "0") int idSolicitud,
            @RequestParam(name = "estado", required = false) String estado,
            HttpServletRequest request) {
        return actualizarEstadoCredito(idSolicitud, estado, request);
    }
    // Reemplaza a doPost de ConsumirCreditoServlet (/vendedor/creditos/consumir)
    @PostMapping("/consumir")
    public String consumirCredito(
            @RequestParam(name = "idSolicitud", defaultValue = "0") int idSolicitud,
            @RequestParam(name = "idProducto", required = false) String[] idsProductos,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (vendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para administrar créditos.");
        }

        try {
            if (idSolicitud <= 0) {
                throw new IllegalArgumentException("El crédito seleccionado no es válido.");
            }

            List<ItemCreditoDTO> items = new ArrayList<>();
            if (idsProductos != null) {
                for (String id : idsProductos) {
                    int idProducto = parseInt(id);
                    int cantidad = parseInt(request.getParameter("cantidad_" + id));
                    if (idProducto > 0 && cantidad > 0) {
                        items.add(new ItemCreditoDTO(idProducto, cantidad));
                    }
                }
            }

            vendedorService.consumirCredito(vendedor, idSolicitud, items);

            redirectAttributes.addAttribute("id", idSolicitud);
            redirectAttributes.addAttribute("ok", "productos-agregados");
            return "redirect:/vendedor/creditos/detalle";

        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addAttribute("id", idSolicitud);
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/vendedor/creditos/detalle";

        } catch (RuntimeException e) {
            log.error("Error agregando productos al crédito {}", idSolicitud, e);
            redirectAttributes.addAttribute("id", idSolicitud);
            redirectAttributes.addAttribute("error", "No fue posible agregar los productos al crédito.");
            return "redirect:/vendedor/creditos/detalle";
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
    // Reemplaza a doGet de DetalleCreditoServlet (/vendedor/creditos/detalle)
    @GetMapping("/detalle")
    public String verDetalleCredito(
            @RequestParam(name = "id", defaultValue = "0") int id,
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
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de solicitud inválido.");
        }

        try {
            var detalle = vendedorService.obtenerDetalleCredito(id, vendedor);
            if (detalle == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Préstamo no encontrado.");
            }

            model.addAttribute("detalleCredito", detalle);
            model.addAttribute("productosCredito",
                    vendedorService.listarProductosParaCredito(detalle.getSolicitud().getIdTienda(), vendedor));
            model.addAttribute("ok", ok);
            model.addAttribute("error", error);
            return "estado-credito";
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error consultando detalle del crédito {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible consultar el detalle.", e);
        }
    }
    // Reemplaza a doGet de OtorgarCreditoServlet (/vendedor/creditos/otorgar)
    @GetMapping("/otorgar")
    public String mostrarFormularioOtorgar(
            @RequestParam(name = "idTienda", defaultValue = "0") int idTienda,
            @RequestParam(name = "error", required = false) String error,
            HttpServletRequest request,
            Model model) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (vendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para otorgar créditos.");
        }

        return cargarVistaOtorgar(model, vendedor, idTienda, error);
    }

    // Reemplaza a doPost de OtorgarCreditoServlet (/vendedor/creditos/otorgar)
    @PostMapping("/otorgar")
    public String otorgarCredito(
            @RequestParam(name = "idTienda", defaultValue = "0") int idTienda,
            @RequestParam(name = "idCliente", defaultValue = "0") int idCliente,
            @RequestParam(name = "cupo", required = false) String cupoStr,
            @RequestParam(name = "fechaVencimiento", required = false) String fechaVencimientoStr,
            @RequestParam(name = "observaciones", required = false) String observaciones,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (vendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para otorgar créditos.");
        }

        try {
            if (cupoStr == null || cupoStr.isBlank() || fechaVencimientoStr == null || fechaVencimientoStr.isBlank()) {
                return cargarVistaOtorgar(model, vendedor, idTienda,
                        "El comprador, cupo y fecha deben ser válidos.");
            }

            BigDecimal cupo = new BigDecimal(cupoStr.trim());
            LocalDate vencimiento = LocalDate.parse(fechaVencimientoStr.trim());

            LocalDate hoy = LocalDate.now();
            if (vencimiento.isBefore(hoy.plusDays(15)) || vencimiento.isAfter(hoy.plusMonths(1))) {
                throw new IllegalArgumentException("El plazo del crédito debe estar entre 15 días y 1 mes.");
            }
            if (cupo.signum() <= 0) {
                throw new IllegalArgumentException("El cupo debe ser mayor que cero.");
            }

            int idSolicitud = vendedorService.asignarCreditoCupo(
                    vendedor, idTienda, idCliente, cupo, vencimiento, observaciones);

            redirectAttributes.addAttribute("id", idSolicitud);
            redirectAttributes.addAttribute("ok", "credito-creado");
            return "redirect:/vendedor/creditos/detalle";

        } catch (DateTimeParseException | NumberFormatException e) {
            return cargarVistaOtorgar(model, vendedor, idTienda,
                    "El comprador, cupo y fecha deben ser válidos.");
        } catch (SecurityException | IllegalArgumentException e) {
            return cargarVistaOtorgar(model, vendedor, idTienda, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error asignando cupo de crédito", e);
            return cargarVistaOtorgar(model, vendedor, idTienda,
                    "No fue posible asignar el cupo de crédito.");
        }
    }

    // Reemplaza al método privado cargarVista y el forward("/otorgar-credito.html")
    private String cargarVistaOtorgar(Model model, int vendedor, int idTiendaParam, String error) {
        List<TiendaDTO> tiendas = vendedorService.listarTiendasDelVendedor(vendedor);
        int idTienda = idTiendaParam;
        if (idTienda <= 0 && !tiendas.isEmpty()) {
            idTienda = tiendas.get(0).getIdTienda();
        }

        model.addAttribute("tiendas", tiendas);
        model.addAttribute("compradores", vendedorService.listarCompradores());
        model.addAttribute("idTienda", idTienda);
        model.addAttribute("error", error);
        return "otorgar-credito";
    }
    // Reemplaza a doGet de SolicitudesCreditoVendedorServlet (/vendedor/creditos)
    @GetMapping
    public String listarSolicitudesPendientes(HttpServletRequest request, Model model) {
        Integer idVendedor = AuthUtil.getIdUsuario(request);

        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para acceder a la administración.");
        }
        if (idVendedor == null) {
            return "redirect:/";
        }

        try {
            model.addAttribute("solicitudes",
                    vendedorService.listarSolicitudesPendientes(idVendedor));
            return "aprobar-creditos";
        } catch (RuntimeException e) {
            log.error("Error listando solicitudes pendientes para el vendedor {}", idVendedor, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible cargar las solicitudes pendientes.", e);
        }
    }
}