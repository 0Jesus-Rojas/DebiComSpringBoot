package com.debicomspringboot.controller;

import com.debicomspringboot.dto.TiendaDTO;
import com.debicomspringboot.entity.Productos;
import com.debicomspringboot.repository.ProductosRepository;
import com.debicomspringboot.service.InventarioService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/vendedor/inventario")
@RequiredArgsConstructor
public class VendedorInventarioController {

    private final InventarioService inventarioService;
    private final ProductosRepository productosRepository;

    // Reemplaza a doGet de InventarioServlet (/vendedor/inventario)
    @GetMapping
    public String verInventario(
            @RequestParam(name = "idTienda", defaultValue = "0") int idTienda,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "editarId", defaultValue = "0") int editarId,
            @RequestParam(name = "ok", required = false) String ok,
            HttpServletRequest request,
            Model model) {

        return mostrarVista(model, request, idTienda, search, editarId, ok, null);
    }

    // Reemplaza a doPost de InventarioServlet (/vendedor/inventario)
    @PostMapping
    public String procesarProducto(
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "idTienda", defaultValue = "0") int idTienda,
            @RequestParam(name = "idProducto", defaultValue = "0") int idProducto,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "precio", required = false) String precioStr,
            @RequestParam(name = "stock", required = false) String stockStr,
            @RequestParam(name = "idUnidad", required = false) String idUnidadStr,
            @RequestParam(name = "idEstadoProducto", required = false) String idEstadoStr,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (vendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para administrar inventario.");
        }

        try {
            if (precioStr == null || stockStr == null || idUnidadStr == null || idEstadoStr == null) {
                throw new NumberFormatException("Faltan campos numéricos.");
            }

            BigDecimal precio = new BigDecimal(precioStr.trim());
            int stock = Integer.parseInt(stockStr.trim());
            int idUnidad = Integer.parseInt(idUnidadStr.trim());
            int idEstado = Integer.parseInt(idEstadoStr.trim());

            if ("actualizar".equalsIgnoreCase(accion)) {
                inventarioService.actualizarProducto(
                        vendedor, idProducto, idTienda, nombre, descripcion,
                        precio, stock, idUnidad, idEstado);

                redirectAttributes.addAttribute("idTienda", idTienda);
                redirectAttributes.addAttribute("ok", "actualizado");
                return "redirect:/vendedor/inventario";
            }

            inventarioService.crearProducto(
                    vendedor, idTienda, nombre, descripcion,
                    precio, stock, idUnidad, idEstado);

            redirectAttributes.addAttribute("idTienda", idTienda);
            redirectAttributes.addAttribute("ok", "producto");
            return "redirect:/vendedor/inventario";

        } catch (NumberFormatException e) {
            return mostrarVista(model, request, idTienda, null, 0, null,
                    "Precio, stock, unidad, estado y producto deben ser valores válidos.");
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            return mostrarVista(model, request, idTienda, null, 0, null, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error procesando producto en tienda {}", idTienda, e);
            return mostrarVista(model, request, idTienda, null, 0, null,
                    "No fue posible procesar el producto. Verifica los datos ingresados.");
        }
    }

    // Reemplaza al método privado mostrarVista y el forward("/inventario.html")
    private String mostrarVista(Model model, HttpServletRequest request, int idTiendaParam,
                                String search, int editarId, String ok, String error) {

        Integer vendedor = AuthUtil.getIdUsuario(request);
        if (vendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para acceder al inventario.");
        }

        try {
            List<TiendaDTO> tiendas = inventarioService.listarTiendas(vendedor);
            int idTienda = idTiendaParam;

            if (idTienda <= 0 && !tiendas.isEmpty()) {
                idTienda = tiendas.get(0).getIdTienda();
            }

            model.addAttribute("tiendas", tiendas);
            model.addAttribute("idTienda", idTienda);
            model.addAttribute("search", search);
            model.addAttribute("unidades", inventarioService.listarUnidades());
            model.addAttribute("estadosProducto", inventarioService.listarEstadosProducto());

            if (editarId > 0) {
                Productos productoEditar = productosRepository.findById(editarId).orElse(null);
                if (productoEditar == null) {
                    model.addAttribute("error", "El producto seleccionado no existe.");
                } else if (!Integer.valueOf(idTienda).equals(productoEditar.getIdTienda())) {
                    model.addAttribute("error", "El producto no pertenece a la tienda seleccionada.");
                } else {
                    model.addAttribute("productoEditar", productoEditar);
                }
            }

            if (error != null) {
                model.addAttribute("error", error);
            } else {
                model.addAttribute("ok", ok);
            }

            if (idTienda > 0) {
                VendedorService.InventarioData data =
                        inventarioService.obtenerInventario(idTienda, vendedor, search);
                model.addAttribute("metricasInventario", data);
                model.addAttribute("productos", data.productos);
            } else {
                model.addAttribute("error", "El vendedor no tiene una tienda registrada.");
            }

            return "inventario";

        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error consultando inventario para el vendedor {}", vendedor, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible cargar el inventario.", e);
        }
    }
}