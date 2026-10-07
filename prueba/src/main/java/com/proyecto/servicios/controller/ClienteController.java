package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClientePatchRequest;
import com.proyecto.servicios.model.cliente.ClienteRegistroRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.service.ClienteService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Operaciones de registro, consultas con filtros, actualización parcial y baja lógica de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(
            summary = "Registrar un nuevo cliente",
            description = "Captura información personal, de contacto, domicilio y laboral, valida reglas de negocio y crea automáticamente su cuenta bancaria con saldo inicial"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente y cuenta bancaria creados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o reglas de negocio incumplidas (CURP/RFC duplicados, menor de edad)")
    })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteResponse> registrarCliente(@Valid @RequestBody ClienteRegistroRequest request) {
        log.info("Petición recibida en POST /clientes para CURP: {}", request.getCurp());
        ClienteResponse response = clienteService.registrarCliente(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar un cliente por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteResponse> consultarPorId(
            @Parameter(description = "Identificador único del cliente", example = "1")
            @PathVariable("id") Integer id) {
        log.info("Petición recibida en GET /clientes/{}", id);
        ClienteResponse response = clienteService.consultarPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consultar clientes con filtros opcionales")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes que coinciden con los criterios de búsqueda")
    })
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<ClienteResponse>> consultarClientes(
            @Parameter(description = "Filtrar por coincidencia en nombre")
            @RequestParam(name = "nombre", required = false) String nombre,
            @Parameter(description = "Filtrar por CURP exacto")
            @RequestParam(name = "curp", required = false) String curp,
            @Parameter(description = "Filtrar por RFC exacto")
            @RequestParam(name = "rfc", required = false) String rfc,
            @Parameter(description = "Filtrar por estatus activo")
            @RequestParam(name = "activo", required = false) Boolean activo) {
        log.info("Petición recibida en GET /clientes con filtros");
        List<ClienteResponse> clientes = clienteService.consultarClientes(nombre, curp, rfc, activo);
        return ResponseEntity.ok(clientes);
    }

    @Operation(summary = "Actualizar información de un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @Parameter(description = "Identificador único del cliente", example = "1")
            @PathVariable("id") Integer id,
            @Valid @RequestBody ClienteActualizaRequest request) {
        log.info("Petición recibida en PUT /clientes/{}", id);
        ClienteResponse response = clienteService.actualizarCliente(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar parcialmente la información de un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campos del cliente actualizados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PatchMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteResponse> actualizarParcial(
            @Parameter(description = "Identificador único del cliente", example = "1")
            @PathVariable("id") Integer id,
            @Valid @RequestBody ClientePatchRequest request) {
        log.info("Petición recibida en PATCH /clientes/{}", id);
        ClienteResponse response = clienteService.actualizarParcial(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Baja lógica de cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Baja lógica efectuada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> bajaLogica(
            @Parameter(description = "Identificador único del cliente", example = "1")
            @PathVariable("id") Integer id) {
        log.info("Petición recibida en DELETE /clientes/{}", id);
        clienteService.bajaLogica(id);
        return ResponseEntity.noContent().build();
    }
}
