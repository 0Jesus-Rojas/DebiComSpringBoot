package com.debicomspringboot.controller;

import com.debicomspringboot.entity.Usuarios;
import com.debicomspringboot.exception.ExcepcionRegistroUsuario;
import com.debicomspringboot.service.UsuariosService;
import com.debicomspringboot.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import com.debicomspringboot.entity.TipoIdentificaciones;
import com.debicomspringboot.service.TipoIdentificacionService;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

import java.sql.Date;
import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuariosService usuarioService;
    private final TipoIdentificacionService tipoIdentificacionService;
    @ModelAttribute("tipos")
    public List<TipoIdentificaciones> cargarTiposIdentificacion() {
        return tipoIdentificacionService.consultarTiposIdentificacion();
    }

    @GetMapping({"/", "/login"})
    public String mostrarLogin() {
        return "index";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam(name = "correo", required = false) String correo,
            @RequestParam(name = "clave", required = false) String clave,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        if (correo == null || correo.trim().isEmpty() || clave == null || clave.trim().isEmpty()) {
            model.addAttribute("authTab", "login");
            model.addAttribute("authMessage", "Debes ingresar el correo y la contraseña.");
            model.addAttribute("authType", "warning");
            return "index";
        }

        String correoLimpio = correo.trim();

        try {
            Usuarios usuario = usuarioService.buscarPorCorreo(correoLimpio);

            if (usuario == null) {
                model.addAttribute("authTab", "login");
                model.addAttribute("authMessage", "No existe un usuario registrado con ese correo.");
                model.addAttribute("authType", "error");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return "index";
            }

            String passwordGuardada = usuario.getPassword();
            boolean passwordCorrecta;

            if (PasswordUtil.isHashed(passwordGuardada)) {
                passwordCorrecta = PasswordUtil.verify(clave, passwordGuardada);
            } else {
                passwordCorrecta = Objects.equals(passwordGuardada, clave);

                if (passwordCorrecta) {
                    usuarioService.actualizarPassword(
                            usuario.getIdUsuario(),
                            PasswordUtil.hash(clave));
                }
            }

            if (!passwordCorrecta) {
                model.addAttribute("authTab", "login");
                model.addAttribute("authMessage", "La contraseña ingresada es incorrecta.");
                model.addAttribute("authType", "error");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return "index";
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("idUsuario", usuario.getIdUsuario());
            session.setAttribute("correo", usuario.getCorreo());
            session.setAttribute("nombre", usuario.getNombre());
            session.setAttribute("apellido", usuario.getApellido());
            session.setAttribute("idRol", usuario.getIdRol());

            Integer idCliente = usuarioService.obtenerIdClientePorUsuario(usuario.getIdUsuario());
            if (idCliente != null) {
                session.setAttribute("idCliente", idCliente);
            }

            Integer rol = usuario.getIdRol();
            if (rol != null && (rol == 1 || rol == 2 || rol == 3)) {
                return "redirect:/vendedor/dashboard";
            } else {
                return "redirect:/comprador/estado-credito";
            }

        } catch (RuntimeException e) {
            log.error("Error de autenticación para el correo {}", correoLimpio, e);
            model.addAttribute("authTab", "login");
            model.addAttribute("authMessage",
                    "No fue posible consultar la base de datos. Intenta nuevamente.");
            model.addAttribute("authType", "error");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "index";
        }
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String cerrarSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
    // Reemplaza a doGet de RegisterServlet (/register)
    @GetMapping("/register")
    public String mostrarRegistro() {
        return "redirect:/";
    }

    // Reemplaza a doPost de RegisterServlet (/register)
    @PostMapping("/register")
    public String procesarRegistro(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "apellido", required = false) String apellido,
            @RequestParam(name = "tipo_id", required = false) String tipo,
            @RequestParam(name = "num_id", required = false) String identificacion,
            @RequestParam(name = "fecha_nac", required = false) String fecha,
            @RequestParam(name = "telefono", required = false) String telefono,
            @RequestParam(name = "direccion", required = false) String direccion,
            @RequestParam(name = "correo", required = false) String correo,
            @RequestParam(name = "clave", required = false) String clave,
            @RequestParam(name = "autoriza", required = false) String autorizaParam,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        boolean autoriza = "true".equalsIgnoreCase(autorizaParam)
                || "on".equalsIgnoreCase(autorizaParam);

        if (blank(nombre) || blank(apellido) || blank(tipo) || blank(identificacion)
                || blank(fecha) || blank(correo) || blank(clave)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Todos los campos obligatorios deben estar diligenciados.");
        }

        final int idTipo;
        final Date fechaNacimiento;

        try {
            idTipo = Integer.parseInt(tipo.trim());
            fechaNacimiento = Date.valueOf(fecha.trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo de identificación o fecha de nacimiento inválidos.", e);
        }

        Usuarios usuario = new Usuarios();
        usuario.setNombre(nombre.trim());
        usuario.setApellido(apellido.trim());
        usuario.setIdentificacion(identificacion.trim());
        usuario.setFechaNacimiento(fechaNacimiento);
        usuario.setCorreo(correo.trim());
        usuario.setTelefono(telefono == null ? "" : telefono.trim());
        usuario.setDireccion(direccion == null ? "" : direccion.trim());
        usuario.setPassword(PasswordUtil.hash(clave));
        usuario.setAutorizaDatos(autoriza ? (byte) 1 : (byte) 0);
        usuario.setIdTipoIdentificacion(idTipo);
        usuario.setIdRol(0);

        try {
            int idUsuario = usuarioService.registrarUsuarioComoComprador(usuario);

            HttpSession session = request.getSession(true);
            session.setAttribute("idUsuario", idUsuario);
            session.setAttribute("correo", usuario.getCorreo());
            session.setAttribute("nombre", usuario.getNombre());
            session.setAttribute("apellido", usuario.getApellido());
            session.setAttribute("idRol", 0);
            session.setAttribute("idCliente",
                    usuarioService.obtenerIdClientePorUsuario(idUsuario));

            return "redirect:/comprador/estado-credito";

        } catch (ExcepcionRegistroUsuario e) {
            log.error("Error controlado registrando usuario/comprador", e);
            model.addAttribute("authTab", "register");
            model.addAttribute("authMessage", e.getMessage());

            switch (e.getTipo()) {
                case CORREO_DUPLICADO:
                case IDENTIFICACION_DUPLICADA:
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    break;
                case REFERENCIA_INVALIDA:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    break;
            }

            return "index";

        } catch (RuntimeException e) {
            log.error("Error inesperado registrando usuario/comprador", e);
            model.addAttribute("authTab", "register");
            model.addAttribute("authMessage",
                    "No fue posible completar el registro debido a un error interno.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "index";
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}