package com.proyecto.servicios.controller;

import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.service.GestoPagoTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GestoPagoTokenControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestoPagoTokenService tokenService;

    @InjectMocks
    private GestoPagoTokenController controller;

    private GestoPagoToken activeToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "defaultDistribuidor", 1001);
        ReflectionTestUtils.setField(controller, "defaultDispositivo", "DEV_TEST_001");

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        activeToken = new GestoPagoToken();
        activeToken.setId(10);
        activeToken.setIdDistribuidor(1001);
        activeToken.setCodigoDispositivo("DEV_TEST_001");
        activeToken.setToken("jwt-secret");
        activeToken.setActivo(true);
    }

    @Test
    @DisplayName("GET /gestopago/token debe retornar 200 OK cuando existe token activo y 401 si no existe")
    void testObtenerTokenActivo() throws Exception {
        when(tokenService.obtenerTokenActivo(1001, "DEV_TEST_001")).thenReturn(Optional.of(activeToken));

        mockMvc.perform(get("/gestopago/token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.idDistribuidor").value(1001))
                .andExpect(jsonPath("$.activo").value(true));

        when(tokenService.obtenerTokenActivo(2002, "DEV_02")).thenReturn(Optional.empty());

        mockMvc.perform(get("/gestopago/token")
                        .param("idDistribuidor", "2002")
                        .param("codigoDispositivo", "DEV_02"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /gestopago/token/renovar debe renovar y retornar resumen del token activo")
    void testRenovarToken() throws Exception {
        activeToken.setId(null);
        when(tokenService.asegurarTokenActivo(1001, "DEV_TEST_001")).thenReturn(activeToken);

        mockMvc.perform(post("/gestopago/token/renovar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.activo").value(true));

        verify(tokenService).renovarToken();
    }
}

