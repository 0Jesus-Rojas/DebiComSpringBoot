package com.debicomspringboot.controller;

import com.debicomspringboot.dto.ItemCreditoDTO;
import com.debicomspringboot.dto.ProductoDTO;
import com.debicomspringboot.dto.TiendaDTO;
import com.debicomspringboot.service.FiadoService;
import com.debicomspringboot.service.VendedorService;
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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/vendedor/fiados")
@RequiredArgsConstructor
public class VendedorFiadoController {

    private final VendedorService vendedorService;
    private final FiadoService fiadoService;

    // Reemplaza a doGet de AsignarFiadoServlet (/vendedor/fiados/asignar)
    @GetMapping("/asignar")
    public String mostrarFormularioAsignar(
            @RequestParam(name = "idTienda", defaultValue = "0") int idTienda,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "ok", required = false) String ok,
            @RequestParam(name = "id", required = false) String idFiado,
            HttpServletRequest request,
            Model model) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (vendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para administrar fiados.");
        }

        return cargarVista(model, vendedor, idTienda, error, ok, idFiado);
    }

    // Reemplaza a doPost de AsignarFiadoServlet (/vendedor/fiados/asignar)
    @PostMapping("/asignar")
    public String asignarFiado(
            @RequestParam(name = "idTienda", defaultValue = "0") int idTienda,
            @RequestParam(name = "idCliente", defaultValue = "0") int idCliente,
            @RequestParam(name = "fechaVencimiento", required = false) String fechaVencimientoStr,
            @RequestParam(name = "observaciones", required = false) String observaciones,
            @RequestParam(name = "idProducto", required = false) String[] idsProductos,
            HttpServletRequest request,
            Model model) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (vendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para administrar fiados.");
        }

        try {
            if (fechaVencimientoStr == null || fechaVencimientoStr.isBlank()) {
                return cargarVista(model, vendedor, idTienda,
                        "El comprador, fecha y cantidades deben ser válidos.", null, null);
            }

            LocalDate fecha = LocalDate.parse(fechaVencimientoStr.trim());
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

            int idFiado = fiadoService.asignarFiado(vendedor, idTienda, idCliente, fecha, observaciones, items);
            return "redirect:/vendedor/fiados/asignar?ok=fiado-creado&id=" + idFiado;

        } catch (DateTimeParseException | NumberFormatException e) {
            return cargarVista(model, vendedor, idTienda,
                    "El comprador, fecha y cantidades deben ser válidos.", null, null);
        } catch (SecurityException | IllegalArgumentException e) {
            return cargarVista(model, vendedor, idTienda, e.getMessage(), null, null);
        } catch (RuntimeException e) {
            log.error("Error asignando fiado", e);
            return cargarVista(model, vendedor, idTienda, "No fue posible asignar el fiado.", null, null);
        }
    }

    // Reemplaza al método privado cargarVista y el forward("/asignar-fiado.html")
    private String cargarVista(Model model, int vendedor, int idTiendaParam,
                               String error, String ok, String idFiado) {
        List<TiendaDTO> tiendas = vendedorService.listarTiendasDelVendedor(vendedor);
        int idTienda = idTiendaParam;
        if (idTienda <= 0 && !tiendas.isEmpty()) {
            idTienda = tiendas.get(0).getIdTienda();
        }

        model.addAttribute("tiendas", tiendas);
        model.addAttribute("compradores", vendedorService.listarCompradores());
        model.addAttribute("productosFiado", idTienda > 0
                ? fiadoService.listarProductos(idTienda, vendedor)
                : List.<ProductoDTO>of());
        model.addAttribute("fiadosPendientes", fiadoService.listarFiadosPendientes(vendedor));
        model.addAttribute("idTienda", idTienda);
        model.addAttribute("error", error);
        model.addAttribute("ok", ok);
        model.addAttribute("idFiado", idFiado);

        return "asignar-fiado";
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
}