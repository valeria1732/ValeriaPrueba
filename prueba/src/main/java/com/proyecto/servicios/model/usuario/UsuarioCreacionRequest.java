package com.proyecto.servicios.model.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.io.Serializable;

@Builder
@Schema(description = "Datos para alta manual o adición de usuario de acceso")
public record UsuarioCreacionRequest(
        @NotNull(message = "El clienteId es obligatorio")
        @Schema(description = "Identificador del cliente asociado", example = "1")
        Integer clienteId,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo debe tener un formato válido")
        @Schema(description = "Correo electrónico del usuario", example = "mariana.hernandez@example.com")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#\\-])[A-Za-z\\d@$!%*?&._#\\-]{8,}$",
                message = "La contraseña debe contener mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un carácter especial"
        )
        @Schema(description = "Contraseña segura", example = "Segura123!")
        String password
) implements Serializable {

    public UsuarioCreacionRequest {
        if (clienteId == null) {
            throw new IllegalArgumentException("El clienteId es obligatorio");
        }
        if (correo == null || !correo.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("El correo debe tener un formato válido");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe contener mínimo 8 caracteres");
        }
    }

    public Integer getClienteId() { return clienteId; }
    public String getCorreo() { return correo; }
    public String getPassword() { return password; }
}
