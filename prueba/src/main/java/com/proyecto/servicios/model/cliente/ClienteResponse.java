package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información integral del cliente y su cuenta bancaria")
public class ClienteResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Identificador único del cliente", example = "1")
    private Integer id;

    @Schema(description = "Primer nombre", example = "Mariana")
    private String nombre;

    @Schema(description = "Segundo nombre", example = "Sofia")
    private String segundoNombre;

    @Schema(description = "Apellido paterno", example = "Hernandez")
    private String apellidoPaterno;

    @Schema(description = "Apellido materno", example = "Torres")
    private String apellidoMaterno;

    @Schema(description = "Nombre completo formateado", example = "Mariana Sofia Hernandez Torres")
    private String nombreCompleto;

    @Schema(description = "Fecha de nacimiento", example = "1994-08-22")
    private LocalDate fechaNacimiento;

    @Schema(description = "CURP", example = "HETM940822MDFRRN03")
    private String curp;

    @Schema(description = "RFC", example = "HETM9408228K4")
    private String rfc;

    @Schema(description = "Teléfono de contacto", example = "5512345678")
    private String telefono;

    @Schema(description = "Correo electrónico", example = "mariana.hernandez@example.com")
    private String email;

    @Schema(description = "Calle", example = "Av. Insurgentes Sur")
    private String calle;

    @Schema(description = "Número exterior", example = "1200")
    private String numeroExterior;

    @Schema(description = "Colonia", example = "Del Valle")
    private String colonia;

    @Schema(description = "Código postal", example = "03100")
    private String codigoPostal;

    @Schema(description = "Ciudad", example = "Ciudad de México")
    private String ciudad;

    @Schema(description = "Estado", example = "CDMX")
    private String estado;

    @Schema(description = "Puesto laboral", example = "Desarrollador de Software")
    private String puestoLaboral;

    @Schema(description = "Ingreso mensual", example = "35000.00")
    private BigDecimal ingresoMensual;

    @Schema(description = "Estatus activo del cliente", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha de registro", example = "2026-10-06T18:30:00")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de última modificación", example = "2026-10-06T18:30:00")
    private LocalDateTime fechaActualizacion;

    @Schema(description = "Cuenta bancaria vinculada")
    private CuentaBancariaResponse cuentaBancaria;
}
