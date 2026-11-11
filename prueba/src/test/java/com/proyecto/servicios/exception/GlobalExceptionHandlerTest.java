package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.GenericResponse;
import feign.FeignException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Debe mapear excepciones de integración externa y Feign a los códigos HTTP esperados")
    void testMapeoExcepcionesIntegracion() {
        ResponseEntity<GenericResponse> r504Conc = handler.handlePendienteConciliacion(
                new TransaccionPendienteConciliacionException("Pendiente", "FOLIO-1"));
        assertEquals(HttpStatus.GATEWAY_TIMEOUT, r504Conc.getStatusCode());

        ResponseEntity<GenericResponse> r401Auth = handler.handleGestoPagoAuth(
                new GestoPagoAuthException("Auth error"));
        assertEquals(HttpStatus.UNAUTHORIZED, r401Auth.getStatusCode());

        ResponseEntity<GenericResponse> r504Timeout = handler.handleTimeouts(
                new GestoPagoTimeoutException("Timeout"));
        assertEquals(HttpStatus.GATEWAY_TIMEOUT, r504Timeout.getStatusCode());

        ResponseEntity<GenericResponse> r502Int = handler.handleGestoPagoIntegration(
                new GestoPagoIntegrationException("Fallo externo", 502));
        assertEquals(HttpStatus.BAD_GATEWAY, r502Int.getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY, handler.handleGestoPagoIntegration(
                new GestoPagoIntegrationException("Sin codigo")).getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY, handler.handleGestoPagoIntegration(
                new GestoPagoIntegrationException("Con causa", new RuntimeException("io"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleBadRequest(
                new ContrasenaInvalidaException("PIN inválido")).getStatusCode());

        FeignException.Unauthorized feignUnauth = mock(FeignException.Unauthorized.class);
        assertEquals(HttpStatus.UNAUTHORIZED, handler.handleFeignUnauthorized(feignUnauth).getStatusCode());

        FeignException feignErr = mock(FeignException.class);
        when(feignErr.status()).thenReturn(502);
        assertEquals(HttpStatus.BAD_GATEWAY, handler.handleFeignException(feignErr).getStatusCode());
    }

    @Test
    @DisplayName("Debe mapear conflictos, no encontrados, prohibidos y credenciales inválidas")
    void testMapeoConflictosNotFoundYAuth() {
        assertEquals(HttpStatus.CONFLICT, handler.handleDuplicados(new CurpDuplicadaException("CURP dup")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.handleConcurrencyAndIntegrity(
                new DataIntegrityViolationException("unique")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, handler.handleNotFound(
                new CuentaNotFoundException("No existe")).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, handler.handleForbidden(
                new UsuarioInactivoException("Inactivo")).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, handler.handleCredencialesInvalidas(
                new CredencialesInvalidasException("Creds")).getStatusCode());
    }

    @Test
    @DisplayName("Debe mapear validaciones, JSON ilegible y errores genéricos sanitizando el mensaje")
    void testValidacionesYGenericos() throws NoSuchMethodException {
        ResponseEntity<GenericResponse> badReqNull = handler.handleBadRequest(new IllegalArgumentException((String) null));
        assertEquals(HttpStatus.BAD_REQUEST, badReqNull.getStatusCode());

        ResponseEntity<GenericResponse> badReqSanit = handler.handleBadRequest(
                new SaldoInsuficienteException("Saldo insuficiente <alert>"));
        assertEquals(HttpStatus.BAD_REQUEST, badReqSanit.getStatusCode());

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("monto");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("debe ser positivo");
        ConstraintViolationException cve = new ConstraintViolationException(Set.of(violation));
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleConstraintViolation(cve).getStatusCode());

        HttpMessageNotReadableException notReadable = new HttpMessageNotReadableException(
                "JSON malformado", new RuntimeException("unexpected char"), null);
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleNotReadable(notReadable).getStatusCode());

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "req");
        bindingResult.addError(new FieldError("req", "precio", "es obligatorio"));
        MethodArgumentNotValidException manve = new MethodArgumentNotValidException(
                new MethodParameter(String.class.getMethod("toString"), -1),
                bindingResult);
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleValidation(manve).getStatusCode());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.handleGeneric(new RuntimeException("boom")).getStatusCode());
    }
}

