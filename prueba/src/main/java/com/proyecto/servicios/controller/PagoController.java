package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.pago.ConciliacionRequest;
import com.proyecto.servicios.model.pago.PagoServicioRequest;
import com.proyecto.servicios.model.pago.PagoServicioResponse;
import com.proyecto.servicios.service.PagoServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos y Transacciones", description = "Procesamiento seguro de pagos GestoPago (SendTx), idempotencia y conciliación")
public class PagoController {

    private final PagoServicioService pagoServicioService;

    @Operation(summary = "Procesar pago de servicio (Flujo SendTx)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoServicioResponse> procesarPago(
            @Valid @RequestBody PagoServicioRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(pagoServicioService.procesarPagoServicio(request, authorizationHeader, idempotencyKey));
    }

    @Operation(summary = "Consultar estado de una transacción de pago")
    @GetMapping(value = "/{idTransaccion}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoServicioResponse> consultarEstadoTransaccion(@PathVariable("idTransaccion") Integer idTransaccion) {
        return ResponseEntity.ok(pagoServicioService.consultarEstadoTransaccion(idTransaccion));
    }

    @Operation(summary = "Conciliar una transacción pendiente por timeout")
    @PostMapping(value = "/{idTransaccion}/conciliar", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoServicioResponse> conciliarTransaccion(
            @PathVariable("idTransaccion") Integer idTransaccion,
            @Valid @RequestBody ConciliacionRequest request) {
        return ResponseEntity.ok(pagoServicioService.conciliarTransaccion(idTransaccion, request));
    }
}
