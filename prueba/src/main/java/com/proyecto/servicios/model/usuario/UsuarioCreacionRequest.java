package com.proyecto.servicios.model.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para alta manual o adición de usuario de acceso")
public class UsuarioCreacionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "El clienteId es obligatorio")
    @Schema(description = "Identificador del cliente asociado", example = "1")
    private Integer clienteId;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Schema(description = "Correo electrónico del usuario", example = "mariana.hernandez@example.com")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#\\-])[A-Za-z\\d@$!%*?&._#\\-]{8,}$",
            message = "La contraseña debe contener mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un carácter especial"
    )
    @Schema(description = "Contraseña segura", example = "Segura123!")
    private String password;
}
