package com.proyecto.servicios.mapper;

import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class GestoPagoTokenMapperTest {

    private final GestoPagoTokenMapper mapper = Mappers.getMapper(GestoPagoTokenMapper.class);

    @Test
    @DisplayName("Debe mapear y actualizar GestoPagoToken desde GestoPagoAuthResponse")
    void testToEntityAndUpdateEntity() {
        GestoPagoAuthResponse response = new GestoPagoAuthResponse();
        response.setToken("jwt-token-123");
        response.setTokenType("Bearer");
        response.setExpiresIn(7200L);

        GestoPagoToken entity = mapper.toEntity(response);
        assertNotNull(entity);
        assertEquals("jwt-token-123", entity.getToken());
        assertEquals("Bearer", entity.getTokenType());
        assertEquals(7200L, entity.getExpiresIn());

        GestoPagoAuthResponse update = new GestoPagoAuthResponse();
        update.setToken("jwt-token-456");
        mapper.updateEntity(update, entity);
        assertEquals("jwt-token-456", entity.getToken());
        assertEquals("Bearer", entity.getTokenType());

        assertNull(mapper.toEntity(null));
        mapper.updateEntity(null, entity);
    }
}

