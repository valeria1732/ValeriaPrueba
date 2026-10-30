package com.proyecto.servicios.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Schema(description = "Estructura estándar de respuesta paginada")
public record PageResponse<T>(
        @Schema(description = "Lista de elementos en la página actual")
        @JsonProperty("contenido")
        List<T> contenido,

        @Schema(description = "Número de página actual (base 0)", example = "0")
        @JsonProperty("pagina")
        int pagina,

        @Schema(description = "Tamaño de página configurado", example = "10")
        @JsonProperty("tamanoPagina")
        int tamanoPagina,

        @Schema(description = "Total general de elementos encontrados", example = "50")
        @JsonProperty("totalElementos")
        long totalElementos,

        @Schema(description = "Total de páginas disponibles", example = "5")
        @JsonProperty("totalPaginas")
        int totalPaginas,

        @Schema(description = "Indica si es la primera página", example = "true")
        @JsonProperty("primera")
        boolean primera,

        @Schema(description = "Indica si es la última página", example = "false")
        @JsonProperty("ultima")
        boolean ultima
) implements Serializable {

    public static <T> PageResponse<T> from(Page<T> page) {
        if (page == null) {
            return new PageResponse<>(Collections.emptyList(), 0, 10, 0, 0, true, true);
        }
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public static <T> PageResponse<T> of(List<T> contenido, int pagina, int tamanoPagina, long totalElementos) {
        List<T> items = contenido != null ? contenido : Collections.emptyList();
        int safeSize = tamanoPagina > 0 ? tamanoPagina : 10;
        int safePage = Math.max(pagina, 0);
        int totalPaginas = (int) Math.ceil((double) totalElementos / safeSize);
        boolean primera = safePage == 0;
        boolean ultima = safePage >= (totalPaginas - 1);
        return new PageResponse<>(items, safePage, safeSize, totalElementos, totalPaginas, primera, ultima);
    }

    public List<T> getContent() {
        return contenido;
    }

    public int getPageNumber() {
        return pagina;
    }

    public int getPageSize() {
        return tamanoPagina;
    }

    public long getTotalElements() {
        return totalElementos;
    }

    public int getTotalPages() {
        return totalPaginas;
    }

    public boolean isFirst() {
        return primera;
    }

    public boolean isLast() {
        return ultima;
    }
}
