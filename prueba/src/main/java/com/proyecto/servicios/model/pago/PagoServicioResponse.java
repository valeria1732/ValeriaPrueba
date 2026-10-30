package com.proyecto.servicios.model.pago;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta del procesamiento de pago de servicio y afectación de saldo")
public class PagoServicioResponse {

    @Schema(description = "Identificador interno de la transacción registrada", example = "1")
    private Integer idTransaccion;

    @Schema(description = "Indica si la transacción fue completada exitosamente", example = "true")
    private Boolean exitoso;

    @Schema(description = "Estatus operativo de la transacción (EXITOSA, PENDIENTE_CONCILIACION, REVERSADA)", example = "EXITOSA")
    private String estatusTransaccion;

    @Schema(description = "Código de respuesta de GestoPago (0 indica éxito)", example = "0")
    private Integer codigo;

    @Schema(description = "Descripción o mensaje del resultado de la transacción", example = "Transacción exitosa (Aprobada)")
    private String mensaje;

    @Schema(description = "Folio o número de autorización generado para la transacción", example = "GP-TX-1792541234567")
    private String folioTransaccion;

    @Schema(description = "Número de cuenta bancaria enmascarado o procesado", example = "0604471442")
    private String numeroCuenta;

    @Schema(description = "Monto pagado y descontado de la cuenta", example = "250.00")
    private BigDecimal precioPagado;

    @Schema(description = "Saldo disponible en la cuenta antes del pago", example = "1500.00")
    private BigDecimal saldoAnterior;

    @Schema(description = "Saldo restante en la cuenta tras el débito", example = "1250.00")
    private BigDecimal saldoRestante;

    @Schema(description = "Teléfono o referencia a la cual se aplicó el pago", example = "5512345678")
    private String telefonoReferencia;

    @JsonIgnore
    @ToString.Exclude
    private String firmaAes;

    @JsonIgnore
    @ToString.Exclude
    private String tokenUtilizado;

    @Schema(description = "Fecha y hora de ejecución de la transacción")
    private LocalDateTime fechaHora;
}
