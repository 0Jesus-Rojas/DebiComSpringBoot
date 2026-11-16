package com.debicomspringboot.service;

import com.debicomspringboot.dto.PerfilDTO;
import com.debicomspringboot.entity.Clientes;
import com.debicomspringboot.entity.Usuarios;
import com.debicomspringboot.exception.ExcepcionRegistroUsuario;
import com.debicomspringboot.repository.ClientesRepository;
import com.debicomspringboot.repository.UsuariosRepository;
import com.debicomspringboot.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UsuariosService {

    private final UsuariosRepository usuarioRepository;
    private final ClientesRepository clienteRepository;

    // 1. Reemplaza consultarUsuario()
    public Usuarios consultarUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario).orElse(null);
    }

    // 2. Reemplaza buscarPorCorreo()
    public Usuarios buscarPorCorreo(String correo) {
        return usuarioRepository.buscarPorCorreo(correo).orElse(null);
    }

    // 3. Reemplaza autenticar() manteniendo tu migración automática de contraseñas
    @Transactional
    public Usuarios autenticar(String correo, String password) {
        Usuarios usuario = buscarPorCorreo(correo);
        if (usuario == null) {
            return null;
        }

        String stored = usuario.getPassword();
        boolean valid;

        if (PasswordUtil.isHashed(stored)) {
            valid = PasswordUtil.verify(password, stored);
        } else {
            valid = Objects.equals(stored, password);
            if (valid) {
                actualizarPassword(usuario.getIdUsuario(), PasswordUtil.hash(password));
            }
        }
        return valid ? usuario : null;
    }

    // 4. Reemplaza actualizarPassword()
    @Transactional
    public boolean actualizarPassword(Integer idUsuario, String passwordHash) {
        if (!PasswordUtil.isHashed(passwordHash)) {
            throw new IllegalArgumentException("La contraseña debe recibirse ya convertida a un hash PBKDF2.");
        }
        return usuarioRepository.findById(idUsuario).map(u -> {
            u.setPassword(passwordHash);
            usuarioRepository.save(u);
            return true;
        }).orElse(false);
    }

    // 5. Reemplaza consultarPerfil()
    public PerfilDTO consultarPerfil(Integer idUsuario) {
        Object[] fila = usuarioRepository.consultarPerfilCrudo(idUsuario);
        if (fila == null || fila.length == 0) {
            return null;
        }
        // Si Spring devuelve un arreglo anidado por ser consulta nativa, tomamos la fila 0
        Object[] row = (fila[0] instanceof Object[]) ? (Object[]) fila[0] : fila;

        PerfilDTO dto = new PerfilDTO();
        dto.setIdUsuario(((Number) row[0]).intValue());
        dto.setIdCliente(((Number) row[1]).intValue());
        dto.setNombre((String) row[2]);
        dto.setApellido((String) row[3]);
        dto.setIdentificacion((String) row[4]);
        dto.setTipoIdentificacion((String) row[5]);
        Object fechaNacimiento = row[6];
        if (fechaNacimiento == null) {
            dto.setFechaNacimiento(null);
        } else if (fechaNacimiento instanceof LocalDate fechaLocal) {
            dto.setFechaNacimiento(Date.valueOf(fechaLocal));
        } else if (fechaNacimiento instanceof Date fechaSql) {
            dto.setFechaNacimiento(fechaSql);
        } else if (fechaNacimiento instanceof LocalDateTime fechaHora) {
            dto.setFechaNacimiento(Date.valueOf(fechaHora.toLocalDate()));
        } else if (fechaNacimiento instanceof java.sql.Timestamp timestamp) {
            dto.setFechaNacimiento(new Date(timestamp.getTime()));
        } else {
            throw new IllegalStateException("Tipo de fecha de nacimiento no soportado: "+ fechaNacimiento.getClass().getName());
        }
        dto.setCorreo((String) row[7]);
        dto.setTelefono((String) row[8]);
        dto.setDireccion((String) row[9]);
        dto.setAutorizaDatos(row[10] == null ? null : (row[10] instanceof Boolean b ? (byte) (b ? 1 : 0) : ((Number) row[10]).byteValue()));
        dto.setRol((String) row[11]);
        return dto;
    }

    // 6. Reemplaza actualizarPerfil() sin tocar la contraseña
    @Transactional
    public boolean actualizarPerfil(PerfilDTO perfil) {
        return usuarioRepository.findById(perfil.getIdUsuario()).map(u -> {
            u.setNombre(perfil.getNombre());
            u.setApellido(perfil.getApellido());
            u.setIdentificacion(perfil.getIdentificacion());
            u.setFechaNacimiento(perfil.getFechaNacimiento());
            u.setCorreo(perfil.getCorreo());
            u.setTelefono(perfil.getTelefono());
            u.setDireccion(perfil.getDireccion());
            u.setAutorizaDatos(perfil.getAutorizaDatos());
            usuarioRepository.save(u);
            return true;
        }).orElse(false);
    }

    // 7. Reemplaza obtenerIdClientePorUsuario()
    public Integer obtenerIdClientePorUsuario(Integer idUsuario) {
        return clienteRepository.findByIdUsuario(idUsuario)
                .map(Clientes::getIdCliente)
                .orElse(null);
    }

    // 8. Reemplaza registrarUsuarioComoComprador() con transacción automática
    @Transactional(rollbackFor = Exception.class)
    public int registrarUsuarioComoComprador(Usuarios u) {
        if (u == null) {
            throw new ExcepcionRegistroUsuario(
                    ExcepcionRegistroUsuario.Tipo.REFERENCIA_INVALIDA, "Los datos del usuario son obligatorios.");
        }

        String correo = u.getCorreo() == null ? "" : u.getCorreo().trim();
        String identificacion = u.getIdentificacion() == null ? "" : u.getIdentificacion().trim();

        if (correo.isBlank()) {
            throw new ExcepcionRegistroUsuario(
                    ExcepcionRegistroUsuario.Tipo.REFERENCIA_INVALIDA, "El correo es obligatorio.");
        }
        if (identificacion.isBlank()) {
            throw new ExcepcionRegistroUsuario(
                    ExcepcionRegistroUsuario.Tipo.REFERENCIA_INVALIDA, "La identificación es obligatoria.");
        }

        if (usuarioRepository.existeCorreo(correo)) {
            throw new ExcepcionRegistroUsuario(
                    ExcepcionRegistroUsuario.Tipo.CORREO_DUPLICADO, "El correo electrónico ya está registrado.");
        }
        if (usuarioRepository.existeIdentificacion(identificacion)) {
            throw new ExcepcionRegistroUsuario(
                    ExcepcionRegistroUsuario.Tipo.IDENTIFICACION_DUPLICADA, "La identificación ya está registrada.");
        }

        try {
            // Preparamos los valores por defecto que antes hacías en el SQL
            u.setIdUsuario(null);
            u.setCorreo(correo);
            u.setIdentificacion(identificacion);
            u.setTelefono(u.getTelefono() == null ? "" : u.getTelefono());
            u.setDireccion(u.getDireccion() == null ? "" : u.getDireccion());
            u.setPassword(protegerPassword(u.getPassword()));
            u.setFechaVencimientoClave(Date.valueOf(LocalDate.now().plusDays(180))); // Reemplaza DATE_ADD(..., INTERVAL 180 DAY)
            u.setFechaRegistro(LocalDateTime.now());                      // Reemplaza CURRENT_TIMESTAMP
            u.setIdRol(null);

            // 1. Guarda el usuario y recupera el ID generado automáticamente
            Usuarios usuarioGuardado = usuarioRepository.save(u);

            // 2. Crea el registro asociado en la tabla clientes
            Clientes nuevoCliente = new Clientes();
            nuevoCliente.setCreditoActual(BigDecimal.ZERO);
            nuevoCliente.setIdUsuario(usuarioGuardado.getIdUsuario());
            clienteRepository.save(nuevoCliente);

            return usuarioGuardado.getIdUsuario();

        } catch (DataIntegrityViolationException e) {
            // Reemplaza la captura del error 1452 de MySQL (llave foránea inválida)
            throw new ExcepcionRegistroUsuario(
                    ExcepcionRegistroUsuario.Tipo.REFERENCIA_INVALIDA,
                    "El tipo de identificación seleccionado no existe en la base de datos.", e);
        }
    }

    // 9. Reemplaza puedeRegistrarseComoVendedor()
    public boolean puedeRegistrarseComoVendedor(Integer idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            return false;
        }
        String rol = usuarioRepository.obtenerNombreRolPorUsuario(idUsuario);
        return rol == null || "COMPRADOR".equalsIgnoreCase(rol.trim());
    }

    // 10. Reemplaza registrarComoVendedor()
    @Transactional
    public int registrarComoVendedor(Integer idUsuario) {
        Integer idRolVendedor = usuarioRepository.obtenerIdRolPorNombre("VENDEDOR");
        if (idRolVendedor == null) {
            throw new IllegalStateException("No existe el rol Vendedor en la base de datos.");
        }

        if (!puedeRegistrarseComoVendedor(idUsuario)) {
            return 0;
        }

        Usuarios usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            return 0;
        }

        usuario.setIdRol(idRolVendedor);
        usuarioRepository.save(usuario);
        return idRolVendedor;
    }

    // 11. Reemplaza eliminarUsuario()
    @Transactional
    public boolean eliminarUsuario(Integer idUsuario) {
        if (usuarioRepository.existsById(idUsuario)) {
            usuarioRepository.deleteById(idUsuario);
            return true;
        }
        return false;
    }

    private String protegerPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        return PasswordUtil.isHashed(password) ? password : PasswordUtil.hash(password);
    }
}