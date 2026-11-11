package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.GenericResponse;
import feign.FeignException;
import feign.RetryableException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransaccionPendienteConciliacionException.class)
    public ResponseEntity<GenericResponse> handlePendienteConciliacion(TransaccionPendienteConciliacionException ex) {
        log.error("Transacción PENDIENTE_CONCILIACION (Folio={}): {}", ex.getFolioInterno(), sanitizar(ex.getMessage()));
        return build(HttpStatus.GATEWAY_TIMEOUT, ex.getMessage());
    }

    @ExceptionHandler(GestoPagoAuthException.class)
    public ResponseEntity<GenericResponse> handleGestoPagoAuth(GestoPagoAuthException ex) {
        log.error("Error de autenticación en servicio externo: {}", sanitizar(ex.getMessage()));
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({GestoPagoTimeoutException.class, RetryableException.class})
    public ResponseEntity<GenericResponse> handleTimeouts(Exception ex) {
        log.error("Timeout al comunicar con servicio externo: {}", sanitizar(ex.getMessage()));
        return build(HttpStatus.GATEWAY_TIMEOUT, "Tiempo de espera agotado al conectar con el servicio externo");
    }

    @ExceptionHandler(GestoPagoIntegrationException.class)
    public ResponseEntity<GenericResponse> handleGestoPagoIntegration(GestoPagoIntegrationException ex) {
        log.error("Error de integración externa (código={}): {}", ex.getCodigo(), sanitizar(ex.getMessage()));
        int code = ex.getCodigo() != null ? ex.getCodigo() : HttpStatus.BAD_GATEWAY.value();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new GenericResponse(code, sanitizar(ex.getMessage())));
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    public ResponseEntity<GenericResponse> handleFeignUnauthorized(FeignException.Unauthorized ex) {
        log.error("Error 401 Unauthorized desde servicio externo");
        return build(HttpStatus.UNAUTHORIZED, "Credenciales o Bearer Token no autorizados para el servicio externo");
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<GenericResponse> handleFeignException(FeignException ex) {
        log.error("Error en invocación Feign: status={}", ex.status());
        HttpStatus status = HttpStatus.resolve(ex.status());
        HttpStatus resolved = status != null ? status : HttpStatus.BAD_GATEWAY;
        int code = ex.status() > 0 ? ex.status() : HttpStatus.BAD_GATEWAY.value();
        return ResponseEntity.status(resolved).body(new GenericResponse(code, "Error al procesar la respuesta del servicio externo: " + ex.status()));
    }

    @ExceptionHandler({
            CurpDuplicadaException.class,
            RfcDuplicadoException.class,
            CorreoDuplicadoException.class,
            TransaccionDuplicadaException.class
    })
    public ResponseEntity<GenericResponse> handleDuplicados(RuntimeException ex) {
        log.warn("Conflicto por duplicidad: {}", sanitizar(ex.getMessage()));
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({
            PessimisticLockingFailureException.class,
            CannotAcquireLockException.class,
            ObjectOptimisticLockingFailureException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<GenericResponse> handleConcurrencyAndIntegrity(Exception ex) {
        log.warn("Conflicto de concurrencia o unicidad: {}", ex.getClass().getSimpleName());
        return build(HttpStatus.CONFLICT, "Operación simultánea o duplicada detectada sobre la cuenta. Intente nuevamente.");
    }

    @ExceptionHandler({
            ClienteNotFoundException.class,
            CuentaNotFoundException.class,
            UsuarioNotFoundException.class,
            ProductoNoEncontradoException.class
    })
    public ResponseEntity<GenericResponse> handleNotFound(RuntimeException ex) {
        log.warn("Recurso no encontrado: {}", sanitizar(ex.getMessage()));
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({UsuarioInactivoException.class, OperacionNoAutorizadaException.class})
    public ResponseEntity<GenericResponse> handleForbidden(RuntimeException ex) {
        log.warn("Acceso denegado: {}", sanitizar(ex.getMessage()));
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<GenericResponse> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        log.warn("Credenciales inválidas: {}", sanitizar(ex.getMessage()));
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({
            ContrasenaInvalidaException.class,
            MontoInvalidoException.class,
            SaldoInsuficienteException.class,
            ClienteBusinessException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<GenericResponse> handleBadRequest(RuntimeException ex) {
        log.warn("Validación o regla de negocio no cumplida: {}", sanitizar(ex.getMessage()));
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GenericResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .findFirst().map(ConstraintViolation::getMessage).orElse("Parámetros de entrada inválidos");
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        Throwable root = ex.getMostSpecificCause();
        String msg = (root instanceof IllegalArgumentException && root.getMessage() != null)
                ? root.getMessage() : "Formato o datos de petición inválidos";
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Parámetros de entrada inválidos");
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleGeneric(Exception ex) {
        log.error("Error no controlado: {}", ex.getClass().getSimpleName());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno al procesar la solicitud");
    }

    private ResponseEntity<GenericResponse> build(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(new GenericResponse(status.value(), sanitizar(mensaje)));
    }

    private String sanitizar(String mensaje) {
        if (mensaje == null) return "Error en el procesamiento de la solicitud";
        return mensaje
                .replaceAll("(?i)Bearer\\s+[A-Z0-9\\-._~+/]+=*", "Bearer [REDACTED]")
                .replaceAll("(?i)GP-JWT-[A-Z0-9\\-]+", "[REDACTED_TOKEN]")
                .replaceAll("(?i)(password|pin|token|secret)=([^,\\s&]+)", "$1=[REDACTED]");
    }
}
