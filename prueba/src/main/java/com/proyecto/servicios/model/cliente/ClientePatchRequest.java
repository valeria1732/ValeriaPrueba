package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Campos opcionales para actualización parcial de cliente (no permite CURP ni RFC)")
public class ClientePatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    @Schema(description = "Nombre", example = "Mariana")
    private String nombre;

    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El segundo nombre solo puede contener letras y espacios")
    @Schema(description = "Segundo nombre", example = "Sofia")
    private String segundoNombre;

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido paterno solo puede contener letras y espacios")
    @Schema(description = "Apellido paterno", example = "Hernandez")
    private String apellidoPaterno;

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido materno solo puede contener letras y espacios")
    @Schema(description = "Apellido materno", example = "Torres")
    private String apellidoMaterno;

    @Schema(description = "Fecha de nacimiento", example = "1994-08-22")
    private LocalDate fechaNacimiento;

    @Schema(description = "Sexo", example = "FEMENINO")
    private String sexo;

    @Schema(description = "Nacionalidad", example = "Mexicana")
    private String nacionalidad;

    @Schema(description = "Estado civil", example = "SOLTERO")
    private String estadoCivil;

    @Pattern(regexp = "^\\d{10}$", message = "El teléfono móvil debe contener 10 dígitos numéricos")
    @Schema(description = "Teléfono celular", example = "5598765432")
    private String telefonoMovil;

    @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono alternativo debe contener 10 dígitos numéricos")
    @Schema(description = "Teléfono alternativo", example = "5511223344")
    private String telefonoAlternativo;

    @Valid
    @Schema(description = "Domicilio")
    private DomicilioDTO domicilio;

    @Schema(description = "Ocupación", example = "Gerente de Sistemas")
    private String ocupacion;

    @Schema(description = "Empresa", example = "Fintech Group")
    private String empresa;

    @DecimalMin(value = "0.01", inclusive = true, message = "El ingreso mensual debe ser mayor a cero")
    @Schema(description = "Ingreso mensual", example = "60000.00")
    private BigDecimal ingresoMensual;
}
