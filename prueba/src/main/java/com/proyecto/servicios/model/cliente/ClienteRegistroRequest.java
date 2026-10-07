package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para el registro de un nuevo cliente")
public class ClienteRegistroRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    @Schema(description = "Primer nombre del cliente", example = "Mariana")
    private String nombre;

    @Size(max = 100, message = "El segundo nombre no debe exceder 100 caracteres")
    @Schema(description = "Segundo nombre del cliente", example = "Sofia")
    private String segundoNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 100, message = "El apellido paterno no debe exceder 100 caracteres")
    @Schema(description = "Apellido paterno del cliente", example = "Hernandez")
    private String apellidoPaterno;

    @Size(max = 100, message = "El apellido materno no debe exceder 100 caracteres")
    @Schema(description = "Apellido materno del cliente", example = "Torres")
    private String apellidoMaterno;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @Schema(description = "Fecha de nacimiento en formato YYYY-MM-DD", example = "1994-08-22")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La CURP es obligatoria")
    @Pattern(regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$", message = "Formato de CURP inválido (18 caracteres)")
    @Schema(description = "Clave Única de Registro de Población", example = "HETM940822MDFRRN03")
    private String curp;

    @NotBlank(message = "El RFC es obligatorio")
    @Pattern(regexp = "^[A-Z&Ñ]{3,4}\\d{6}[A-V1-9][A-Z1-9][0-9A]$", message = "Formato de RFC inválido (12 o 13 caracteres)")
    @Schema(description = "Registro Federal de Contribuyentes", example = "HETM9408228K4")
    private String rfc;

    @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe contener 10 dígitos numéricos")
    @Schema(description = "Teléfono celular a 10 dígitos", example = "5512345678")
    private String telefono;

    @Email(message = "El formato de correo electrónico es inválido")
    @Schema(description = "Correo electrónico de contacto", example = "mariana.hernandez@example.com")
    private String email;

    @Schema(description = "Calle de domicilio", example = "Av. Insurgentes Sur")
    private String calle;

    @Schema(description = "Número exterior de domicilio", example = "1200")
    private String numeroExterior;

    @Schema(description = "Colonia de domicilio", example = "Del Valle")
    private String colonia;

    @Pattern(regexp = "^\\d{5}$", message = "El código postal debe tener 5 dígitos")
    @Schema(description = "Código postal", example = "03100")
    private String codigoPostal;

    @Schema(description = "Ciudad", example = "Ciudad de México")
    private String ciudad;

    @Schema(description = "Estado", example = "CDMX")
    private String estado;

    @Schema(description = "Puesto o cargo laboral", example = "Desarrollador de Software")
    private String puestoLaboral;

    @DecimalMin(value = "0.0", inclusive = true, message = "El ingreso mensual debe ser mayor o igual a 0")
    @Schema(description = "Ingreso mensual estimado", example = "35000.00")
    private BigDecimal ingresoMensual;

    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
    @Schema(description = "Saldo inicial para la apertura de cuenta bancaria", example = "1000.00")
    private BigDecimal saldoInicial;
}
