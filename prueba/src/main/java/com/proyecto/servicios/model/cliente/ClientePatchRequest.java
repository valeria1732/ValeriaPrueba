package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Campos opcionales para actualización parcial de cliente")
public class ClientePatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Primer nombre", example = "Mariana")
    private String nombre;

    @Schema(description = "Segundo nombre", example = "Sofia")
    private String segundoNombre;

    @Schema(description = "Apellido paterno", example = "Hernandez")
    private String apellidoPaterno;

    @Schema(description = "Apellido materno", example = "Torres")
    private String apellidoMaterno;

    @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe contener 10 dígitos numéricos")
    @Schema(description = "Teléfono celular", example = "5598765432")
    private String telefono;

    @Email(message = "El formato de correo electrónico es inválido")
    @Schema(description = "Correo electrónico", example = "m.hernandez.nuevo@example.com")
    private String email;

    @Schema(description = "Calle", example = "Av. Paseo de la Reforma")
    private String calle;

    @Schema(description = "Número exterior", example = "222")
    private String numeroExterior;

    @Schema(description = "Colonia", example = "Juárez")
    private String colonia;

    @Pattern(regexp = "^\\d{5}$", message = "El código postal debe tener 5 dígitos")
    @Schema(description = "Código postal", example = "06600")
    private String codigoPostal;

    @Schema(description = "Ciudad", example = "Ciudad de México")
    private String ciudad;

    @Schema(description = "Estado", example = "CDMX")
    private String estado;

    @Schema(description = "Puesto laboral", example = "Gerente de Sistemas")
    private String puestoLaboral;

    @Schema(description = "Ingreso mensual", example = "50000.00")
    private BigDecimal ingresoMensual;
}
