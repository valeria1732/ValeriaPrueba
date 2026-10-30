package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.pago.ConciliacionRequest;
import com.proyecto.servicios.model.pago.PagoServicioRequest;
import com.proyecto.servicios.model.pago.PagoServicioResponse;
import com.proyecto.servicios.service.PagoServicioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PagoControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PagoServicioService pagoServicioService;

    @InjectMocks
    private PagoController pagoController;

    private PagoServicioRequest validRequest;
    private PagoServicioResponse successResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(pagoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        validRequest = PagoServicioRequest.builder()
                .numCuenta("0019547596")
                .clienteId(1)
                .idServicio(101)
                .idProducto(202)
                .precio(new BigDecimal("350.00"))
                .distribuidor(1001)
                .telefonoReferencia("5512345678")
                .bandera("0")
                .idempotencyKey("IDEMP-2026-01")
                .pin("Password123!")
                .build();

        successResponse = PagoServicioResponse.builder()
                .idTransaccion(1)
                .exitoso(true)
                .estatusTransaccion("EXITOSA")
                .codigo(0)
                .mensaje("Pago procesado exitosamente")
                .folioTransaccion("GP-TX-10001")
                .numeroCuenta("0019547596")
                .precioPagado(new BigDecimal("350.00"))
                .saldoAnterior(new BigDecimal("1500.00"))
                .saldoRestante(new BigDecimal("1150.00"))
                .telefonoReferencia("5512345678")
                .tokenUtilizado("SECRET_BEARER_TOKEN_SHOULD_NOT_SERIALIZE")
                .firmaAes("SECRET_AES_SIGNATURE_SHOULD_NOT_SERIALIZE")
                .fechaHora(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /pagos debe responder 200 OK y ocultar información sensible (tokenUtilizado y firmaAes)")
    void testProcesarPago_200_OK_SinExponerDatosSensibles() throws Exception {
        when(pagoServicioService.procesarPagoServicio(any(PagoServicioRequest.class), any(), any()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/pagos")
                        .header("X-Idempotency-Key", "IDEMP-2026-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.estatusTransaccion").value("EXITOSA"))
                .andExpect(jsonPath("$.folioTransaccion").value("GP-TX-10001"))
                .andExpect(jsonPath("$.tokenUtilizado").doesNotExist())
                .andExpect(jsonPath("$.firmaAes").doesNotExist())
                .andExpect(content().string(not(containsString("SECRET_BEARER_TOKEN_SHOULD_NOT_SERIALIZE"))));
    }

    @Test
    @DisplayName("POST /pagos con monto fuera de límites permitidos debe responder 400 Bad Request")
    void testProcesarPago_400_MontoInvalido() throws Exception {
        when(pagoServicioService.procesarPagoServicio(any(PagoServicioRequest.class), any(), any()))
                .thenThrow(new MontoInvalidoException("El monto solicitado excede el monto máximo permitido"));

        mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    @DisplayName("POST /pagos sin autorización sobre la cuenta debe responder 403 Forbidden")
    void testProcesarPago_403_OperacionNoAutorizada() throws Exception {
        when(pagoServicioService.procesarPagoServicio(any(PagoServicioRequest.class), any(), any()))
                .thenThrow(new OperacionNoAutorizadaException("El cliente no tiene autorización sobre la cuenta"));

        mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(403));
    }

    @Test
    @DisplayName("POST /pagos con solicitud duplicada debe responder 409 Conflict")
    void testProcesarPago_409_TransaccionDuplicada() throws Exception {
        when(pagoServicioService.procesarPagoServicio(any(PagoServicioRequest.class), any(), any()))
                .thenThrow(new TransaccionDuplicadaException("Solicitud duplicada detectada"));

        mockMvc.perform(post("/pagos")
                        .header("X-Idempotency-Key", "IDEMP-2026-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(409));
    }

    @Test
    @DisplayName("POST /pagos ante timeout del proveedor debe responder 504 Gateway Timeout con estado PENDIENTE_CONCILIACION")
    void testProcesarPago_504_TimeoutPendienteConciliacion() throws Exception {
        when(pagoServicioService.procesarPagoServicio(any(PagoServicioRequest.class), any(), any()))
                .thenThrow(new TransaccionPendienteConciliacionException(
                        "Operación en estado PENDIENTE_CONCILIACION con folio TX-12345", "TX-12345"));

        mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.codigo").value(504))
                .andExpect(jsonPath("$.mensaje", containsString("PENDIENTE_CONCILIACION")));
    }

    @Test
    @DisplayName("POST /pagos/{id}/conciliar debe resolver la transacción pendiente y responder 200 OK")
    void testConciliarTransaccion_200_OK() throws Exception {
        ConciliacionRequest request = ConciliacionRequest.builder()
                .confirmadaPorProveedor(true)
                .folioProveedor("GP-CONC-001")
                .motivo("Confirmado en portal del proveedor")
                .build();

        when(pagoServicioService.conciliarTransaccion(eq(1), any(ConciliacionRequest.class)))
                .thenReturn(successResponse);

        mockMvc.perform(post("/pagos/1/conciliar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estatusTransaccion").value("EXITOSA"));
    }

    @Test
    @DisplayName("GET /pagos/{id} debe consultar el estado actual de la transacción")
    void testConsultarEstadoTransaccion_200_OK() throws Exception {
        when(pagoServicioService.consultarEstadoTransaccion(1)).thenReturn(successResponse);

        mockMvc.perform(get("/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTransaccion").value(1))
                .andExpect(jsonPath("$.estatusTransaccion").value("EXITOSA"));
    }
}

