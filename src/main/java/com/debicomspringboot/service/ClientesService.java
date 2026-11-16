package com.debicomspringboot.service;

import com.debicomspringboot.entity.Clientes;
import com.debicomspringboot.repository.ClientesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor // Inyecta ClienteRepository automáticamente (reemplaza "new Conexion()")
public class ClientesService {

    private final ClientesRepository clienteRepository;

    // Equivale a consultarCliente(int idCliente)
    public Clientes consultarCliente(Integer idCliente) {
        return clienteRepository.findById(idCliente).orElse(null);
    }

    // Equivale a insertarCliente(Cliente miCliente)
    public boolean insertarCliente(Clientes miCliente) {
        try {
            miCliente.setIdCliente(null); // Asegura que sea un registro nuevo (INSERT)
            clienteRepository.save(miCliente);
            return true;
        } catch (Exception e) {
            System.out.println("Error al insertar: " + e.getMessage());
            return false;
        }
    }

    // Equivale a actualizarCliente(Cliente miCliente)
    public boolean actualizarCliente(Clientes miCliente) {
        // Reemplaza tu validación: if (filasAfectadas > 0)
        if (miCliente.getIdCliente() != null && clienteRepository.existsById(miCliente.getIdCliente())) {
            clienteRepository.save(miCliente);
            return true;
        }
        System.out.println("No se encontró el cliente");
        return false;
    }

    // Equivale a eliminarCliente(int idCliente)
    public boolean eliminarCliente(Integer idCliente) {
        if (clienteRepository.existsById(idCliente)) {
            clienteRepository.deleteById(idCliente);
            return true;
        }
        return false;
    }

    // Extra útil para tus vistas con Thymeleaf:
    public List<Clientes> listarTodos() {
        return clienteRepository.findAll();
    }
}