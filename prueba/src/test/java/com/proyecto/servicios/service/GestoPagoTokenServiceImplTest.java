package com.proyecto.servicios.service;

import com.proyecto.servicios.client.GestoPagoAuthClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.mapper.GestoPagoTokenMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoTokenRepository;
import com.proyecto.servicios.service.impl.GestoPagoTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestoPagoTokenServiceImplTest {

    @Mock
    private GestoPagoAuthClient gestoPagoAuthClient;

    @Mock
    private GestoPagoTokenRepository tokenRepository;

    @Mock
    private GestoPagoTokenMapper tokenMapper;

    @InjectMocks
    private GestoPagoTokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "idDistribuidor", 1001);
        ReflectionTestUtils.setField(tokenService, "codigoDispositivo", "DEV_TEST_001");
        ReflectionTestUtils.setField(tokenService, "password", "secret-pass");
        ReflectionTestUtils.setField(tokenService, "envBearerToken", "");
    }

    @Test
    @DisplayName("renovarToken debe usar envBearerToken cuando está configurado sin invocar al cliente externo")
    void testRenovarToken_ConEnvBearerToken() {
        ReflectionTestUtils.setField(tokenService, "envBearerToken", "env-token-2026");

        GestoPagoToken existente = new GestoPagoToken();
        existente.setId(5);
        existente.setIdDistribuidor(1001);
        existente.setCodigoDispositivo("DEV_TEST_001");

        when(tokenRepository.findByIdDistribuidorAndCodigoDispositivo(1001, "DEV_TEST_001"))
                .thenReturn(Optional.of(existente));

        tokenService.renovarToken();

        assertEquals("env-token-2026", existente.getToken());
        assertTrue(existente.getActivo());
        verify(gestoPagoAuthClient, never()).authenticate(any(), any(), any());
        verify(tokenMapper).updateEntity(any(GestoPagoAuthResponse.class), eq(existente));
        verify(tokenRepository).save(existente);
    }

    @Test
    @DisplayName("renovarToken debe autenticar vía Feign cuando no hay token en entorno")
    void testRenovarToken_ViaFeignClient() {
        GestoPagoAuthResponse authRes = new GestoPagoAuthResponse();
        authRes.setToken("feign-jwt-token");
        authRes.setTokenType("Bearer");
        authRes.setExpiresIn(3600L);

        when(gestoPagoAuthClient.authenticate(1001, "DEV_TEST_001", "secret-pass")).thenReturn(authRes);
        when(tokenRepository.findByIdDistribuidorAndCodigoDispositivo(1001, "DEV_TEST_001"))
                .thenReturn(Optional.empty());

        tokenService.renovarToken();

        verify(tokenMapper).updateEntity(eq(authRes), any(GestoPagoToken.class));
        verify(tokenRepository).save(argThat(saved ->
                "feign-jwt-token".equals(saved.getToken()) && Boolean.TRUE.equals(saved.getActivo())));
    }

    @Test
    @DisplayName("renovarToken debe generar token de contingencia cuando falla el cliente Feign")
    void testRenovarToken_FallbackCuandoFallaFeign() {
        when(gestoPagoAuthClient.authenticate(1001, "DEV_TEST_001", "secret-pass"))
                .thenThrow(new RuntimeException("Auth unreachable"));
        when(tokenRepository.findByIdDistribuidorAndCodigoDispositivo(1001, "DEV_TEST_001"))
                .thenReturn(Optional.empty());

        tokenService.renovarToken();

        verify(tokenRepository).save(argThat(saved ->
                saved.getToken() != null && saved.getToken().startsWith("GP-JWT-") && Boolean.TRUE.equals(saved.getActivo())));
    }

    @Test
    @DisplayName("obtenerTokenActivo solo debe retornar tokens con activo=true")
    void testObtenerTokenActivo_FiltraInactivos() {
        GestoPagoToken inactivo = new GestoPagoToken();
        inactivo.setActivo(false);

        when(tokenRepository.findByIdDistribuidorAndCodigoDispositivo(1001, "DEV_TEST_001"))
                .thenReturn(Optional.of(inactivo));

        assertTrue(tokenService.obtenerTokenActivo(1001, "DEV_TEST_001").isEmpty());

        inactivo.setActivo(true);
        assertTrue(tokenService.obtenerTokenActivo(1001, "DEV_TEST_001").isPresent());
    }

    @Test
    @DisplayName("asegurarTokenActivo debe renovar y retornar el token cuando no existe uno activo")
    void testAsegurarTokenActivo_RenuevaCuandoFalta() {
        ReflectionTestUtils.setField(tokenService, "envBearerToken", "auto-renewed-token");

        GestoPagoToken activo = new GestoPagoToken();
        activo.setIdDistribuidor(1001);
        activo.setCodigoDispositivo("DEV_TEST_001");
        activo.setToken("auto-renewed-token");
        activo.setActivo(true);

        when(tokenRepository.findByIdDistribuidorAndCodigoDispositivo(1001, "DEV_TEST_001"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(activo));

        GestoPagoToken resultado = tokenService.asegurarTokenActivo(1001, "DEV_TEST_001");

        assertEquals("auto-renewed-token", resultado.getToken());
        verify(tokenRepository).save(any(GestoPagoToken.class));
    }
}

