package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.io.Serializable;

@Builder
@Schema(description = "Información del domicilio del cliente")
public record DomicilioDTO(
        @NotBlank(message = "La calle es obligatoria")
        @Schema(description = "Calle", example = "Av. Insurgentes Sur")
        String calle,

        @NotBlank(message = "El número exterior es obligatorio")
        @Schema(description = "Número exterior", example = "1200")
        String numeroExterior,

        @Schema(description = "Número interior", example = "Piso 4, Depto B")
        String numeroInterior,

        @NotBlank(message = "La colonia es obligatoria")
        @Schema(description = "Colonia", example = "Del Valle")
        String colonia,

        @NotBlank(message = "El municipio o alcaldía es obligatorio")
        @Schema(description = "Municipio o alcaldía", example = "Benito Juárez")
        String municipio,

        @NotBlank(message = "El estado es obligatorio")
        @Schema(description = "Estado", example = "Ciudad de México")
        String estado,

        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(regexp = "^\\d{5}$", message = "El código postal debe contener exactamente 5 dígitos numéricos")
        @Schema(description = "Código postal de 5 dígitos", example = "03100")
        String codigoPostal,

        @Schema(description = "País", example = "México")
        String pais
) implements Serializable {

    public DomicilioDTO {
        if (calle == null || calle.isBlank()) {
            throw new IllegalArgumentException("La calle es obligatoria");
        }
        if (numeroExterior == null || numeroExterior.isBlank()) {
            throw new IllegalArgumentException("El número exterior es obligatorio");
        }
        if (colonia == null || colonia.isBlank()) {
            throw new IllegalArgumentException("La colonia es obligatoria");
        }
        if (municipio == null || municipio.isBlank()) {
            throw new IllegalArgumentException("El municipio o alcaldía es obligatorio");
        }
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        if (codigoPostal == null || !codigoPostal.matches("^\\d{5}$")) {
            throw new IllegalArgumentException("El código postal debe contener exactamente 5 dígitos numéricos");
        }
        if (pais == null || pais.isBlank()) {
            pais = "México";
        }
    }

    public String getCalle() { return calle; }
    public String getNumeroExterior() { return numeroExterior; }
    public String getNumeroInterior() { return numeroInterior; }
    public String getColonia() { return colonia; }
    public String getMunicipio() { return municipio; }
    public String getEstado() { return estado; }
    public String getCodigoPostal() { return codigoPostal; }
    public String getPais() { return pais; }
}
