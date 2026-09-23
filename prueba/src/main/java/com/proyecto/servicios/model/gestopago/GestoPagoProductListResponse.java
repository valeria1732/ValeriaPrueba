package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GestoPagoProductListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("codigo")
    private Integer codigo;

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("productos")
    @Builder.Default
    private List<GestoPagoProductResponse> productos = new ArrayList<>();
}
