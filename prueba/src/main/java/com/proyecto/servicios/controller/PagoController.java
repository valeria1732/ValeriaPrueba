package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.pago.ConciliacionRequest;
import com.proyecto.servicios.model.pago.PagoServicioRequest;
import com.proyecto.servicios.model.pago.PagoServicioResponse;
import com.proyecto.servicios.service.PagoServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/pagos")
@Tag(name = "Pagos y Transacciones", description = "Procesamiento seguro de pagos de servicios GestoPago (SendTx), control de idempotencia y conciliación")
public class PagoController {

    private final PagoServicioService pagoServicioService;

    @Autowired
    public PagoController(PagoServicioService pagoServicioService) {
        this.pagoServicioService = pagoServicioService;
    }

    @Operation(
            summary = "Procesar pago de servicio (Flujo SendTx)",
            description = "Valida identidad del cliente, cuenta activa, saldo disponible, existencia de producto en catálogo, límites de monto e idempotencia. Reserva el saldo preventivamente, cifra el payload con AES, envía la petición autenticada con Bearer Token a GestoPago SendTx y gestiona confirmación, reverso o estado PENDIENTE_CONCILIACION ante timeouts."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago procesado y saldo debitado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos, monto fuera de rango, saldo insuficiente o cuenta inactiva"),
            @ApiResponse(responseCode = "401", description = "Token inválido o credenciales no autorizadas"),
            @ApiResponse(responseCode = "403", description = "Cliente inactivo o sin autorización sobre la cuenta"),
            @ApiResponse(responseCode = "404", description = "Cuenta bancaria o producto en catálogo no encontrado"),
            @ApiResponse(responseCode = "409", description = "Solicitud duplicada (idempotencia) o conflicto de concurrencia"),
            @ApiResponse(responseCode = "502", description = "Error determinístico en comunicación con el proveedor externo"),
            @ApiResponse(responseCode = "504", description = "Timeout del proveedor externo; operación en estado PENDIENTE_CONCILIACION")
    })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PagoServicioResponse> procesarPago(
            @Valid @RequestBody PagoServicioRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Solicitud recibida en POST /pagos para servicio={}, producto={}, monto={}",
                request.getIdServicio(), request.getIdProducto(), request.getPrecio());
        PagoServicioResponse response = pagoServicioService.procesarPagoServicio(request, authorizationHeader, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Consultar estado de una transacción de pago",
            description = "Permite verificar el estado actual de una operación (EN_PROCESO, EXITOSA, PENDIENTE_CONCILIACION, REVERSADA) antes de intentar cualquier reintento o conciliación."
    )
    @GetMapping(value = "/{idTransaccion}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoServicioResponse> consultarEstadoTransaccion(
            @PathVariable("idTransaccion") Integer idTransaccion) {
        PagoServicioResponse response = pagoServicioService.consultarEstadoTransaccion(idTransaccion);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Conciliar una transacción pendiente por timeout",
            description = "Resuelve una transacción en estado PENDIENTE_CONCILIACION tras verificar con el proveedor si el cargo fue procesado (confirmando como EXITOSA) o no procesado (ejecutando el reverso del saldo a la cuenta del cliente)."
    )
    @PostMapping(
            value = "/{idTransaccion}/conciliar",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PagoServicioResponse> conciliarTransaccion(
            @PathVariable("idTransaccion") Integer idTransaccion,
            @Valid @RequestBody ConciliacionRequest request) {
        log.info("Solicitud de conciliación recibida para transacción ID={}", idTransaccion);
        PagoServicioResponse response = pagoServicioService.conciliarTransaccion(idTransaccion, request);
        return ResponseEntity.ok(response);
    }
}
