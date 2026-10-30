package com.proyecto.servicios.entity.sf;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos_servicios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoServicioEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "numero_cuenta", nullable = false, length = 10)
    private String numeroCuenta;

    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "id_servicio", nullable = false)
    private Integer idServicio;

    @Column(name = "id_producto", nullable = false)
    private Integer idProducto;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "telefono_referencia", nullable = false, length = 50)
    private String telefonoReferencia;

    @Column(name = "distribuidor", nullable = false)
    private Integer distribuidor;

    @Column(name = "bandera", length = 20)
    private String bandera;

    @Column(name = "estatus_transaccion", nullable = false, length = 40)
    @Builder.Default
    private String estatusTransaccion = "EN_PROCESO";

    @Column(name = "codigo_respuesta", nullable = false)
    private Integer codigoRespuesta;

    @Column(name = "mensaje_respuesta", length = 255)
    private String mensajeRespuesta;

    @Column(name = "folio_autorizacion", length = 100)
    private String folioAutorizacion;

    @Column(name = "firma_aes", columnDefinition = "TEXT")
    private String firmaAes;

    @Column(name = "saldo_anterior", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    @Builder.Default
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = LocalDateTime.now();
        }
        if (estatusTransaccion == null) {
            estatusTransaccion = "EN_PROCESO";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
