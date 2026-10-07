package com.proyecto.servicios.service;

import com.proyecto.servicios.model.cuenta.CuentaActualizaRequest;
import com.proyecto.servicios.model.cuenta.CuentaCreacionRequest;
import com.proyecto.servicios.model.cuenta.CuentaResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CuentaService {

    CuentaResponse crearCuenta(CuentaCreacionRequest request);

    CuentaResponse consultarPorNumeroCuenta(String numeroCuenta);

    List<CuentaResponse> consultarPorClienteId(Integer clienteId);

    List<CuentaResponse> consultarPorEstatus(String estatus);

    CuentaResponse actualizarParcial(String numeroCuenta, CuentaActualizaRequest request);

    BigDecimal consultarSaldo(String numeroCuenta);
}
