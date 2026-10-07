package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para actualización total de información de cliente")
public class ClienteActualizaRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Schema(description = "Primer nombre del cliente", example = "Mariana")
    private String nombre;

    @Size(max = 100)
    @Schema(description = "Segundo nombre del cliente", example = "Sofia")
    private String segundoNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 100)
    @Schema(description = "Apellido paterno del cliente", example = "Hernandez")
    private String apellidoPaterno;

    @Size(max = 100)
    @Schema(description = "Apellido materno del cliente", example = "Torres")
    private String apellidoMaterno;

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

    @Schema(description = "Puesto o cargo laboral", example = "Líder Técnico")
    private String puestoLaboral;

    @Schema(description = "Ingreso mensual estimado", example = "42000.00")
    private BigDecimal ingresoMensual;
}
