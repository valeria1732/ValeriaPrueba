package com.proyecto.servicios.model.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credenciales requeridas para inicio de sesión")
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Schema(description = "Correo electrónico registrado", example = "mariana.hernandez@example.com")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña de acceso", example = "Segura123!")
    private String password;
}
