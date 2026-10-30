package com.proyecto.servicios.model.pago;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Estructura interna del Payload que se firma y cifra con AES
 * según lo especificado en la arquitectura GestoPago.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GestoPagoTxPayload {

    @JsonProperty("idProducto")
    private Integer idProducto;

    @JsonProperty("idServicio")
    private Integer idServicio;

    @JsonProperty("telefonoReferencia")
    private String telefonoReferencia;

    @JsonProperty("horaLocal")
    private String horaLocal;

    @JsonProperty("montoPago")
    private BigDecimal montoPago;

    @JsonProperty("upc")
    private String upc;

    @JsonProperty("unidad")
    private Integer unidad;
}
