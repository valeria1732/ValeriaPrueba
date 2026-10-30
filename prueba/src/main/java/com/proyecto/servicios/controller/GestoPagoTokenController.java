package com.proyecto.servicios.controller;

import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.service.GestoPagoTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/gestopago/token")
@Tag(name = "GestoPago Token", description = "Operaciones de verificación de estado y renovación del token de autenticación GestoPago (sin exponer el secreto Bearer en respuestas HTTP)")
public class GestoPagoTokenController {

    private final GestoPagoTokenService tokenService;

    @Value("${gestopago.auth.id-distribuidor:1001}")
    private Integer defaultDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo:DEV_TEST_001}")
    private String defaultDispositivo;

    @Autowired
    public GestoPagoTokenController(GestoPagoTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Operation(summary = "Verificar estado del token activo en Base de Datos (sin exponer el token Bearer)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del token activo verificado"),
            @ApiResponse(responseCode = "401", description = "No existe token activo registrado")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTokenActivo(
            @RequestParam(value = "idDistribuidor", required = false) Integer idDistribuidor,
            @RequestParam(value = "codigoDispositivo", required = false) String codigoDispositivo) {

        Integer dist = idDistribuidor != null ? idDistribuidor : defaultDistribuidor;
        String disp = codigoDispositivo != null ? codigoDispositivo : defaultDispositivo;

        GestoPagoToken token = tokenService.obtenerTokenActivo(dist, disp)
                .orElseThrow(() -> new GestoPagoAuthException("No se encontró token activo en base de datos para distribuidor " + dist));

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", token.getId());
        resp.put("idDistribuidor", token.getIdDistribuidor());
        resp.put("codigoDispositivo", token.getCodigoDispositivo());
        resp.put("tokenType", token.getTokenType());
        resp.put("activo", token.getActivo());
        resp.put("fechaActualizacion", token.getFechaActualizacion());
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Forzar renovación y persistencia de token en Base de Datos (sin exponer el token en respuesta HTTP)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado y persistido correctamente en BD")
    })
    @PostMapping("/renovar")
    public ResponseEntity<Map<String, Object>> renovarToken(
            @RequestParam(value = "idDistribuidor", required = false) Integer idDistribuidor,
            @RequestParam(value = "codigoDispositivo", required = false) String codigoDispositivo) {

        Integer dist = idDistribuidor != null ? idDistribuidor : defaultDistribuidor;
        String disp = codigoDispositivo != null ? codigoDispositivo : defaultDispositivo;

        log.info("Solicitud manual de renovación de token para distribuidor={}", dist);
        tokenService.renovarToken();

        GestoPagoToken token = tokenService.asegurarTokenActivo(dist, disp);

        Map<String, Object> resp = new HashMap<>();
        resp.put("mensaje", "Token renovado y persistido exitosamente en Base de Datos");
        resp.put("id", token.getId());
        resp.put("idDistribuidor", token.getIdDistribuidor());
        resp.put("codigoDispositivo", token.getCodigoDispositivo());
        resp.put("tokenType", token.getTokenType());
        resp.put("activo", token.getActivo());
        resp.put("fechaActualizacion", token.getFechaActualizacion());
        return ResponseEntity.ok(resp);
    }
}
