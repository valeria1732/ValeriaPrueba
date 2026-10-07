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
@Schema(description = "Solicitud completa de registro para Onboarding de Clientes Personas Físicas")
public class ClienteRegistroRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- Datos Personales ---

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    @Schema(description = "Nombre de la persona", example = "Mariana")
    private String nombre;

    @Size(max = 50, message = "El segundo nombre no debe exceder 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El segundo nombre solo puede contener letras y espacios")
    @Schema(description = "Segundo nombre (opcional)", example = "Sofia")
    private String segundoNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido paterno debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido paterno solo puede contener letras y espacios")
    @Schema(description = "Apellido paterno", example = "Hernandez")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido materno debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido materno solo puede contener letras y espacios")
    @Schema(description = "Apellido materno", example = "Torres")
    private String apellidoMaterno;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento no puede ser una fecha futura")
    @Schema(description = "Fecha de nacimiento (debe cumplir mayoría de edad: >= 18 años)", example = "1994-08-22")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La CURP es obligatoria")
    @Pattern(regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$", message = "La CURP debe contener 18 caracteres en formato oficial mexicano")
    @Schema(description = "CURP (18 caracteres)", example = "HETM940822MDFRRN03")
    private String curp;

    @NotBlank(message = "El RFC es obligatorio")
    @Pattern(regexp = "^[A-Z&Ñ]{3,4}\\d{6}[A-V1-9][A-Z1-9][0-9A]$", message = "El RFC debe contener 12 o 13 caracteres con formato oficial")
    @Schema(description = "RFC con homoclave (12 o 13 caracteres)", example = "HETM9408228K4")
    private String rfc;

    @NotBlank(message = "El sexo es obligatorio")
    @Schema(description = "Sexo", example = "FEMENINO")
    private String sexo;

    @NotBlank(message = "La nacionalidad es obligatoria")
    @Schema(description = "Nacionalidad", example = "Mexicana")
    @Builder.Default
    private String nacionalidad = "Mexicana";

    @NotBlank(message = "El estado civil es obligatorio")
    @Schema(description = "Estado civil", example = "SOLTERO")
    private String estadoCivil;

    // --- Datos de Contacto ---

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe tener un formato válido")
    @Size(max = 100, message = "El correo no debe exceder 100 caracteres")
    @Schema(description = "Correo electrónico único del cliente", example = "mariana.hernandez@example.com")
    private String correo;

    @NotBlank(message = "El teléfono móvil es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El teléfono móvil debe contener exactamente 10 dígitos numéricos")
    @Schema(description = "Teléfono celular a 10 dígitos", example = "5512345678")
    private String telefonoMovil;

    @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono alternativo debe contener exactamente 10 dígitos numéricos")
    @Schema(description = "Teléfono alternativo o fijo (opcional, 10 dígitos)", example = "5587654321")
    private String telefonoAlternativo;

    // --- Domicilio ---

    @NotNull(message = "La información del domicilio es obligatoria")
    @Valid
    @Schema(description = "Domicilio del cliente")
    private DomicilioDTO domicilio;

    // --- Información Laboral ---

    @NotBlank(message = "La ocupación es obligatoria")
    @Schema(description = "Ocupación o profesión", example = "Ingeniera de Software")
    private String ocupacion;

    @NotBlank(message = "La empresa es obligatoria")
    @Schema(description = "Empresa o centro de trabajo", example = "Tecnologías Financieras S.A.")
    private String empresa;

    @NotNull(message = "El ingreso mensual es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El ingreso mensual debe ser mayor a cero")
    @Schema(description = "Ingreso mensual comprobable (mayor a cero)", example = "45000.00")
    private BigDecimal ingresoMensual;

    // --- Seguridad y Acceso ---

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#\\-])[A-Za-z\\d@$!%*?&._#\\-]{8,}$",
            message = "La contraseña debe contener mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un carácter especial"
    )
    @Schema(description = "Contraseña de acceso para el usuario del cliente", example = "Segura123!")
    private String password;

    // --- Saldo Inicial Opcional para Cuenta Bancaria ---

    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
    @Schema(description = "Saldo inicial de apertura de la cuenta bancaria", example = "1000.00")
    @Builder.Default
    private BigDecimal saldoInicial = new BigDecimal("1000.00");
}
