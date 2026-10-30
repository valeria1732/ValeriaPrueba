package com.proyecto.servicios.model.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de conciliación para resolver transacciones en estado PENDIENTE_CONCILIACION")
public class ConciliacionRequest {

    @NotNull(message = "Debe especificar si la operación fue confirmada o rechazada por el proveedor")
    @Schema(description = "Indica si el proveedor GestoPago confirmó que la transacción fue procesada exitosamente", example = "true")
    private Boolean confirmadaPorProveedor;

    @Schema(description = "Folio o número de autorización reportado por el proveedor en la conciliación", example = "GP-CONC-998877")
    private String folioProveedor;

    @Schema(description = "Observación o motivo de la resolución de conciliación", example = "Verificado en portal de conciliación GestoPago")
    private String motivo;
}

