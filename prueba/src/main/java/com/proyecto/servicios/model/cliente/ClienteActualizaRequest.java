package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Schema(description = "Datos para actualización completa de información de cliente (excepto CURP y RFC)")
public record ClienteActualizaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
        @Schema(description = "Nombre", example = "Mariana")
        String nombre,

        @Size(max = 50)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El segundo nombre solo puede contener letras y espacios")
        @Schema(description = "Segundo nombre", example = "Sofia")
        String segundoNombre,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido paterno solo puede contener letras y espacios")
        @Schema(description = "Apellido paterno", example = "Hernandez")
        String apellidoPaterno,

        @NotBlank(message = "El apellido materno es obligatorio")
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido materno solo puede contener letras y espacios")
        @Schema(description = "Apellido materno", example = "Torres")
        String apellidoMaterno,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser en el pasado")
        @Schema(description = "Fecha de nacimiento", example = "1994-08-22")
        LocalDate fechaNacimiento,

        @NotBlank(message = "El sexo es obligatorio")
        @Schema(description = "Sexo", example = "FEMENINO")
        String sexo,

        @NotBlank(message = "La nacionalidad es obligatoria")
        @Schema(description = "Nacionalidad", example = "Mexicana")
        String nacionalidad,

        @NotBlank(message = "El estado civil es obligatorio")
        @Schema(description = "Estado civil", example = "CASADO")
        String estadoCivil,

        @NotBlank(message = "El teléfono móvil es obligatorio")
        @Pattern(regexp = "^\\d{10}$", message = "El teléfono móvil debe contener exactamente 10 dígitos numéricos")
        @Schema(description = "Teléfono móvil", example = "5512345678")
        String telefonoMovil,

        @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono alternativo debe contener exactamente 10 dígitos numéricos")
        @Schema(description = "Teléfono alternativo", example = "5587654321")
        String telefonoAlternativo,

        @Valid
        @Schema(description = "Domicilio")
        DomicilioDTO domicilio,

        @NotBlank(message = "La ocupación es obligatoria")
        @Schema(description = "Ocupación", example = "Líder Técnico")
        String ocupacion,

        @NotBlank(message = "La empresa es obligatoria")
        @Schema(description = "Empresa", example = "Fintech Innovations")
        String empresa,

        @NotNull(message = "El ingreso mensual es obligatorio")
        @DecimalMin(value = "0.01", inclusive = true, message = "El ingreso mensual debe ser mayor a cero")
        @Schema(description = "Ingreso mensual", example = "50000.00")
        BigDecimal ingresoMensual
) implements Serializable {

    public ClienteActualizaRequest {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (apellidoPaterno == null || apellidoPaterno.isBlank()) {
            throw new IllegalArgumentException("El apellido paterno es obligatorio");
        }
        if (apellidoMaterno == null || apellidoMaterno.isBlank()) {
            throw new IllegalArgumentException("El apellido materno es obligatorio");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        if (sexo == null || sexo.isBlank()) {
            throw new IllegalArgumentException("El sexo es obligatorio");
        }
        if (nacionalidad == null || nacionalidad.isBlank()) {
            nacionalidad = "Mexicana";
        }
        if (estadoCivil == null || estadoCivil.isBlank()) {
            throw new IllegalArgumentException("El estado civil es obligatorio");
        }
        if (telefonoMovil == null || !telefonoMovil.trim().matches("^\\d{10}$")) {
            throw new IllegalArgumentException("El teléfono móvil debe contener exactamente 10 dígitos numéricos");
        }
        if (ocupacion == null || ocupacion.isBlank()) {
            throw new IllegalArgumentException("La ocupación es obligatoria");
        }
        if (empresa == null || empresa.isBlank()) {
            throw new IllegalArgumentException("La empresa es obligatoria");
        }
        if (ingresoMensual == null || ingresoMensual.compareTo(new BigDecimal("0.01")) < 0) {
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
