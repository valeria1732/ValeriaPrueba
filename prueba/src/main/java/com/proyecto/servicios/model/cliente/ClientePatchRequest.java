package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Schema(description = "Campos opcionales para actualización parcial de cliente (no permite CURP ni RFC)")
public record ClientePatchRequest(
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
        @Schema(description = "Nombre", example = "Mariana")
        String nombre,

        @Size(max = 50)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El segundo nombre solo puede contener letras y espacios")
        @Schema(description = "Segundo nombre", example = "Sofia")
        String segundoNombre,

        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido paterno solo puede contener letras y espacios")
        @Schema(description = "Apellido paterno", example = "Hernandez")
        String apellidoPaterno,

        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido materno solo puede contener letras y espacios")
        @Schema(description = "Apellido materno", example = "Torres")
        String apellidoMaterno,

        @Schema(description = "Fecha de nacimiento", example = "1994-08-22")
        LocalDate fechaNacimiento,

        @Schema(description = "Sexo", example = "FEMENINO")
        String sexo,

        @Schema(description = "Nacionalidad", example = "Mexicana")
        String nacionalidad,

        @Schema(description = "Estado civil", example = "SOLTERO")
        String estadoCivil,

        @Pattern(regexp = "^\\d{10}$", message = "El teléfono móvil debe contener 10 dígitos numéricos")
        @Schema(description = "Teléfono celular", example = "5598765432")
        String telefonoMovil,

        @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono alternativo debe contener 10 dígitos numéricos")
        @Schema(description = "Teléfono alternativo", example = "5511223344")
        String telefonoAlternativo,

        @Valid
        @Schema(description = "Domicilio")
        DomicilioDTO domicilio,

        @Schema(description = "Ocupación", example = "Gerente de Sistemas")
        String ocupacion,

        @Schema(description = "Empresa", example = "Fintech Group")
        String empresa,

        @DecimalMin(value = "0.01", inclusive = true, message = "El ingreso mensual debe ser mayor a cero")
        @Schema(description = "Ingreso mensual", example = "60000.00")
        BigDecimal ingresoMensual
) implements Serializable {

    public ClientePatchRequest {
        if (telefonoMovil != null && !telefonoMovil.isBlank() && !telefonoMovil.trim().matches("^\\d{10}$")) {
            throw new IllegalArgumentException("El teléfono móvil debe contener 10 dígitos numéricos");
        }
        if (telefonoAlternativo != null && !telefonoAlternativo.isBlank() && !telefonoAlternativo.trim().matches("^\\d{10}$")) {
            throw new IllegalArgumentException("El teléfono alternativo debe contener 10 dígitos numéricos");
        }
        if (ingresoMensual != null && ingresoMensual.compareTo(new BigDecimal("0.01")) < 0) {
            throw new IllegalArgumentException("El ingreso mensual debe ser mayor a cero");
        }
    }

    public String getNombre() { return nombre; }
    public String getSegundoNombre() { return segundoNombre; }
    public String getApellidoPaterno() { return apellidoPaterno; }
    public String getApellidoMaterno() { return apellidoMaterno; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getSexo() { return sexo; }
    public String getNacionalidad() { return nacionalidad; }
    public String getEstadoCivil() { return estadoCivil; }
    public String getTelefonoMovil() { return telefonoMovil; }
    public String getTelefonoAlternativo() { return telefonoAlternativo; }
    public DomicilioDTO getDomicilio() { return domicilio; }
    public String getOcupacion() { return ocupacion; }
    public String getEmpresa() { return empresa; }
    public BigDecimal getIngresoMensual() { return ingresoMensual; }
}
