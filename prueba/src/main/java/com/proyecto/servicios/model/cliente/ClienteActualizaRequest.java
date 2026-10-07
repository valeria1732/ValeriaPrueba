package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para actualización completa de información de cliente (excepto CURP y RFC)")
public class ClienteActualizaRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    @Schema(description = "Nombre", example = "Mariana")
    private String nombre;

    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El segundo nombre solo puede contener letras y espacios")
    @Schema(description = "Segundo nombre", example = "Sofia")
    private String segundoNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido paterno solo puede contener letras y espacios")
    @Schema(description = "Apellido paterno", example = "Hernandez")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido materno solo puede contener letras y espacios")
    @Schema(description = "Apellido materno", example = "Torres")
    private String apellidoMaterno;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @Schema(description = "Fecha de nacimiento", example = "1994-08-22")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El sexo es obligatorio")
    @Schema(description = "Sexo", example = "FEMENINO")
    private String sexo;

    @NotBlank(message = "La nacionalidad es obligatoria")
    @Schema(description = "Nacionalidad", example = "Mexicana")
    private String nacionalidad;

    @NotBlank(message = "El estado civil es obligatorio")
    @Schema(description = "Estado civil", example = "CASADO")
    private String estadoCivil;

    @NotBlank(message = "El teléfono móvil es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El teléfono móvil debe contener exactamente 10 dígitos numéricos")
    @Schema(description = "Teléfono móvil", example = "5512345678")
    private String telefonoMovil;

    @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono alternativo debe contener exactamente 10 dígitos numéricos")
    @Schema(description = "Teléfono alternativo", example = "5587654321")
    private String telefonoAlternativo;

    @Valid
    @Schema(description = "Domicilio")
    private DomicilioDTO domicilio;

    @NotBlank(message = "La ocupación es obligatoria")
    @Schema(description = "Ocupación", example = "Líder Técnico")
    private String ocupacion;

    @NotBlank(message = "La empresa es obligatoria")
    @Schema(description = "Empresa", example = "Fintech Innovations")
    private String empresa;

    @NotNull(message = "El ingreso mensual es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El ingreso mensual debe ser mayor a cero")
    @Schema(description = "Ingreso mensual", example = "50000.00")
    private BigDecimal ingresoMensual;
}
