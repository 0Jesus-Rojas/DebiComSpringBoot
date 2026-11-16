package com.debicomspringboot.controller;

import com.debicomspringboot.dto.*;
import com.debicomspringboot.service.*;
import com.debicomspringboot.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/comprador")
@RequiredArgsConstructor
public class CompradorController {

    private final UsuariosService usuarioService;
    private final SolicitudCreditoService solicitudCreditoService;
    private final ReporteCompradorService reporteCompradorService;
    private final UsuariosService usuariosService;
    private final SolicitudCreditoService solicitudesCreditoService;
    private final RegistroVendedorService registroVendedorService;
    private final TiendaService tiendaService;

    // Reemplaza a doGet de DetallePrestamoCompradorServlet (/comprador/estado-credito/detalle)
    @GetMapping("/estado-credito/detalle")
    public String verDetallePrestamo(
            @RequestParam(name = "id", defaultValue = "0") int id,
            HttpServletRequest request,
            Model model) {

        Integer idUsuario = AuthUtil.getIdUsuario(request);
        if (idUsuario == null) {
            return "redirect:/";
        }
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de préstamo inválido.");
        }

        try {
            Integer idCliente = usuarioService.obtenerIdClientePorUsuario(idUsuario);
            if (idCliente == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El usuario no tiene perfil de cliente.");
            }

            SolicitudCreditoDTO prestamo = solicitudCreditoService.consultarPorIdYCliente(id, idCliente)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Préstamo no encontrado."));

            model.addAttribute("prestamo", prestamo);
            model.addAttribute("productosPrestamo", solicitudCreditoService.listarProductosSolicitud(id));
            return "detalle-prestamo";
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error consultando detalle del préstamo {} del comprador", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible consultar el detalle del préstamo.", e);
        }
    }
    // Reemplaza a doGet de DeudasPendientesServlet (/comprador/deudas)
    @GetMapping("/deudas")
    public String verDeudasPendientes(HttpServletRequest request, Model model) {
        Integer idUsuario = AuthUtil.getIdUsuario(request);
        if (idUsuario == null) {
            return "redirect:/";
        }

        try {
            List<DeudaPendienteDTO> deudas = reporteCompradorService.listarDeudasPorUsuario(idUsuario);

            BigDecimal total = deudas.stream()
                    .map(DeudaPendienteDTO::getSaldoPendiente)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long vencidas = deudas.stream()
                    .filter(d -> d.getFechaVencimiento() != null
                            && d.getFechaVencimiento().isBefore(LocalDate.now()))
                    .count();

            model.addAttribute("deudas", deudas);
            model.addAttribute("deudaTotal", total);
            model.addAttribute("pagosAlDia", deudas.size() - vencidas);
            model.addAttribute("pagosVencidos", vencidas);

            return "consultar-deudas";
        } catch (RuntimeException e) {
            log.error("Error consultando deudas del usuario {}", idUsuario, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible consultar las deudas.", e);
        }
    }
    // Reemplaza a doGet de EstadoCreditoServlet (/comprador/estado-credito)
    @GetMapping("/estado-credito")
    public String verEstadoCredito(HttpServletRequest request, Model model) {
        Integer idUsuario = AuthUtil.getIdUsuario(request);
        if (idUsuario == null) {
            return "redirect:/";
        }

        try {
            Integer idCliente = usuarioService.obtenerIdClientePorUsuario(idUsuario);
            if (idCliente == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El usuario no tiene perfil de cliente.");
            }

            model.addAttribute("solicitudes", solicitudCreditoService.listarPorCliente(idCliente));
            return "estado-credito";
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error consultando estado de préstamos del usuario {}", idUsuario, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible consultar el estado de los préstamos.", e);
        }
    }
    // Reemplaza a doGet de HistorialPagosServlet (/comprador/historial-pagos)
    @GetMapping("/historial-pagos")
    public String verHistorialPagos(HttpServletRequest request, Model model) {
        Integer idUsuario = AuthUtil.getIdUsuario(request);
        if (idUsuario == null) {
            return "redirect:/";
        }

        try {
            List<PagoHistorialDTO> pagos = reporteCompradorService.listarPagosPorUsuario(idUsuario);

            BigDecimal total = pagos.stream()
                    .map(PagoHistorialDTO::getMontoPagado)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("pagos", pagos);
            model.addAttribute("totalPagado", total);

            return "historial-pagos";
        } catch (RuntimeException e) {
            log.error("Error consultando historial de pagos del usuario {}", idUsuario, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible consultar el historial.", e);
        }
    }
    // Reemplaza a doGet de PerfilServlet (/comprador/perfil)
    @GetMapping("/perfil")
    public String verPerfil(HttpServletRequest request, Model model) {
        Integer idUsuario = obtenerIdUsuario(request.getSession(false));
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida");
        }

        PerfilDTO perfil = usuarioService.consultarPerfil(idUsuario);
        if (perfil == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        model.addAttribute("perfil", perfil);
        model.addAttribute("puedeRegistrarseVendedor",
                usuarioService.puedeRegistrarseComoVendedor(idUsuario));
        return "perfil";
    }

    // Reemplaza a doPost de PerfilServlet (/comprador/perfil)
    @PostMapping("/perfil")
    public String actualizarPerfil(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "apellido", required = false) String apellido,
            @RequestParam(name = "identificacion", required = false) String identificacion,
            @RequestParam(name = "correo", required = false) String correo,
            @RequestParam(name = "telefono", required = false) String telefono,
            @RequestParam(name = "direccion", required = false) String direccion,
            @RequestParam(name = "autorizaDatos", required = false) String autorizaDatos,
            @RequestParam(name = "fechaNacimiento", required = false) String fecha,
            HttpServletRequest request) {

        Integer idUsuario = obtenerIdUsuario(request.getSession(false));
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida");
        }

        PerfilDTO perfil = new PerfilDTO();
        perfil.setIdUsuario(idUsuario);
        perfil.setNombre(nombre);
        perfil.setApellido(apellido);
        perfil.setIdentificacion(identificacion);
        perfil.setCorreo(correo);
        perfil.setTelefono(telefono);
        perfil.setDireccion(direccion);
        boolean autoriza = "on".equalsIgnoreCase(autorizaDatos)
                || "true".equalsIgnoreCase(autorizaDatos)
                || "1".equals(autorizaDatos);
        perfil.setAutorizaDatos(autoriza ? (byte) 1 : (byte) 0);

        if (fecha != null && !fecha.isBlank()) {
            try {
                perfil.setFechaNacimiento(java.sql.Date.valueOf(LocalDate.parse(fecha.trim())));
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha de nacimiento inválida", e);
            }
        }

        if (!datosValidos(perfil)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos de perfil incompletos");
        }

        try {
            if (!usuarioService.actualizarPerfil(perfil)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo actualizar el perfil");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error actualizando el perfil del usuario {}", idUsuario, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error actualizando el perfil", e);
        }

        HttpSession session = request.getSession();
        session.setAttribute("correo", perfil.getCorreo());
        session.setAttribute("nombre", perfil.getNombre());
        session.setAttribute("apellido", perfil.getApellido());

        return "redirect:/comprador/perfil";
    }

    private Integer obtenerIdUsuario(HttpSession session) {
        if (session == null) return null;

        Object id = session.getAttribute("idUsuario");
        if (id instanceof Integer i) return i;
        if (id instanceof Long l) return l.intValue();
        if (id instanceof String s) {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean datosValidos(PerfilDTO p) {
        return noVacio(p.getNombre())
                && noVacio(p.getApellido())
                && noVacio(p.getIdentificacion())
                && noVacio(p.getCorreo())
                && noVacio(p.getTelefono())
                && noVacio(p.getDireccion())
                && p.getFechaNacimiento() != null;
    }

    private boolean noVacio(String value) {
        return value != null && !value.isBlank();
    }
    // Reemplaza a doGet de RegistroVendedorServlet (/comprador/registrarse-vendedor)
    @GetMapping("/registrarse-vendedor")
    public String mostrarRegistroVendedor(HttpServletRequest request, Model model) {
        Integer idUsuario = AuthUtil.getIdUsuario(request);
        if (idUsuario == null) {
            return "redirect:/";
        }

        if (!registroVendedorService.puedeRegistrarseComoVendedor(idUsuario)) {
            model.addAttribute("error",
                    "Tu usuario ya tiene un rol asignado o ya está registrado como vendedor.");
        }

        return "registrarse-vendedor";
    }

    // Reemplaza a doPost de RegistroVendedorServlet (/comprador/registrarse-vendedor)
    @PostMapping("/registrarse-vendedor")
    public String procesarRegistroVendedor(HttpServletRequest request, Model model) {
        Integer idUsuario = AuthUtil.getIdUsuario(request);
        if (idUsuario == null) {
            return "redirect:/";
        }

        try {
            int idRol = registroVendedorService.registrarComoVendedor(idUsuario);
            if (idRol <= 0) {
                model.addAttribute("error",
                        "No fue posible completar el registro. Verifica que tu cuenta sea un comprador.");
                return "registrarse-vendedor";
            }

            HttpSession session = request.getSession();
            session.setAttribute("idRol", idRol);
            session.setAttribute("mensajeVendedor",
                    "Tu cuenta ahora está registrada como vendedor.");

            return "redirect:/vendedor/dashboard";

        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error registrando vendedor para usuario {}", idUsuario, e);
            model.addAttribute("error",
                    "Ocurrió un error al registrar tu cuenta como vendedor.");
            return "registrarse-vendedor";
        }
    }
    // Reemplaza a doGet de SolicitudCreditoServlet (/comprador/solicitar-credito)
    @GetMapping("/solicitar-credito")
    public String mostrarSolicitarCredito(
            @RequestParam(name = "ok", required = false) String ok,
            @RequestParam(name = "id", required = false) String idSolicitud,
            HttpServletRequest request,
            Model model) {

        Integer idUsuario = AuthUtil.getIdUsuario(request);
        if (idUsuario == null) {
            return "redirect:/";
        }

        try {
            Integer idCliente = usuarioService.obtenerIdClientePorUsuario(idUsuario);
            if (idCliente == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "El usuario autenticado no tiene un perfil de comprador.");
            }

            cargarFormularioSolicitud(model, idCliente, ok, idSolicitud);
            return "solicitar-credito";
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error al preparar la solicitud de crédito para usuario {}", idUsuario, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible preparar el formulario de crédito.", e);
        }
    }

    // Reemplaza a doPost de SolicitudCreditoServlet (/comprador/solicitar-credito)
    @PostMapping("/solicitar-credito")
    public String procesarSolicitarCredito(
            @RequestParam(name = "idTienda", required = false) String idTiendaStr,
            @RequestParam(name = "monto", required = false) String montoStr,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "observaciones", required = false) String observacionesParam,
            @RequestParam(name = "fechaVencimiento", required = false) String fechaVencimientoStr,
            @RequestParam(name = "plazo", required = false) String plazoStr,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer idUsuario = AuthUtil.getIdUsuario(request);
        if (idUsuario == null) {
            return "redirect:/";
        }

        try {
            Integer idCliente = usuarioService.obtenerIdClientePorUsuario(idUsuario);
            if (idCliente == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "El usuario autenticado no tiene un perfil de comprador.");
            }

            int idTienda = parsePositiveInt(idTiendaStr, "La tienda es obligatoria.");
            BigDecimal monto = parseMonto(montoStr);
            String observaciones = trim(descripcion);
            if (observaciones == null) {
                observaciones = trim(observacionesParam);
            }

            LocalDate fechaVencimiento = parseFechaVencimiento(fechaVencimientoStr, plazoStr);

            int idSolicitud = solicitudCreditoService.registrarSolicitud(
                    idCliente,
                    idTienda,
                    monto,
                    fechaVencimiento,
                    observaciones);

            redirectAttributes.addAttribute("ok", "1");
            redirectAttributes.addAttribute("id", idSolicitud);
            return "redirect:/comprador/solicitar-credito";

        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());

            try {
                Integer idCliente = usuarioService.obtenerIdClientePorUsuario(idUsuario);
                cargarFormularioSolicitud(model, idCliente == null ? 0 : idCliente, null, null);
            } catch (RuntimeException ignored) {
                // Conservamos el mensaje de validación original.
            }

            return "solicitar-credito";

        } catch (RuntimeException e) {
            log.error("Error al registrar solicitud de crédito para usuario {}", idUsuario, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible registrar la solicitud de crédito.", e);
        }
    }

    private void cargarFormularioSolicitud(Model model, int idCliente, String ok, String idSolicitud) {
        List<TiendaDTO> tiendas = tiendaService.listarTiendasDisponibles();
        model.addAttribute("tiendas", tiendas);
        model.addAttribute("idCliente", idCliente);

        if ("1".equals(ok)) {
            model.addAttribute("mensaje", "Solicitud registrada correctamente.");
            model.addAttribute("idSolicitud", idSolicitud);
        }
    }

    private int parsePositiveInt(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private BigDecimal parseMonto(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El monto solicitado es obligatorio.");
        }
        try {
            BigDecimal monto = new BigDecimal(value.replace("$", "").replace(",", "").trim());
            if (monto.signum() <= 0) {
                throw new NumberFormatException();
            }
            return monto.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El monto solicitado no es válido.");
        }
    }

    private LocalDate parseFechaVencimiento(String fechaParam, String plazoParam) {
        LocalDate hoy = LocalDate.now();
        LocalDate minimo = hoy.plusDays(15);
        LocalDate maximo = hoy.plusMonths(1);

        String fecha = trim(fechaParam);
        if (fecha != null) {
            try {
                LocalDate value = LocalDate.parse(fecha);
                if (value.isBefore(minimo) || value.isAfter(maximo)) {
                    throw new IllegalArgumentException("El plazo del crédito debe estar entre 15 días y 1 mes.");
                }
                return value;
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("La fecha de vencimiento no es válida.");
            }
        }

        String plazo = trim(plazoParam);
        if (plazo != null) {
            String normalizado = plazo.toLowerCase(Locale.ROOT);
            LocalDate fechaSeleccionada;
            if (normalizado.contains("15")) {
                fechaSeleccionada = hoy.plusDays(15);
            } else if (normalizado.contains("21")) {
                fechaSeleccionada = hoy.plusDays(21);
            } else if (normalizado.contains("30") || normalizado.contains("1 mes") || normalizado.contains("1mes")) {
                fechaSeleccionada = maximo;
            } else {
                throw new IllegalArgumentException("El plazo del crédito debe ser de 15 días, 21 días o 1 mes.");
            }
            if (fechaSeleccionada.isBefore(minimo) || fechaSeleccionada.isAfter(maximo)) {
                throw new IllegalArgumentException("El plazo del crédito seleccionado no es válido.");
            }
            return fechaSeleccionada;
        }

        return maximo;
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
    // Reemplaza a doGet de TiendasDisponiblesServlet (/comprador/tiendas)
    @GetMapping("/tiendas")
    public String listarTiendasDisponibles(HttpServletRequest request, Model model) {
        Integer idUsuario = obtenerIdUsuario(request.getSession(false));
        if (idUsuario == null) {
            return "redirect:/";
        }

        Integer idCliente = usuarioService.obtenerIdClientePorUsuario(idUsuario);
        if (idCliente == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El usuario autenticado no tiene un perfil de comprador.");
        }

        try {
            List<TiendaDTO> tiendas = tiendaService.listarTiendasDisponibles();
            model.addAttribute("tiendas", tiendas);
            model.addAttribute("idCliente", idCliente);
            return "solicitar-credito";
        } catch (RuntimeException e) {
            log.error("Error al cargar las tiendas disponibles para usuario {}", idUsuario, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible cargar las tiendas.", e);
        }
    }
}