package com.proyecto.servicios.service;

import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClientePatchRequest;
import com.proyecto.servicios.model.cliente.ClienteRegistroRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;

import java.util.List;

public interface ClienteService {

    /**
     * Registra un nuevo cliente, valida reglas de negocio (unicidad CURP/RFC, mayoría de edad)
     * y genera automáticamente su cuenta bancaria con saldo inicial.
     */
    ClienteResponse registrarCliente(ClienteRegistroRequest request);

    /**
     * Consulta un cliente por su identificador único.
     */
    ClienteResponse consultarPorId(Integer id);

    /**
     * Consulta clientes con filtros opcionales (nombre, CURP, RFC, activo).
     */
    List<ClienteResponse> consultarClientes(String nombre, String curp, String rfc, Boolean activo);

    /**
     * Actualización total de información del cliente (PUT).
     */
    ClienteResponse actualizarCliente(Integer id, ClienteActualizaRequest request);

    /**
     * Actualización parcial de información del cliente (PATCH).
     */
    ClienteResponse actualizarParcial(Integer id, ClientePatchRequest request);

    /**
     * Baja lógica del cliente y su cuenta bancaria vinculada (DELETE).
     */
    void bajaLogica(Integer id);
}
