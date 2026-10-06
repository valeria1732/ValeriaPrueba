package com.proyecto.servicios.entity.gestopago;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gestopago_productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GestoPagoProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "codigo", nullable = false, unique = true, length = 100)
    private String codigo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "monto_minimo", precision = 12, scale = 2)
    private BigDecimal montoMinimo;

    @Column(name = "monto_maximo", precision = 12, scale = 2)
    private BigDecimal montoMaximo;

    @Column(name = "comision", precision = 12, scale = 2)
    private BigDecimal comision;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_sincronizacion", nullable = false)
    private LocalDateTime fechaSincronizacion;

    @PrePersist
    @PreUpdate
    void onSave() {
        this.fechaSincronizacion = LocalDateTime.now();
    }
}
