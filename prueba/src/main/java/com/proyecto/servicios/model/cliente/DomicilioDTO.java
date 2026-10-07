package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información del domicilio del cliente")
public class DomicilioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "La calle es obligatoria")
    @Schema(description = "Calle", example = "Av. Insurgentes Sur")
    private String calle;

    @NotBlank(message = "El número exterior es obligatorio")
    @Schema(description = "Número exterior", example = "1200")
    private String numeroExterior;

    @Schema(description = "Número interior", example = "Piso 4, Depto B")
    private String numeroInterior;

    @NotBlank(message = "La colonia es obligatoria")
    @Schema(description = "Colonia", example = "Del Valle")
    private String colonia;

    @NotBlank(message = "El municipio o alcaldía es obligatorio")
    @Schema(description = "Municipio o alcaldía", example = "Benito Juárez")
    private String municipio;

    @NotBlank(message = "El estado es obligatorio")
    @Schema(description = "Estado", example = "Ciudad de México")
    private String estado;

    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^\\d{5}$", message = "El código postal debe contener exactamente 5 dígitos numéricos")
    @Schema(description = "Código postal de 5 dígitos", example = "03100")
    private String codigoPostal;

    @Schema(description = "País", example = "México")
    @Builder.Default
    private String pais = "México";
}
