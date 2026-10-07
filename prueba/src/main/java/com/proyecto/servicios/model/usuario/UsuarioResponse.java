package com.proyecto.servicios.model.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información del usuario de acceso al sistema")
public class UsuarioResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Identificador único del usuario", example = "1")
    private Integer id;

    @Schema(description = "Identificador del cliente asociado", example = "1")
    private Integer clienteId;

    @Schema(description = "Correo electrónico de acceso", example = "mariana.hernandez@example.com")
    private String correo;

    @Schema(description = "Estatus activo del usuario", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha de creación del usuario", example = "2026-10-06T19:00:00")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de última modificación", example = "2026-10-06T19:00:00")
    private LocalDateTime fechaActualizacion;
}
