package com.proyecto.servicios.model.cuenta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para apertura de una nueva cuenta bancaria")
public class CuentaCreacionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "El clienteId es obligatorio")
    @Schema(description = "Identificador del cliente titular de la cuenta", example = "1")
    private Integer clienteId;

    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
    @Schema(description = "Saldo inicial de apertura", example = "1000.00")
    @Builder.Default
    private BigDecimal saldoInicial = BigDecimal.ZERO;
}
