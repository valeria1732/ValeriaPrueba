package com.proyecto.servicios.client.decoder;

import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GestoPagoCustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        log.error("Error devuelto por servicio externo en método {}: status={}", methodKey, status);

        switch (status) {
            case 401:
            case 403:
                return new GestoPagoAuthException("Autenticación fallida o token inválido al invocar el servicio externo (HTTP " + status + ")");
            case 408:
            case 504:
                return new GestoPagoTimeoutException("Tiempo de espera agotado al conectar con el servicio externo (HTTP " + status + ")");
            case 404:
                return new GestoPagoIntegrationException("El recurso solicitado no fue encontrado en el servicio externo (HTTP 404)", 404);
            case 500:
            case 502:
            case 503:
                return new GestoPagoIntegrationException("Error interno en el servidor del servicio externo (HTTP " + status + ")", status);
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
