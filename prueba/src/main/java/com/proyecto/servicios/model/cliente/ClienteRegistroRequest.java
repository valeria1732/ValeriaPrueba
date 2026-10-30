package com.proyecto.servicios.model.cliente;

import com.proyecto.servicios.exception.ClienteBusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Builder
@Schema(description = "Solicitud completa de registro para Onboarding de Clientes Personas Físicas")
public record ClienteRegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
        @Schema(description = "Nombre de la persona", example = "Mariana")
        String nombre,

        @Size(max = 50, message = "El segundo nombre no debe exceder 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El segundo nombre solo puede contener letras y espacios")
        @Schema(description = "Segundo nombre (opcional)", example = "Sofia")
        String segundoNombre,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(min = 2, max = 50, message = "El apellido paterno debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido paterno solo puede contener letras y espacios")
        @Schema(description = "Apellido paterno", example = "Hernandez")
        String apellidoPaterno,

        @NotBlank(message = "El apellido materno es obligatorio")
        @Size(min = 2, max = 50, message = "El apellido materno debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido materno solo puede contener letras y espacios")
        @Schema(description = "Apellido materno", example = "Torres")
        String apellidoMaterno,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento no puede ser una fecha futura")
        @Schema(description = "Fecha de nacimiento (debe cumplir mayoría de edad: >= 18 años)", example = "1994-08-22")
        LocalDate fechaNacimiento,

        @NotBlank(message = "La CURP es obligatoria")
        @Pattern(regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$", message = "La CURP debe contener 18 caracteres en formato oficial mexicano")
        @Schema(description = "CURP (18 caracteres)", example = "HETM940822MDFRRN03")
        String curp,

        @NotBlank(message = "El RFC es obligatorio")
        @Pattern(regexp = "^[A-Z&Ñ]{3,4}\\d{6}[A-V1-9][A-Z1-9][0-9A]$", message = "El RFC debe contener 12 o 13 caracteres con formato oficial")
        @Schema(description = "RFC con homoclave (12 o 13 caracteres)", example = "HETM9408228K4")
        String rfc,

        @NotBlank(message = "El sexo es obligatorio")
        @Schema(description = "Sexo", example = "FEMENINO")
        String sexo,

        @Schema(description = "Nacionalidad", example = "Mexicana")
        String nacionalidad,

        @NotBlank(message = "El estado civil es obligatorio")
        @Schema(description = "Estado civil", example = "SOLTERO")
        String estadoCivil,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico debe tener un formato válido")
        @Size(max = 100, message = "El correo no debe exceder 100 caracteres")
        @Schema(description = "Correo electrónico único del cliente", example = "mariana.hernandez@example.com")
        String correo,

        @NotBlank(message = "El teléfono móvil es obligatorio")
        @Pattern(regexp = "^\\d{10}$", message = "El teléfono móvil debe contener exactamente 10 dígitos numéricos")
        @Schema(description = "Teléfono celular a 10 dígitos", example = "5512345678")
        String telefonoMovil,

        @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono alternativo debe contener exactamente 10 dígitos numéricos")
        @Schema(description = "Teléfono alternativo o fijo (opcional, 10 dígitos)", example = "5587654321")
        String telefonoAlternativo,

        @NotNull(message = "La información del domicilio es obligatoria")
        @Valid
        @Schema(description = "Domicilio del cliente")
        DomicilioDTO domicilio,

        @NotBlank(message = "La ocupación es obligatoria")
        @Schema(description = "Ocupación o profesión", example = "Ingeniera de Software")
        String ocupacion,

        @NotBlank(message = "La empresa es obligatoria")
        @Schema(description = "Empresa o centro de trabajo", example = "Tecnologías Financieras S.A.")
        String empresa,

        @NotNull(message = "El ingreso mensual es obligatorio")
        @DecimalMin(value = "0.01", inclusive = true, message = "El ingreso mensual debe ser mayor a cero")
        @Schema(description = "Ingreso mensual comprobable (mayor a cero)", example = "45000.00")
        BigDecimal ingresoMensual,

        @NotBlank(message = "La contraseña es obligatoria")
        @Schema(description = "Contraseña de acceso para el usuario del cliente", example = "Segura123!")
        String password,

        @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
        @Schema(description = "Saldo inicial de apertura de la cuenta bancaria", example = "1000.00")
        BigDecimal saldoInicial
) implements Serializable {

    public ClienteRegistroRequest {
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
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser una fecha futura");
        }
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < 18) {
            throw new ClienteBusinessException("El cliente debe ser mayor de edad para registrarse (edad actual: " + edad + " años)");
        }
        if (curp == null || !curp.trim().toUpperCase().matches("^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$")) {
            throw new IllegalArgumentException("La CURP debe contener 18 caracteres en formato oficial mexicano");
        }
        if (rfc == null || !rfc.trim().toUpperCase().matches("^[A-Z&Ñ]{3,4}\\d{6}[A-V1-9][A-Z1-9][0-9A]$")) {
            throw new IllegalArgumentException("El RFC debe contener 12 o 13 caracteres con formato oficial");
        }
        if (sexo == null || sexo.isBlank()) {
            throw new IllegalArgumentException("El sexo es obligatorio");
        }
        if (estadoCivil == null || estadoCivil.isBlank()) {
            throw new IllegalArgumentException("El estado civil es obligatorio");
        }
        if (correo == null || !correo.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("El correo electrónico debe tener un formato válido");
        }
        if (telefonoMovil == null || !telefonoMovil.trim().matches("^\\d{10}$")) {
            throw new IllegalArgumentException("El teléfono móvil debe contener exactamente 10 dígitos numéricos");
        }
        if (domicilio == null) {
            throw new IllegalArgumentException("La información del domicilio es obligatoria");
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
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe contener mínimo 8 caracteres");
        }
        if (saldoInicial != null && saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new ClienteBusinessException("El saldo inicial no puede ser negativo");
        }
        if (saldoInicial == null) {
            saldoInicial = new BigDecimal("1000.00");
        }
        if (nacionalidad == null || nacionalidad.isBlank()) {
            nacionalidad = "Mexicana";
        }
    }

    public String getNombre() { return nombre; }
    public String getSegundoNombre() { return segundoNombre; }
    public String getApellidoPaterno() { return apellidoPaterno; }
    public String getApellidoMaterno() { return apellidoMaterno; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getCurp() { return curp; }
    public String getRfc() { return rfc; }
    public String getSexo() { return sexo; }
    public String getNacionalidad() { return nacionalidad; }
    public String getEstadoCivil() { return estadoCivil; }
    public String getCorreo() { return correo; }
    public String getTelefonoMovil() { return telefonoMovil; }
    public String getTelefonoAlternativo() { return telefonoAlternativo; }
    public DomicilioDTO getDomicilio() { return domicilio; }
    public String getOcupacion() { return ocupacion; }
    public String getEmpresa() { return empresa; }
    public BigDecimal getIngresoMensual() { return ingresoMensual; }
    public String getPassword() { return password; }
    public BigDecimal getSaldoInicial() { return saldoInicial; }
}
