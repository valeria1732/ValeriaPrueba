package com.proyecto.servicios.model.pago;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoServicioRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El número de cuenta debe tener exactamente 10 dígitos numéricos")
    private String numCuenta;

    private Integer clienteId;

    @NotNull(message = "El idServicio es obligatorio")
    @Positive(message = "El idServicio debe ser un número entero positivo")
    private Integer idServicio;

    @NotNull(message = "El idProducto es obligatorio")
    @Positive(message = "El idProducto debe ser un número entero positivo")
    private Integer idProducto;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio o monto a pagar debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "El identificador de distribuidor es obligatorio")
    @Positive(message = "El identificador de distribuidor debe ser un número entero positivo")
    private Integer distribuidor;

    @NotBlank(message = "El teléfono o referencia es obligatorio")
    @Pattern(regexp = "^[0-9A-Za-z\\-]{6,30}$", message = "El teléfono o referencia debe tener entre 6 y 30 caracteres alfanuméricos")
    private String telefonoReferencia;

    @Builder.Default
    private String bandera = "0";

    @Size(max = 100, message = "La clave de idempotencia no puede exceder 100 caracteres")
    private String idempotencyKey;

    @ToString.Exclude
    @NotBlank(message = "El PIN o pass seguro es obligatorio")
    private String pin;
}
