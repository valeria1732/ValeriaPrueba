package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.proyecto.servicios.exception.ClienteBusinessException;
import com.proyecto.servicios.exception.ClienteNotFoundException;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClientePatchRequest;
import com.proyecto.servicios.model.cliente.ClienteRegistroRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.service.ClienteService;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    private ClienteResponse mockResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(clienteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockResponse = ClienteResponse.builder()
                .id(1)
                .nombre("Mariana")
                .apellidoPaterno("Hernandez")
                .curp("HETM940822MDFRRN03")
                .rfc("HETM9408228K4")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("POST /clientes debe responder 201 Created al registrar un cliente exitosamente")
    void testRegistrarCliente_201_Created() throws Exception {
        ClienteRegistroRequest request = ClienteRegistroRequest.builder()
                .nombre("Mariana")
                .apellidoPaterno("Hernandez")
                .fechaNacimiento(LocalDate.of(1994, 8, 22))
                .curp("HETM940822MDFRRN03")
                .rfc("HETM9408228K4")
                .telefono("5512345678")
                .email("mariana@example.com")
                .saldoInicial(new BigDecimal("1000.00"))
                .build();

        when(clienteService.registrarCliente(any(ClienteRegistroRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Mariana"))
                .andExpect(jsonPath("$.curp").value("HETM940822MDFRRN03"));
    }

    @Test
    @DisplayName("POST /clientes debe responder 400 Bad Request cuando CURP o RFC ya existen")
    void testRegistrarCliente_400_CurpDuplicada() throws Exception {
        ClienteRegistroRequest request = ClienteRegistroRequest.builder()
                .nombre("Mariana")
                .apellidoPaterno("Hernandez")
                .fechaNacimiento(LocalDate.of(1994, 8, 22))
                .curp("HETM940822MDFRRN03")
                .rfc("HETM9408228K4")
                .build();

        when(clienteService.registrarCliente(any(ClienteRegistroRequest.class)))
                .thenThrow(new ClienteBusinessException("Ya existe un cliente registrado con la CURP"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.mensaje").value("Ya existe un cliente registrado con la CURP"));
    }

    @Test
    @DisplayName("GET /clientes/{id} debe responder 200 OK cuando el cliente existe")
    void testConsultarPorId_200_OK() throws Exception {
        when(clienteService.consultarPorId(1)).thenReturn(mockResponse);

        mockMvc.perform(get("/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Mariana"));
    }

    @Test
    @DisplayName("GET /clientes/{id} debe responder 404 Not Found cuando el cliente no existe")
    void testConsultarPorId_404_NotFound() throws Exception {
        when(clienteService.consultarPorId(999))
                .thenThrow(new ClienteNotFoundException("Cliente no encontrado"));

        mockMvc.perform(get("/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    @Test
    @DisplayName("GET /clientes debe responder 200 OK con la lista de clientes")
    void testConsultarClientes_200_OK() throws Exception {
        when(clienteService.consultarClientes(any(), any(), any(), any())).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/clientes?nombre=Mariana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Mariana"));
    }

    @Test
    @DisplayName("PUT /clientes/{id} debe responder 200 OK al actualizar un cliente")
    void testActualizarCliente_200_OK() throws Exception {
        ClienteActualizaRequest request = ClienteActualizaRequest.builder()
                .nombre("Mariana")
                .apellidoPaterno("Hernandez")
                .puestoLaboral("Tech Lead")
                .build();

        when(clienteService.actualizarCliente(eq(1), any(ClienteActualizaRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PATCH /clientes/{id} debe responder 200 OK al actualizar parcialmente")
    void testActualizarParcial_200_OK() throws Exception {
        ClientePatchRequest request = ClientePatchRequest.builder()
                .telefono("5512345678")
                .build();

        when(clienteService.actualizarParcial(eq(1), any(ClientePatchRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(patch("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /clientes/{id} debe responder 204 No Content en baja lógica")
    void testBajaLogica_204_NoContent() throws Exception {
        doNothing().when(clienteService).bajaLogica(1);

        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isNoContent());

        verify(clienteService, times(1)).bajaLogica(1);
    }
}
