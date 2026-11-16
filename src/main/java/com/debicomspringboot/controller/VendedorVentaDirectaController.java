package com.debicomspringboot.controller;

import com.debicomspringboot.dto.CompradorDTO;
import com.debicomspringboot.dto.FacturaPDFDTO;
import com.debicomspringboot.dto.ItemVentaDirectaDTO;
import com.debicomspringboot.dto.ProductoDTO;
import com.debicomspringboot.dto.TiendaDTO;
import com.debicomspringboot.entity.TipoPago;
import com.debicomspringboot.service.FacturaPDFService;
import com.debicomspringboot.service.VendedorService;
import com.debicomspringboot.service.VentaDirectaService;
import com.debicomspringboot.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/vendedor/ventas-directas")
@RequiredArgsConstructor
public class VendedorVentaDirectaController {

    private final VentaDirectaService ventaDirectaService;
    private final VendedorService vendedorService;

    // Reemplaza a doGet de VentaDirectaServlet (/vendedor/ventas-directas)
    @GetMapping
    public String verVentasDirectas(
            @RequestParam(name = "idTienda", defaultValue = "0") int idTienda,
            @RequestParam(name = "idFactura", defaultValue = "0") int idFactura,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "ok", required = false) String ok,
            HttpServletRequest request,
            Model model) {

        Integer idVendedor = AuthUtil.getIdUsuario(request);
        if (idVendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para registrar ventas directas.");
        }

        return cargarVista(model, idVendedor, idTienda, idFactura, error, ok);
    }

    // Reemplaza a doPost de VentaDirectaServlet (/vendedor/ventas-directas)
    @PostMapping
    public String registrarVentaDirecta(
            @RequestParam(name = "idTienda", defaultValue = "0") int idTienda,
            @RequestParam(name = "idCliente", defaultValue = "0") int idCliente,
            @RequestParam(name = "idTipoPago", defaultValue = "0") int idTipoPago,
            @RequestParam(name = "idProducto", required = false) String[] ids,
            @RequestParam(name = "observaciones", required = false) String observaciones,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer idVendedor = AuthUtil.getIdUsuario(request);
        if (idVendedor == null) {
            return "redirect:/";
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para registrar ventas directas.");
        }

        try {
            List<ItemVentaDirectaDTO> items = new ArrayList<>();
            if (ids != null) {
                for (String id : ids) {
                    int idProducto = parseInt(id);
                    int cantidad = parseInt(request.getParameter("cantidad_" + id));
                    items.add(new ItemVentaDirectaDTO(idProducto, cantidad));
                }
            }

            int idFactura = ventaDirectaService.registrarVentaDirecta(
                    idVendedor,
                    idTienda,
                    idCliente,
                    idTipoPago,
                    observaciones,
                    items);

            redirectAttributes.addAttribute("ok", "venta-registrada");
            redirectAttributes.addAttribute("idFactura", idFactura);
            redirectAttributes.addAttribute("idTienda", idTienda);
            return "redirect:/vendedor/ventas-directas";

        } catch (NumberFormatException | ArithmeticException e) {
            return cargarVista(model, idVendedor, idTienda, 0,
                    "Los identificadores y cantidades deben ser válidos.", null);
        } catch (SecurityException | IllegalArgumentException e) {
            return cargarVista(model, idVendedor, idTienda, 0,
                    e.getMessage(), null);
        } catch (RuntimeException e) {
            log.error("Error registrando venta directa en tienda {}", idTienda, e);
            return cargarVista(model, idVendedor, idTienda, 0,
                    "No fue posible registrar la venta directa. Verifica los datos e inténtalo de nuevo.", null);
        }
    }

    // Reemplaza a doGet de FacturaPDFServlet (/vendedor/ventas-directas/factura)
    @GetMapping("/factura")
    public ResponseEntity<byte[]> descargarFacturaPdf(
            @RequestParam(name = "id", defaultValue = "0") int idFactura,
            HttpServletRequest request) {

        Integer idVendedor = AuthUtil.getIdUsuario(request);
        if (idVendedor == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(request.getContextPath() + "/"))
                    .build();
        }
        if (!AuthUtil.isStaff(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para descargar facturas.");
        }
        if (idFactura <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Factura inválida.");
        }

        try {
            FacturaPDFDTO factura = ventaDirectaService.obtenerFacturaDirectaParaPDF(idFactura, idVendedor);
            if (factura == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La factura no existe o no pertenece al vendedor autenticado.");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            FacturaPDFService.generar(factura, out);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(sanitizeFilename(factura.getNumeroFactura()) + ".pdf")
                            .build()
            );
            headers.add("X-Content-Type-Options", "nosniff");

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error generando PDF de la factura {}", idFactura, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible generar la factura PDF.", e);
        }
    }

    private String cargarVista(Model model, int idVendedor, int idTiendaParam, int idFactura,
                               String error, String ok) {
        List<TiendaDTO> tiendas = vendedorService.listarTiendasDelVendedor(idVendedor);
        int idTienda = idTiendaParam;

        if (idTienda <= 0 && !tiendas.isEmpty()) {
            idTienda = tiendas.get(0).getIdTienda();
        }
        boolean tiendaValida = false;
        for (TiendaDTO tienda : tiendas) {
            if (tienda.getIdTienda() == idTienda) {
                tiendaValida = true;
                break;
            }
        }
        if (idTienda > 0 && !tiendaValida) {
            idTienda = tiendas.isEmpty() ? 0 : tiendas.get(0).getIdTienda();
        }

        List<ProductoDTO> productos = idTienda > 0
                ? ventaDirectaService.listarProductosParaVenta(idTienda, idVendedor)
                : List.of();
        List<CompradorDTO> compradores = vendedorService.listarCompradores();
        List<TipoPago> tiposPago = ventaDirectaService.listarTiposPago();

        model.addAttribute("tiendas", tiendas);
        model.addAttribute("productosVenta", productos);
        model.addAttribute("compradores", compradores);
        model.addAttribute("tiposPago", tiposPago);
        model.addAttribute("idTienda", idTienda);
        model.addAttribute("idFactura", idFactura);
        model.addAttribute("error", error);
        model.addAttribute("ok", ok);

        return "ventas-directas";
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private String sanitizeFilename(String value) {
        if (value == null || value.isBlank()) {
            return "factura-venta-directa";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}