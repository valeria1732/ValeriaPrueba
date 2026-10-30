package com.proyecto.servicios.model.pago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GestoPagoTxResponse {

    @JsonProperty("codigo")
    private Integer codigo;

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("folio")
    private String folio;

    @JsonProperty("autorizacion")
    private String autorizacion;

    @JsonProperty("referencia")
    private String referencia;

    @JsonProperty("fecha")
    private LocalDateTime fecha;
}
