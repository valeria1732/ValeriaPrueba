package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GestoPagoProductResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("idProducto")
    private Integer idProducto;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("categoria")
    private String categoria;

    @JsonProperty("montoMinimo")
    private BigDecimal montoMinimo;

    @JsonProperty("montoMaximo")
    private BigDecimal montoMaximo;

    @JsonProperty("comision")
    private BigDecimal comision;

    @JsonProperty("activo")
    private Boolean activo;
}
