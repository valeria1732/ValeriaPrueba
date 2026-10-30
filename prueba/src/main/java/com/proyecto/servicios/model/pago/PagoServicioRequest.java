package com.proyecto.servicios.model.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para procesamiento de pago de servicios y recargas (Flujo SendTx)")
public class PagoServicioRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El número de cuenta debe tener exactamente 10 dígitos numéricos")
    @Schema(description = "Número de cuenta bancaria del cliente emisor (10 dígitos)", example = "0604471442")
    private String numCuenta;

    @Schema(description = "Identificador opcional del cliente titular para validación cruzada de identidad", example = "1")
    private Integer clienteId;

    @NotNull(message = "El idServicio es obligatorio")
    @Positive(message = "El idServicio debe ser un número entero positivo")
    @Schema(description = "Identificador del servicio externo (ej. CFE, Telmex, etc.)", example = "101")
    private Integer idServicio;

    @NotNull(message = "El idProducto es obligatorio")
    @Positive(message = "El idProducto debe ser un número entero positivo")
    @Schema(description = "Identificador del producto en el catálogo de GestoPago", example = "202")
    private Integer idProducto;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio o monto a pagar debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    @Schema(description = "Monto a pagar del servicio", example = "250.00")
    private BigDecimal precio;

    @NotNull(message = "El identificador de distribuidor es obligatorio")
    @Positive(message = "El identificador de distribuidor debe ser un número entero positivo")
    @Schema(description = "ID del distribuidor registrado en GestoPago", example = "1001")
    private Integer distribuidor;

    @NotBlank(message = "El teléfono o referencia es obligatorio")
    @Pattern(regexp = "^[0-9A-Za-z\\-]{6,30}$", message = "El teléfono o referencia debe tener entre 6 y 30 caracteres alfanuméricos")
    @Schema(description = "Número telefónico (para tiempo aire) o referencia de pago del servicio", example = "5512345678")
    private String telefonoReferencia;

    @Schema(description = "Bandera de operación o tipo de cobro (opcional)", example = "0")
    @Builder.Default
    private String bandera = "0";

    @Size(max = 100, message = "La clave de idempotencia no puede exceder 100 caracteres")
    @Schema(description = "Clave única de idempotencia para prevenir cargos duplicados", example = "IDEMP-20261030-0001")
    private String idempotencyKey;

    @ToString.Exclude
    @NotBlank(message = "El PIN o pass seguro es obligatorio")
    @Schema(description = "Pass Seguro / PIN de autorización de la cuenta del cliente", example = "Password123!")
    private String pin;
}
