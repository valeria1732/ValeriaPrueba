package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;

public interface ProductoService {

    /**
     * Consulta el catálogo de productos desde el servicio externo GestoPago.
     * Utiliza autenticación Bearer Token obtenida desde la configuración.
     *
     * @return Respuesta estructurada con la lista de productos y metadatos.
     */
    GestoPagoProductListResponse obtenerListaProductos();
}
