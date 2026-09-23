package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.GenericResponse;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GestoPagoAuthException.class)
    public ResponseEntity<GenericResponse> handleGestoPagoAuthException(GestoPagoAuthException ex) {
        log.error("Error de autenticación en servicio externo: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.UNAUTHORIZED.value());
        response.setMensaje(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(GestoPagoTimeoutException.class)
    public ResponseEntity<GenericResponse> handleGestoPagoTimeoutException(GestoPagoTimeoutException ex) {
        log.error("Timeout al comunicar con servicio externo: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.GATEWAY_TIMEOUT.value());
        response.setMensaje("Tiempo de espera agotado al conectar con el servicio externo");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    @ExceptionHandler(GestoPagoIntegrationException.class)
    public ResponseEntity<GenericResponse> handleGestoPagoIntegrationException(GestoPagoIntegrationException ex) {
        log.error("Error de integración con servicio externo: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(ex.getCodigo() != null ? ex.getCodigo() : HttpStatus.BAD_GATEWAY.value());
        response.setMensaje(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<GenericResponse> handleRetryableException(RetryableException ex) {
        log.error("Error de conexión / timeout en cliente Feign: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.GATEWAY_TIMEOUT.value());
        response.setMensaje("No fue posible establecer comunicación con el servicio externo (Timeout/Conexión)");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    public ResponseEntity<GenericResponse> handleFeignUnauthorized(FeignException.Unauthorized ex) {
        log.error("Error 401 Unauthorized desde servicio externo Feign: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.UNAUTHORIZED.value());
        response.setMensaje("Credenciales o Bearer Token no autorizados para el servicio externo");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<GenericResponse> handleFeignException(FeignException ex) {
        log.error("Error en invocación Feign: status={}, mensaje={}", ex.status(), ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(ex.status() > 0 ? ex.status() : HttpStatus.BAD_GATEWAY.value());
        response.setMensaje("Error al procesar la respuesta del servicio externo: " + ex.status());
        HttpStatus status = HttpStatus.resolve(ex.status());
        return ResponseEntity.status(status != null ? status : HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Error de validación de petición: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.BAD_REQUEST.value());
        response.setMensaje("Parámetros de entrada inválidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleGenericException(Exception ex) {
        log.error("Error no controlado en la aplicación: {}", ex.getMessage(), ex);
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMensaje("Ocurrió un error interno en el servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
