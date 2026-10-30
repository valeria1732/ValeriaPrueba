package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.GenericResponse;
import feign.FeignException;
import feign.RetryableException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransaccionPendienteConciliacionException.class)
    public ResponseEntity<GenericResponse> handleTransaccionPendienteConciliacion(TransaccionPendienteConciliacionException ex) {
        log.error("Transacción en estado indeterminado (PENDIENTE_CONCILIACION). Folio={}: {}",
                ex.getFolioInterno(), sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.GATEWAY_TIMEOUT.value());
        response.setMensaje(sanitizarMensaje(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    @ExceptionHandler(GestoPagoAuthException.class)
    public ResponseEntity<GenericResponse> handleGestoPagoAuthException(GestoPagoAuthException ex) {
        log.error("Error de autenticación en servicio externo: {}", sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.UNAUTHORIZED.value());
        response.setMensaje(sanitizarMensaje(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(GestoPagoTimeoutException.class)
    public ResponseEntity<GenericResponse> handleGestoPagoTimeoutException(GestoPagoTimeoutException ex) {
        log.error("Timeout al comunicar con servicio externo: {}", sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.GATEWAY_TIMEOUT.value());
        response.setMensaje("Tiempo de espera agotado al conectar con el servicio externo");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    @ExceptionHandler(GestoPagoIntegrationException.class)
    public ResponseEntity<GenericResponse> handleGestoPagoIntegrationException(GestoPagoIntegrationException ex) {
        log.error("Error de integración con servicio externo (código={}): {}", ex.getCodigo(), sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(ex.getCodigo() != null ? ex.getCodigo() : HttpStatus.BAD_GATEWAY.value());
        response.setMensaje(sanitizarMensaje(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<GenericResponse> handleRetryableException(RetryableException ex) {
        log.error("Error de conexión / timeout en cliente Feign: {}", sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.GATEWAY_TIMEOUT.value());
        response.setMensaje("No fue posible establecer comunicación con el servicio externo (Timeout/Conexión)");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    public ResponseEntity<GenericResponse> handleFeignUnauthorized(FeignException.Unauthorized ex) {
        log.error("Error 401 Unauthorized desde servicio externo Feign");
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.UNAUTHORIZED.value());
        response.setMensaje("Credenciales o Bearer Token no autorizados para el servicio externo");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<GenericResponse> handleFeignException(FeignException ex) {
        log.error("Error en invocación Feign: status={}", ex.status());
        GenericResponse response = new GenericResponse();
        response.setCodigo(ex.status() > 0 ? ex.status() : HttpStatus.BAD_GATEWAY.value());
        response.setMensaje("Error al procesar la respuesta del servicio externo: " + ex.status());
        HttpStatus status = HttpStatus.resolve(ex.status());
        return ResponseEntity.status(status != null ? status : HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler({
            CurpDuplicadaException.class,
            RfcDuplicadoException.class,
            CorreoDuplicadoException.class,
            TransaccionDuplicadaException.class
    })
    public ResponseEntity<GenericResponse> handleDuplicadosException(RuntimeException ex) {
        log.warn("Conflicto por duplicidad: {}", sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.CONFLICT.value());
        response.setMensaje(sanitizarMensaje(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler({
            PessimisticLockingFailureException.class,
            CannotAcquireLockException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<GenericResponse> handleConcurrencyExceptions(Exception ex) {
        log.warn("Conflicto de concurrencia detectado en transacción financiera: {}", ex.getClass().getSimpleName());
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.CONFLICT.value());
        response.setMensaje("Se detectó una operación simultánea en curso sobre la cuenta. Por favor intente nuevamente.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GenericResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos o clave de idempotencia duplicada");
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.CONFLICT.value());
        response.setMensaje("La solicitud ya fue registrada previamente o viola una restricción de unicidad");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler({
            ClienteNotFoundException.class,
            CuentaNotFoundException.class,
            UsuarioNotFoundException.class,
            ProductoNoEncontradoException.class
    })
    public ResponseEntity<GenericResponse> handleNotFoundExceptions(RuntimeException ex) {
        log.warn("Recurso no encontrado: {}", sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.NOT_FOUND.value());
        response.setMensaje(sanitizarMensaje(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({
            UsuarioInactivoException.class,
            OperacionNoAutorizadaException.class
    })
    public ResponseEntity<GenericResponse> handleForbiddenExceptions(RuntimeException ex) {
        log.warn("Operación no autorizada / acceso denegado: {}", sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.FORBIDDEN.value());
        response.setMensaje(sanitizarMensaje(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<GenericResponse> handleCredencialesInvalidasException(CredencialesInvalidasException ex) {
        log.warn("Credenciales inválidas: {}", sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.UNAUTHORIZED.value());
        response.setMensaje(sanitizarMensaje(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler({
            ContrasenaInvalidaException.class,
            MontoInvalidoException.class,
            SaldoInsuficienteException.class,
            ClienteBusinessException.class
    })
    public ResponseEntity<GenericResponse> handleBusinessExceptions(RuntimeException ex) {
        log.warn("Regla de negocio no cumplida: {}", sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.BAD_REQUEST.value());
        response.setMensaje(sanitizarMensaje(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GenericResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Violación de restricciones en parámetros de entrada");
        String mensaje = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("Parámetros de entrada inválidos");
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.BAD_REQUEST.value());
        response.setMensaje(sanitizarMensaje(mensaje));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Validación fallida en petición: {}", sanitizarMensaje(ex.getMessage()));
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.BAD_REQUEST.value());
        response.setMensaje(sanitizarMensaje(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("Error al deserializar petición o validación de formato");
        Throwable rootCause = ex.getMostSpecificCause();
        String mensaje = (rootCause != null && rootCause.getMessage() != null && rootCause instanceof IllegalArgumentException)
                ? rootCause.getMessage()
                : "Formato o datos de petición inválidos";
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.BAD_REQUEST.value());
        response.setMensaje(sanitizarMensaje(mensaje));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Error de validación de petición en campos de entrada");
        String primerError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Parámetros de entrada inválidos");
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.BAD_REQUEST.value());
        response.setMensaje(sanitizarMensaje(primerError));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleGenericException(Exception ex) {
        log.error("Error no controlado en la aplicación: {}", ex.getClass().getSimpleName());
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMensaje("Ocurrió un error interno al procesar la solicitud");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Evita la exposición accidental de tokens Bearer, contraseñas, PINs o llaves criptográficas
     * en los mensajes de respuesta HTTP o registros de log.
     */
    private String sanitizarMensaje(String mensaje) {
        if (mensaje == null) {
            return "Error en el procesamiento de la solicitud";
        }
        return mensaje
                .replaceAll("(?i)Bearer\\s+[A-Za-z0-9\\-._~+/]+=*", "Bearer [REDACTED]")
                .replaceAll("(?i)GP-JWT-[A-Za-z0-9\\-]+", "[REDACTED_TOKEN]")
                .replaceAll("(?i)(password|pin|token|secret)=([^,\\s&]+)", "$1=[REDACTED]");
    }
}
