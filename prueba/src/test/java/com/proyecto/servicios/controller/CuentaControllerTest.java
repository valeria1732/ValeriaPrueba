package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.exception.CuentaNotFoundException;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.cuenta.CuentaActualizaRequest;
import com.proyecto.servicios.model.cuenta.CuentaCreacionRequest;
import com.proyecto.servicios.model.cuenta.CuentaResponse;
import com.proyecto.servicios.service.CuentaService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CuentaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CuentaService cuentaService;

    @InjectMocks
    private CuentaController cuentaController;

    private CuentaResponse mockCuentaResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(cuentaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockCuentaResponse = CuentaResponse.builder()
                .id(1)
                .clienteId(1)
                .numeroCuenta("1234567890")
                .clabe("012180123456789001")
                .saldo(new BigDecimal("1000.00"))
                .estatus("ACTIVA")
                .build();
    }

    @Test
    @DisplayName("POST /cuentas debe responder 201 Created al crear una cuenta")
    void testCrearCuenta_201_Created() throws Exception {
        CuentaCreacionRequest request = CuentaCreacionRequest.builder()
                .clienteId(1)
                .saldoInicial(new BigDecimal("1000.00"))
                .build();

        when(cuentaService.crearCuenta(any(CuentaCreacionRequest.class))).thenReturn(mockCuentaResponse);

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("1234567890"))
                .andExpect(jsonPath("$.estatus").value("ACTIVA"));
    }

    @Test
    @DisplayName("GET /cuentas/{numeroCuenta} debe responder 200 OK cuando existe")
    void testConsultarPorNumeroCuenta_200_OK() throws Exception {
        when(cuentaService.consultarPorNumeroCuenta("1234567890")).thenReturn(mockCuentaResponse);

        mockMvc.perform(get("/cuentas/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("1234567890"));
    }

    @Test
    @DisplayName("GET /cuentas/{numeroCuenta} debe responder 404 Not Found cuando no existe")
    void testConsultarPorNumeroCuenta_404_NotFound() throws Exception {
        when(cuentaService.consultarPorNumeroCuenta("0000000000"))
                .thenThrow(new CuentaNotFoundException("Cuenta bancaria no encontrada"));

        mockMvc.perform(get("/cuentas/0000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    @Test
    @DisplayName("GET /cuentas/{numeroCuenta}/saldo debe responder 200 OK con el saldo disponible")
    void testConsultarSaldo_200_OK() throws Exception {
        when(cuentaService.consultarSaldo("1234567890")).thenReturn(new BigDecimal("1000.00"));

        mockMvc.perform(get("/cuentas/1234567890/saldo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("1234567890"))
                .andExpect(jsonPath("$.saldo").value(1000.00));
    }

    @Test
    @DisplayName("GET /cuentas?clienteId=1 debe responder 200 OK con las cuentas del cliente")
    void testConsultarCuentasPorClienteId_200_OK() throws Exception {
        when(cuentaService.consultarPorClienteId(1)).thenReturn(List.of(mockCuentaResponse));

        mockMvc.perform(get("/cuentas?clienteId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCuenta").value("1234567890"));
    }

    @Test
    @DisplayName("PATCH /cuentas/{numeroCuenta} debe responder 200 OK al actualizar")
    void testActualizarParcial_200_OK() throws Exception {
        CuentaActualizaRequest request = CuentaActualizaRequest.builder()
                .estatus("BLOQUEADA")
                .build();

        when(cuentaService.actualizarParcial(eq("1234567890"), any(CuentaActualizaRequest.class)))
                .thenReturn(mockCuentaResponse);

        mockMvc.perform(patch("/cuentas/1234567890")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
