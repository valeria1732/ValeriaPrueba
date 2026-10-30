package com.proyecto.servicios.model.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.io.Serializable;

@Builder
@Schema(description = "Credenciales requeridas para inicio de sesión")
public record LoginRequest(
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo debe tener un formato válido")
        @Schema(description = "Correo electrónico registrado", example = "mariana.hernandez@example.com")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Schema(description = "Contraseña de acceso", example = "Segura123!")
        String password
) implements Serializable {

    public LoginRequest {
        if (correo == null || !correo.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("El correo debe tener un formato válido");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
    }

    public String getCorreo() { return correo; }
    public String getPassword() { return password; }
}
