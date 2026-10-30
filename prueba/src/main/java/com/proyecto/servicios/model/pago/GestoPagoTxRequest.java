package com.proyecto.servicios.model.pago;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GestoPagoTxRequest {

    @JsonProperty("payloadFirmado")
    private String payloadFirmado;

    @JsonProperty("token")
    private String token;

    @JsonProperty("idDistribuidor")
    private Integer idDistribuidor;

    @JsonProperty("codigoDispositivo")
    private String codigoDispositivo;

    @JsonProperty("bandera")
    private String bandera;
}
