package com.proyecto.servicios.controller;

import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoProductResponse;
import com.proyecto.servicios.service.ProductoService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /productos debe responder 200 OK con la lista de productos")
    void testGetProductos_200_OK() throws Exception {
        GestoPagoProductResponse item = GestoPagoProductResponse.builder()
                .idProducto(1)
                .codigo("CFE")
                .descripcion("Pago de Luz CFE")
                .categoria("SERVICIOS")
                .montoMinimo(new BigDecimal("1.00"))
                .montoMaximo(new BigDecimal("10000.00"))
                .comision(new BigDecimal("10.00"))
                .activo(true)
                .build();

        GestoPagoProductListResponse response = GestoPagoProductListResponse.builder()
                .codigo(0)
                .mensaje("Operación exitosa")
                .status(200)
                .productos(List.of(item))
                .build();

        when(productoService.obtenerListaProductos()).thenReturn(response);

        mockMvc.perform(get("/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(0))
                .andExpect(jsonPath("$.productos[0].codigo").value("CFE"))
                .andExpect(jsonPath("$.productos[0].descripcion").value("Pago de Luz CFE"));
    }

    @Test
    @DisplayName("GET /productos debe responder 401 Unauthorized cuando ocurre GestoPagoAuthException")
    void testGetProductos_401_Unauthorized() throws Exception {
        when(productoService.obtenerListaProductos())
                .thenThrow(new GestoPagoAuthException("Token no autorizado o expirado"));

        mockMvc.perform(get("/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(401))
                .andExpect(jsonPath("$.mensaje").value("Token no autorizado o expirado"));
    }

    @Test
    @DisplayName("GET /productos debe responder 504 Gateway Timeout cuando ocurre GestoPagoTimeoutException")
    void testGetProductos_504_Timeout() throws Exception {
        when(productoService.obtenerListaProductos())
                .thenThrow(new GestoPagoTimeoutException("Tiempo de espera agotado"));

        mockMvc.perform(get("/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.codigo").value(504));
    }

    @Test
    @DisplayName("GET /productos debe responder 502 Bad Gateway cuando ocurre GestoPagoIntegrationException")
    void testGetProductos_502_BadGateway() throws Exception {
        when(productoService.obtenerListaProductos())
                .thenThrow(new GestoPagoIntegrationException("Error en respuesta externa", 502));

        mockMvc.perform(get("/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.codigo").value(502))
                .andExpect(jsonPath("$.mensaje").value("Error en respuesta externa"));
    }
}
