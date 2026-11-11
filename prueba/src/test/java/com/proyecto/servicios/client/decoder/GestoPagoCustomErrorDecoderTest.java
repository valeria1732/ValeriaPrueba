package com.proyecto.servicios.client.decoder;

import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class GestoPagoCustomErrorDecoderTest {

    private final GestoPagoCustomErrorDecoder decoder = new GestoPagoCustomErrorDecoder();

    private Response buildResponse(int status) {
        Request request = Request.create(
                Request.HttpMethod.POST,
                "/sistema/service/sendTx.do",
                new HashMap<>(),
                Request.Body.empty(),
                new RequestTemplate()
        );
        return Response.builder()
                .status(status)
                .reason("Error " + status)
                .headers(new HashMap<>())
                .body("error body", StandardCharsets.UTF_8)
                .request(request)
                .build();
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    @DisplayName("Debe mapear 401 y 403 a GestoPagoAuthException")
    void testDecode_AuthErrors(int status) {
        Exception ex = decoder.decode("GestoPagoClient#sendTx", buildResponse(status));
        assertInstanceOf(GestoPagoAuthException.class, ex);
    }

    @ParameterizedTest
    @ValueSource(ints = {408, 504})
    @DisplayName("Debe mapear 408 y 504 a GestoPagoTimeoutException")
    void testDecode_TimeoutErrors(int status) {
        Exception ex = decoder.decode("GestoPagoClient#sendTx", buildResponse(status));
        assertInstanceOf(GestoPagoTimeoutException.class, ex);
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503})
    @DisplayName("Debe mapear 500, 502 y 503 a GestoPagoIntegrationException")
    void testDecode_ServerErrors(int status) {
        Exception ex = decoder.decode("GestoPagoClient#sendTx", buildResponse(status));
        assertInstanceOf(GestoPagoIntegrationException.class, ex);
    }

    @Test
    @DisplayName("Debe envolver otros códigos HTTP en GestoPagoIntegrationException")
    void testDecode_DefaultFallback() {
        Exception ex = decoder.decode("GestoPagoClient#sendTx", buildResponse(404));
        assertInstanceOf(GestoPagoIntegrationException.class, ex);
    }
}

