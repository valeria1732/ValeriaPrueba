package com.proyecto.servicios.model.pago;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoServicioResponse {

    private Integer idTransaccion;
    private Boolean exitoso;
    private String estatusTransaccion;
    private Integer codigo;
    private String mensaje;
    private String folioTransaccion;
    private String numeroCuenta;
    private BigDecimal precioPagado;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoRestante;
    private String telefonoReferencia;

    @JsonIgnore
    @ToString.Exclude
    private String firmaAes;

    @JsonIgnore
    @ToString.Exclude
    private String tokenUtilizado;

    private LocalDateTime fechaHora;
}
