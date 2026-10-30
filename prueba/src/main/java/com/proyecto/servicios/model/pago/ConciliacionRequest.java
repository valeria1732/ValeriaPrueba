package com.proyecto.servicios.model.pago;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConciliacionRequest {

    @NotNull(message = "Debe especificar si la operación fue confirmada o rechazada por el proveedor")
    private Boolean confirmadaPorProveedor;

    private String folioProveedor;
    private String motivo;
}
