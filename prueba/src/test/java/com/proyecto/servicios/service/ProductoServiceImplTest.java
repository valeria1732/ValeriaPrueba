package com.proyecto.servicios.service;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoProductResponse;
import com.proyecto.servicios.service.Impl.ProductoServiceImpl;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private GestoPagoProductClient gestoPagoProductClient;

    @Mock
    private GestoPagoTokenService gestoPagoTokenService;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private GestoPagoProductListResponse mockSuccessResponse;

    @BeforeEach
    void setUp() {
        GestoPagoProductResponse item = GestoPagoProductResponse.builder()
                .idProducto(101)
                .codigo("PROD-001")
                .descripcion("Recarga Tiempo Aire")
                .categoria("TELEFONIA")
                .montoMinimo(new BigDecimal("10.00"))
                .montoMaximo(new BigDecimal("500.00"))
                .comision(new BigDecimal("1.50"))
                .activo(true)
                .build();

        mockSuccessResponse = GestoPagoProductListResponse.builder()
                .codigo(0)
                .mensaje("Consulta exitosa")
                .status(200)
                .productos(List.of(item))
                .build();
    }

    @Test
    @DisplayName("Debe retornar lista de productos exitosamente utilizando token configurado en properties")
    void testObtenerListaProductos_Exito_ConTokenConfigurado() {
        // Arrange
        ReflectionTestUtils.setField(productoService, "configuredToken", "test_token_abc123");
        ReflectionTestUtils.setField(productoService, "idDistribuidor", 1001);
        ReflectionTestUtils.setField(productoService, "codigoDispositivo", "DEV_001");

        when(gestoPagoTokenService.obtenerTokenActivo(1001, "DEV_001")).thenReturn(Optional.empty());
        when(gestoPagoProductClient.getProductList("Bearer test_token_abc123")).thenReturn(mockSuccessResponse);

        // Act
        GestoPagoProductListResponse result = productoService.obtenerListaProductos();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getCodigo());
        assertEquals(1, result.getProductos().size());
        assertEquals("PROD-001", result.getProductos().get(0).getCodigo());
        verify(gestoPagoProductClient, times(1)).getProductList("Bearer test_token_abc123");
    }

    @Test
    @DisplayName("Debe retornar lista de productos exitosamente utilizando token activo desde base de datos")
    void testObtenerListaProductos_Exito_ConTokenBaseDatos() {
        // Arrange
        ReflectionTestUtils.setField(productoService, "configuredToken", "fallback_token");
        ReflectionTestUtils.setField(productoService, "idDistribuidor", 1001);
        ReflectionTestUtils.setField(productoService, "codigoDispositivo", "DEV_001");

        GestoPagoToken dbToken = new GestoPagoToken();
        dbToken.setToken("db_token_xyz789");
        dbToken.setActivo(true);

        when(gestoPagoTokenService.obtenerTokenActivo(1001, "DEV_001")).thenReturn(Optional.of(dbToken));
        when(gestoPagoProductClient.getProductList("Bearer db_token_xyz789")).thenReturn(mockSuccessResponse);

        // Act
        GestoPagoProductListResponse result = productoService.obtenerListaProductos();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductos().size());
        verify(gestoPagoProductClient, times(1)).getProductList("Bearer db_token_xyz789");
    }

    @Test
    @DisplayName("Debe lanzar GestoPagoAuthException si no hay ningún token disponible")
    void testObtenerListaProductos_SinToken_LanzaExcepcion() {
        // Arrange
        ReflectionTestUtils.setField(productoService, "configuredToken", "");
        ReflectionTestUtils.setField(productoService, "idDistribuidor", 1001);
        ReflectionTestUtils.setField(productoService, "codigoDispositivo", "DEV_001");

        when(gestoPagoTokenService.obtenerTokenActivo(1001, "DEV_001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(GestoPagoAuthException.class, () -> productoService.obtenerListaProductos());
        verify(gestoPagoProductClient, never()).getProductList(anyString());
    }

    @Test
    @DisplayName("Debe manejar error 401 Unauthorized y lanzar GestoPagoAuthException")
    void testObtenerListaProductos_ErrorAutenticacion_401() {
        // Arrange
        ReflectionTestUtils.setField(productoService, "configuredToken", "invalid_token");
        ReflectionTestUtils.setField(productoService, "idDistribuidor", 0);
        ReflectionTestUtils.setField(productoService, "codigoDispositivo", "");

        Request request = Request.create(
                Request.HttpMethod.GET,
                "/sistema/service/getProductList.do",
                new HashMap<>(),
                Request.Body.empty(),
                new RequestTemplate()
        );
        FeignException.Unauthorized unauthorizedException = new FeignException.Unauthorized("Unauthorized", request, null, null);

        when(gestoPagoProductClient.getProductList("Bearer invalid_token")).thenThrow(unauthorizedException);

        // Act & Assert
        GestoPagoAuthException ex = assertThrows(GestoPagoAuthException.class, () -> productoService.obtenerListaProductos());
        assertTrue(ex.getMessage().contains("Token no autorizado o expirado"));
    }

    @Test
    @DisplayName("Debe manejar Timeout / RetryableException y lanzar GestoPagoTimeoutException")
    void testObtenerListaProductos_ErrorTimeout_RetryableException() {
        // Arrange
        ReflectionTestUtils.setField(productoService, "configuredToken", "valid_token");
        ReflectionTestUtils.setField(productoService, "idDistribuidor", 0);
        ReflectionTestUtils.setField(productoService, "codigoDispositivo", "");

        Request request = Request.create(
                Request.HttpMethod.GET,
                "/sistema/service/getProductList.do",
                new HashMap<>(),
                Request.Body.empty(),
                new RequestTemplate()
        );
        RetryableException timeoutException = new RetryableException(
                504,
                "Read timed out",
                Request.HttpMethod.GET,
                new Date(),
                request
        );

        when(gestoPagoProductClient.getProductList("Bearer valid_token")).thenThrow(timeoutException);

        // Act & Assert
        GestoPagoTimeoutException ex = assertThrows(GestoPagoTimeoutException.class, () -> productoService.obtenerListaProductos());
        assertTrue(ex.getMessage().contains("Tiempo de espera agotado"));
    }

    @Test
    @DisplayName("Debe manejar error 500 del servicio externo y lanzar GestoPagoIntegrationException")
    void testObtenerListaProductos_ErrorServicioExterno_500() {
        // Arrange
        ReflectionTestUtils.setField(productoService, "configuredToken", "valid_token");
        ReflectionTestUtils.setField(productoService, "idDistribuidor", 0);
        ReflectionTestUtils.setField(productoService, "codigoDispositivo", "");

        Request request = Request.create(
                Request.HttpMethod.GET,
                "/sistema/service/getProductList.do",
                new HashMap<>(),
                Request.Body.empty(),
                new RequestTemplate()
        );
        FeignException.InternalServerError serverError = new FeignException.InternalServerError("Internal Server Error", request, null, null);

        when(gestoPagoProductClient.getProductList("Bearer valid_token")).thenThrow(serverError);

        // Act & Assert
        GestoPagoIntegrationException ex = assertThrows(GestoPagoIntegrationException.class, () -> productoService.obtenerListaProductos());
        assertEquals(500, ex.getCodigo());
    }

    @Test
    @DisplayName("Debe preservar el prefijo Bearer si el token ya lo contiene")
    void testObtenerListaProductos_TokenConPrefijoBearer() {
        // Arrange
        ReflectionTestUtils.setField(productoService, "configuredToken", "Bearer custom_bearer_token");
        ReflectionTestUtils.setField(productoService, "idDistribuidor", 0);
        ReflectionTestUtils.setField(productoService, "codigoDispositivo", "");

        when(gestoPagoProductClient.getProductList("Bearer custom_bearer_token")).thenReturn(mockSuccessResponse);

        // Act
        GestoPagoProductListResponse result = productoService.obtenerListaProductos();

        // Assert
        assertNotNull(result);
        verify(gestoPagoProductClient, times(1)).getProductList("Bearer custom_bearer_token");
    }
}
