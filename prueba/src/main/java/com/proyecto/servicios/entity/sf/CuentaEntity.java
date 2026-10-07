package com.proyecto.servicios.entity.sf;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonBackReference
    private ClienteEntity cliente;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 10)
    private String numeroCuenta;

    @Column(name = "clabe", nullable = false, unique = true, length = 18)
    private String clabe;

    @Column(name = "saldo", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "estatus", nullable = false, length = 20)
    @Builder.Default
    private String estatus = "ACTIVA";

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
