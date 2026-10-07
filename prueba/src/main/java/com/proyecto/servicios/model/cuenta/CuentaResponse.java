package com.proyecto.servicios.model.cuenta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle de una cuenta bancaria")
public class CuentaResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Identificador único de la cuenta", example = "1")
    private Integer id;

    @Schema(description = "Identificador del cliente titular", example = "1")
    private Integer clienteId;

    @Schema(description = "Número de cuenta único (10 dígitos)", example = "1234567890")
    private String numeroCuenta;

    @Schema(description = "CLABE Interbancaria (18 dígitos)", example = "012180012345678901")
    private String clabe;

    @Schema(description = "Saldo disponible", example = "1000.00")
    private BigDecimal saldo;

    @Schema(description = "Estatus de la cuenta (ACTIVA, INACTIVA, CANCELADA)", example = "ACTIVA")
    private String estatus;

    @Schema(description = "Fecha de apertura", example = "2026-10-06T19:00:00")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de última modificación", example = "2026-10-06T19:00:00")
    private LocalDateTime fechaActualizacion;
}
