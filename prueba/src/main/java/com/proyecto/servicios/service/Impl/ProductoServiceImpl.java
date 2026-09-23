package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import com.proyecto.servicios.service.GestoPagoTokenService;
import com.proyecto.servicios.service.ProductoService;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final GestoPagoProductClient gestoPagoProductClient;
    private final GestoPagoTokenService gestoPagoTokenService;

    @Value("${gestopago.product.token:}")
    private String configuredToken;

    @Value("${gestopago.auth.id-distribuidor:0}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo:}")
    private String codigoDispositivo;

    @Autowired
    public ProductoServiceImpl(
            GestoPagoProductClient gestoPagoProductClient,
            @Autowired(required = false) GestoPagoTokenService gestoPagoTokenService) {
        this.gestoPagoProductClient = gestoPagoProductClient;
        this.gestoPagoTokenService = gestoPagoTokenService;
    }

    @Override
    public GestoPagoProductListResponse obtenerListaProductos() {
        log.info("Iniciando invocación a servicio externo GestoPago: GET /sistema/service/getProductList.do");
        long startTime = System.currentTimeMillis();

        try {
            String token = resolverBearerToken();
            String authorizationHeader = formatearBearerHeader(token);

            GestoPagoProductListResponse response = gestoPagoProductClient.getProductList(authorizationHeader);

            long duration = System.currentTimeMillis() - startTime;
            int totalProductos = (response != null && response.getProductos() != null) ? response.getProductos().size() : 0;
            log.info("Invocación a servicio externo finalizada exitosamente en {} ms. Total de productos obtenidos: {}",
                    duration, totalProductos);

            return response;

        } catch (GestoPagoAuthException | GestoPagoTimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Fallo controlado en integración tras {} ms: {}", duration, e.getMessage());
            throw e;
        } catch (RetryableException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Timeout o error de conexión con servicio externo tras {} ms: {}", duration, e.getMessage());
            throw new GestoPagoTimeoutException("Tiempo de espera agotado al comunicar con el servicio de productos", e);
        } catch (FeignException.Unauthorized e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error de autenticación 401 Unauthorized desde servicio externo tras {} ms", duration);
            throw new GestoPagoAuthException("Token no autorizado o expirado para consumir el catálogo de productos", e);
        } catch (FeignException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error devuelto por servicio externo (HTTP {}) tras {} ms: {}", e.status(), duration, e.getMessage());
            throw new GestoPagoIntegrationException("Error en respuesta de servicio externo: " + e.status(), e.status(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error inesperado en consulta de productos tras {} ms: {}", duration, e.getMessage(), e);
            throw new GestoPagoIntegrationException("Error interno al procesar la consulta de productos: " + e.getMessage(), e);
        }
    }

    /**
     * Resuelve el token de autenticación:
     * 1. Consulta si existe un token activo persistido en base de datos.
     * 2. En caso de no existir o no estar configurado el servicio de BD, utiliza el token provisto en application.properties.
     */
    private String resolverBearerToken() {
        if (gestoPagoTokenService != null && idDistribuidor != null && StringUtils.isNotBlank(codigoDispositivo)) {
            try {
                Optional<GestoPagoToken> tokenActivo = gestoPagoTokenService.obtenerTokenActivo(idDistribuidor, codigoDispositivo);
                if (tokenActivo.isPresent() && StringUtils.isNotBlank(tokenActivo.get().getToken())) {
                    log.debug("Utilizando token activo obtenido desde base de datos");
                    return tokenActivo.get().getToken();
                }
            } catch (Exception e) {
                log.warn("No fue posible obtener token desde base de datos, usando fallback de configuración: {}", e.getMessage());
            }
        }

        if (StringUtils.isNotBlank(configuredToken)) {
            log.debug("Utilizando token configurado en application.properties");
            return configuredToken;
        }

        throw new GestoPagoAuthException("No se encontró ningún Bearer Token configurado o disponible para la integración");
    }

    /**
     * Formatea el header de autorización agregando el prefijo Bearer si no está presente.
     */
    private String formatearBearerHeader(String token) {
        if (token == null) {
            throw new GestoPagoAuthException("El token de autenticación no puede ser nulo");
        }
        String cleanToken = token.trim();
        if (cleanToken.toLowerCase().startsWith("bearer ")) {
            return cleanToken;
        }
        return "Bearer " + cleanToken;
    }
}
