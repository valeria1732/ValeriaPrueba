package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import com.proyecto.servicios.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "Operaciones de consulta de catálogo de productos externos")
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Obtener lista de productos", description = "Consulta el catálogo de productos desde el servicio externo GestoPago")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catálogo de productos obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Error de autenticación o token inválido"),
            @ApiResponse(responseCode = "502", description = "Error en el servicio externo"),
            @ApiResponse(responseCode = "504", description = "Tiempo de espera agotado al consultar el servicio externo")
    })
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GestoPagoProductListResponse> obtenerListaProductos() {
        log.info("Petición recibida en GET /productos");
        GestoPagoProductListResponse response = productoService.obtenerListaProductos();
        return ResponseEntity.ok(response);
    }
}
