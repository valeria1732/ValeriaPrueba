package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.cuenta.CuentaActualizaRequest;
import com.proyecto.servicios.model.cuenta.CuentaCreacionRequest;
import com.proyecto.servicios.model.cuenta.CuentaResponse;
import com.proyecto.servicios.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cuentas")
@Tag(name = "Cuentas Bancarias", description = "Operaciones para creación, consulta de saldo, filtros y actualización de cuentas bancarias")
public class CuentaController {

    private final CuentaService cuentaService;

    @Autowired
    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Crear una cuenta asociada a un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Saldo inicial negativo o cliente inactivo"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CuentaResponse> crearCuenta(@Valid @RequestBody CuentaCreacionRequest request) {
        log.info("Petición recibida en POST /cuentas para clienteId: {}", request.getClienteId());
        CuentaResponse response = cuentaService.crearCuenta(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar cuentas asociadas a un cliente o filtradas por estatus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de cuentas obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado al filtrar por clienteId")
    })
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<CuentaResponse>> consultarCuentas(
            @Parameter(description = "Identificador del cliente", example = "1")
            @RequestParam(name = "clienteId", required = false) Integer clienteId,
            @Parameter(description = "Estatus de la cuenta (ACTIVA, INACTIVA, etc.)", example = "ACTIVA")
            @RequestParam(name = "estatus", required = false) String estatus) {
        log.info("Petición recibida en GET /cuentas con clienteId: {}, estatus: {}", clienteId, estatus);

        if (clienteId != null) {
            return ResponseEntity.ok(cuentaService.consultarPorClienteId(clienteId));
        }

        if (estatus != null && !estatus.trim().isEmpty()) {
            return ResponseEntity.ok(cuentaService.consultarPorEstatus(estatus));
        }

        // Si no se especifica filtro, devuelve cuentas activas por defecto
        return ResponseEntity.ok(cuentaService.consultarPorEstatus("ACTIVA"));
    }

    @Operation(summary = "Consultar una cuenta por número de cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping(
            value = "/{numeroCuenta}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CuentaResponse> consultarPorNumeroCuenta(
            @Parameter(description = "Número único de la cuenta", example = "1234567890")
            @PathVariable("numeroCuenta") String numeroCuenta) {
        log.info("Petición recibida en GET /cuentas/{}", numeroCuenta);
        CuentaResponse response = cuentaService.consultarPorNumeroCuenta(numeroCuenta);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consultar saldo disponible de una cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saldo obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping(
            value = "/{numeroCuenta}/saldo",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> consultarSaldo(
            @Parameter(description = "Número único de la cuenta", example = "1234567890")
            @PathVariable("numeroCuenta") String numeroCuenta) {
        log.info("Petición recibida en GET /cuentas/{}/saldo", numeroCuenta);
        BigDecimal saldo = cuentaService.consultarSaldo(numeroCuenta);
        Map<String, Object> body = new HashMap<>();
        body.put("numeroCuenta", numeroCuenta);
        body.put("saldo", saldo);
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Actualizar parcialmente la información de una cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Estatus inválido o cliente inactivo"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PatchMapping(
            value = "/{numeroCuenta}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CuentaResponse> actualizarParcial(
            @Parameter(description = "Número único de la cuenta", example = "1234567890")
            @PathVariable("numeroCuenta") String numeroCuenta,
            @Valid @RequestBody CuentaActualizaRequest request) {
        log.info("Petición recibida en PATCH /cuentas/{}", numeroCuenta);
        CuentaResponse response = cuentaService.actualizarParcial(numeroCuenta, request);
        return ResponseEntity.ok(response);
    }
}
