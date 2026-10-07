package com.proyecto.servicios.model.cuenta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para actualización de información de una cuenta bancaria")
public class CuentaActualizaRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Pattern(regexp = "^(ACTIVA|INACTIVA|BLOQUEADA|CANCELADA)$", message = "Estatus no válido. Valores permitidos: ACTIVA, INACTIVA, BLOQUEADA, CANCELADA")
    @Schema(description = "Estatus de la cuenta", example = "INACTIVA")
    private String estatus;

    @Schema(description = "Ajuste o actualización de saldo (opcional)", example = "1500.00")
    private BigDecimal saldo;
}
