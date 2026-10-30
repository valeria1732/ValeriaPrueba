package com.proyecto.servicios.model.cuenta;

import com.proyecto.servicios.exception.ClienteBusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Schema(description = "Solicitud para apertura de una nueva cuenta bancaria")
public record CuentaCreacionRequest(
        @NotNull(message = "El clienteId es obligatorio")
        @Schema(description = "Identificador del cliente titular de la cuenta", example = "1")
        Integer clienteId,

        @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
        @Schema(description = "Saldo inicial de apertura", example = "1000.00")
        BigDecimal saldoInicial
) implements Serializable {

    public CuentaCreacionRequest {
        if (clienteId == null) {
            throw new IllegalArgumentException("El clienteId es obligatorio");
        }
        if (saldoInicial != null && saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new ClienteBusinessException("El saldo inicial no puede ser negativo");
        }
        if (saldoInicial == null) {
            saldoInicial = BigDecimal.ZERO;
        }
    }

    public Integer getClienteId() { return clienteId; }
    public BigDecimal getSaldoInicial() { return saldoInicial; }
}
