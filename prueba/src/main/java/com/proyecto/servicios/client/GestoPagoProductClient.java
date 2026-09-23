package com.proyecto.servicios.client;

import com.proyecto.servicios.config.GestoPagoFeignConfig;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "gestoPagoProductClient",
        url = "${gestopago.product.url}",
        configuration = GestoPagoFeignConfig.class
)
public interface GestoPagoProductClient {

    @GetMapping(
            value = "/sistema/service/getProductList.do",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    GestoPagoProductListResponse getProductList(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    );
}
