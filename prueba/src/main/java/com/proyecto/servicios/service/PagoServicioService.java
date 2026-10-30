package com.proyecto.servicios.service;

import com.proyecto.servicios.model.pago.ConciliacionRequest;
import com.proyecto.servicios.model.pago.PagoServicioRequest;
import com.proyecto.servicios.model.pago.PagoServicioResponse;

public interface PagoServicioService {

    /**
     * Procesa una transacción de pago de servicio con control de concurrencia, idempotencia,
     * validación de catálogo/montos, reserva preventiva de saldo y manejo de conciliación ante timeouts.
     */
    PagoServicioResponse procesarPagoServicio(PagoServicioRequest request);

    /**
     * Procesa una transacción validando adicionalmente el token JWT o identificador del cliente autenticado
     * y la clave de idempotencia recibida por cabecera HTTP.
     */
    PagoServicioResponse procesarPagoServicio(PagoServicioRequest request, String authorizationHeader, String headerIdempotencyKey);

    /**
     * Concilia una operación que quedó en estado PENDIENTE_CONCILIACION tras un timeout con el proveedor.
     * Si el proveedor confirma la operación, pasa a EXITOSA; si confirma que no fue procesada, revierte el saldo.
     */
    PagoServicioResponse conciliarTransaccion(Integer idTransaccion, ConciliacionRequest request);

    /**
     * Consulta el estado actual de una transacción de pago de servicio por su ID interno.
     */
    PagoServicioResponse consultarEstadoTransaccion(Integer idTransaccion);
}
