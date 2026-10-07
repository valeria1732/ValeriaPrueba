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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            description = "Captura información personal, de contacto, domicilio y laboral, valida reglas de negocio y crea automáticamente su cuenta bancaria y usuario con contraseña cifrada"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente, cuenta bancaria y usuario creados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o reglas de negocio incumplidas"),
            @ApiResponse(responseCode = "409", description = "CURP, RFC o Correo ya registrados")
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

    @Operation(summary = "Consultar clientes con filtros opcionales (nombre, apellidos, CURP, RFC, correo, activo, rango de fechas)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes que coinciden con los criterios de búsqueda")
    })
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<ClienteResponse>> consultarClientes(
            @Parameter(description = "Filtrar por coincidencia en nombre")
            @RequestParam(name = "nombre", required = false) String nombre,
            @Parameter(description = "Filtrar por coincidencia en apellido paterno")
            @RequestParam(name = "apellidoPaterno", required = false) String apellidoPaterno,
            @Parameter(description = "Filtrar por coincidencia en apellido materno")
            @RequestParam(name = "apellidoMaterno", required = false) String apellidoMaterno,
            @Parameter(description = "Filtrar por CURP exacto")
            @RequestParam(name = "curp", required = false) String curp,
            @Parameter(description = "Filtrar por RFC exacto")
            @RequestParam(name = "rfc", required = false) String rfc,
            @Parameter(description = "Filtrar por correo electrónico")
            @RequestParam(name = "correo", required = false) String correo,
            @Parameter(description = "Filtrar por estatus activo")
            @RequestParam(name = "activo", required = false) Boolean activo,
            @Parameter(description = "Fecha inicial de registro (YYYY-MM-DD)", example = "2026-01-01")
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha final de registro (YYYY-MM-DD)", example = "2026-12-31")
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        log.info("Petición recibida en GET /clientes con filtros - nombre: {}, apellidoPaterno: {}, curp: {}, rfc: {}, correo: {}, activo: {}",
                nombre, apellidoPaterno, curp, rfc, correo, activo);
        List<ClienteResponse> clientes = clienteService.consultarClientes(
                nombre, apellidoPaterno, apellidoMaterno, curp, rfc, correo, activo, fechaInicio, fechaFin
        );
        return ResponseEntity.ok(clientes);
    }

    @Operation(summary = "Consultar un cliente por CURP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping(
            value = "/curp/{curp}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteResponse> consultarPorCurp(
            @Parameter(description = "CURP del cliente", example = "HETM940822MDFRRN03")
            @PathVariable("curp") String curp) {
        log.info("Petición recibida en GET /clientes/curp/{}", curp);
        ClienteResponse response = clienteService.consultarPorCurp(curp);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consultar un cliente por RFC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping(
            value = "/rfc/{rfc}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteResponse> consultarPorRfc(
            @Parameter(description = "RFC del cliente", example = "HETM9408223R4")
            @PathVariable("rfc") String rfc) {
        log.info("Petición recibida en GET /clientes/rfc/{}", rfc);
        ClienteResponse response = clienteService.consultarPorRfc(rfc);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consultar un cliente por correo electrónico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping(
            value = "/correo/{correo}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteResponse> consultarPorCorreo(
            @Parameter(description = "Correo electrónico del cliente", example = "cliente@example.com")
            @PathVariable("correo") String correo) {
        log.info("Petición recibida en GET /clientes/correo/{}", correo);
        ClienteResponse response = clienteService.consultarPorCorreo(correo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consultar un cliente por número de cuenta bancaria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping(
            value = "/cuenta/{numeroCuenta}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteResponse> consultarPorNumeroCuenta(
            @Parameter(description = "Número de cuenta bancaria", example = "1234567890")
            @PathVariable("numeroCuenta") String numeroCuenta) {
        log.info("Petición recibida en GET /clientes/cuenta/{}", numeroCuenta);
        ClienteResponse response = clienteService.consultarPorNumeroCuenta(numeroCuenta);
        return ResponseEntity.ok(response);
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

    @Operation(summary = "Actualizar parcialmente la información de un cliente (no permite modificar CURP, RFC ni cuentas)")
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

    @Operation(summary = "Baja lógica de cliente (desactiva cliente, usuario y cuentas asociadas)")
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
