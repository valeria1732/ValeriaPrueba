package com.proyecto.servicios.model.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de autenticación exitosa con Token JWT")
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Token de autenticación JWT generado", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Tipo de esquema de autorización", example = "Bearer")
    @Builder.Default
    private String tipoToken = "Bearer";

    @Schema(description = "Correo del usuario autenticado", example = "mariana.hernandez@example.com")
    private String correo;

    @Schema(description = "Identificador del cliente asociado", example = "1")
    private Integer clienteId;

    @Schema(description = "Tiempo de validez del token en milisegundos", example = "86400000")
    private Long expiraEnMs;
}
