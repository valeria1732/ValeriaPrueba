package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.usuario.UsuarioCreacionRequest;
import com.proyecto.servicios.model.usuario.UsuarioResponse;
import com.proyecto.servicios.service.UsuarioService;
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
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema y filtros")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Consultar usuarios aplicando filtros opcionales")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    })
    @GetMapping(
            value = "/filtro",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<UsuarioResponse>> consultarFiltro(
            @Parameter(description = "Filtro por correo electrónico", example = "mariana.hernandez@example.com")
            @RequestParam(name = "correo", required = false) String correo,
            @Parameter(description = "Filtro por estatus de activación", example = "true")
            @RequestParam(name = "activo", required = false) Boolean activo) {
        log.info("Petición recibida en GET /usuarios/filtro con correo: {}, activo: {}", correo, activo);
        List<UsuarioResponse> usuarios = usuarioService.consultarUsuariosConFiltro(correo, activo);
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Agregar un nuevo usuario de acceso para un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Contraseña débil o cliente ya tiene un usuario"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "409", description = "Correo electrónico ya registrado")
    })
    @PutMapping(
            value = "/agregar",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UsuarioResponse> agregarUsuario(@Valid @RequestBody UsuarioCreacionRequest request) {
        log.info("Petición recibida en PUT /usuarios/agregar para cliente ID: {}", request.getClienteId());
        UsuarioResponse response = usuarioService.agregarUsuario(request);
        return ResponseEntity.ok(response);
    }
}
