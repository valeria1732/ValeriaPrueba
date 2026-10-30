package com.proyecto.servicios.client;

import com.proyecto.servicios.config.GestoPagoFeignConfig;
import com.proyecto.servicios.model.pago.GestoPagoTxRequest;
import com.proyecto.servicios.model.pago.GestoPagoTxResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "gestoPagoTransactionClient",
        url = "${gestopago.transaction.url:https://gestopago.portalventas.net}",
        configuration = GestoPagoFeignConfig.class
)
public interface GestoPagoTransactionClient {

    @PostMapping(
            value = "/sistema/service/sendTx.do",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    GestoPagoTxResponse sendTx(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody GestoPagoTxRequest request
    );
}
