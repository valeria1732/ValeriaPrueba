package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle de la cuenta bancaria del cliente")
public class CuentaBancariaResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Identificador de la cuenta", example = "1")
    private Integer id;

    @Schema(description = "Número de cuenta asignado (10 dígitos)", example = "1234567890")
    private String numeroCuenta;

    @Schema(description = "CLABE Interbancaria (18 dígitos)", example = "012180012345678901")
    private String clabe;

    @Schema(description = "Saldo actual de la cuenta", example = "1000.00")
    private BigDecimal saldo;

    @Schema(description = "Estatus activo de la cuenta", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha de apertura", example = "2026-10-06T18:30:00")
    private LocalDateTime fechaCreacion;
}
