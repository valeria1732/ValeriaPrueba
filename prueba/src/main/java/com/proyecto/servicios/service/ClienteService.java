package com.proyecto.servicios.service;

import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClientePatchRequest;
import com.proyecto.servicios.model.cliente.ClienteRegistroRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;

import java.time.LocalDate;
import java.util.List;

public interface ClienteService {

    ClienteResponse registrarCliente(ClienteRegistroRequest request);

    ClienteResponse consultarPorId(Integer id);

    ClienteResponse consultarPorCurp(String curp);

    ClienteResponse consultarPorRfc(String rfc);

    ClienteResponse consultarPorCorreo(String correo);

    ClienteResponse consultarPorNumeroCuenta(String numeroCuenta);

    List<ClienteResponse> consultarClientes(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            String curp,
            String rfc,
            String correo,
            Boolean activo,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    ClienteResponse actualizarCliente(Integer id, ClienteActualizaRequest request);

    ClienteResponse actualizarParcial(Integer id, ClientePatchRequest request);

    void bajaLogica(Integer id);
}
