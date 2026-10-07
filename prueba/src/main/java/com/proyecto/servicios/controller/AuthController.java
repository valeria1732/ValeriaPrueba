package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.usuario.LoginRequest;
import com.proyecto.servicios.model.usuario.LoginResponse;
import com.proyecto.servicios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y generación de token de seguridad JWT")
public class AuthController {

    private final UsuarioService usuarioService;

    @Autowired
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Iniciar sesión y obtener token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa y token JWT generado"),
            @ApiResponse(responseCode = "400", description = "Formato de credenciales inválido"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas"),
            @ApiResponse(responseCode = "403", description = "Usuario inactivo"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Petición recibida en POST /auth/login para: {}", request.getCorreo());
        LoginResponse response = usuarioService.login(request);
        return ResponseEntity.ok(response);
    }
}
