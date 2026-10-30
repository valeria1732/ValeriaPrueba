package com.proyecto.servicios.model.cuenta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Schema(description = "Solicitud para actualización de información de una cuenta bancaria")
public record CuentaActualizaRequest(
        @Pattern(regexp = "^(ACTIVA|INACTIVA|BLOQUEADA|CANCELADA)$", message = "Estatus no válido. Valores permitidos: ACTIVA, INACTIVA, BLOQUEADA, CANCELADA")
        @Schema(description = "Estatus de la cuenta", example = "INACTIVA")
        String estatus,

        @Schema(description = "Ajuste o actualización de saldo (opcional)", example = "1500.00")
        BigDecimal saldo
) implements Serializable {

    public CuentaActualizaRequest {
        if (estatus != null && !estatus.trim().matches("^(ACTIVA|INACTIVA|BLOQUEADA|CANCELADA)$")) {
            throw new IllegalArgumentException("Estatus no válido. Valores permitidos: ACTIVA, INACTIVA, BLOQUEADA, CANCELADA");
        }
        if (saldo != null && saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo");
        }
    }

    public String getEstatus() { return estatus; }
    public BigDecimal getSaldo() { return saldo; }
}
