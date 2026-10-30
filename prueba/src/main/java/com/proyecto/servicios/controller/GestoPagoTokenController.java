package com.proyecto.servicios.controller;

import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.service.GestoPagoTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/gestopago/token")
@RequiredArgsConstructor
@Tag(name = "GestoPago Token", description = "Verificación y renovación de token GestoPago")
public class GestoPagoTokenController {

    private final GestoPagoTokenService tokenService;

    @Value("${gestopago.auth.id-distribuidor:1001}")
    private Integer defaultDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo:DEV_TEST_001}")
    private String defaultDispositivo;

    @Operation(summary = "Verificar estado del token activo en BD")
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTokenActivo(
            @RequestParam(value = "idDistribuidor", required = false) Integer idDistribuidor,
            @RequestParam(value = "codigoDispositivo", required = false) String codigoDispositivo) {
        Integer dist = idDistribuidor != null ? idDistribuidor : defaultDistribuidor;
        String disp = codigoDispositivo != null ? codigoDispositivo : defaultDispositivo;
        GestoPagoToken t = tokenService.obtenerTokenActivo(dist, disp)
                .orElseThrow(() -> new GestoPagoAuthException("No se encontró token activo para distribuidor " + dist));
        return ResponseEntity.ok(resumenToken(t));
    }

    @Operation(summary = "Forzar renovación de token en BD")
    @PostMapping("/renovar")
    public ResponseEntity<Map<String, Object>> renovarToken(
            @RequestParam(value = "idDistribuidor", required = false) Integer idDistribuidor,
            @RequestParam(value = "codigoDispositivo", required = false) String codigoDispositivo) {
        tokenService.renovarToken();
        GestoPagoToken t = tokenService.asegurarTokenActivo(
                idDistribuidor != null ? idDistribuidor : defaultDistribuidor,
                codigoDispositivo != null ? codigoDispositivo : defaultDispositivo);
        return ResponseEntity.ok(resumenToken(t));
    }

    private Map<String, Object> resumenToken(GestoPagoToken t) {
        return Map.of(
                "id", t.getId() != null ? t.getId() : 0,
                "idDistribuidor", t.getIdDistribuidor(),
                "codigoDispositivo", t.getCodigoDispositivo(),
                "activo", Boolean.TRUE.equals(t.getActivo())
        );
    }
}
