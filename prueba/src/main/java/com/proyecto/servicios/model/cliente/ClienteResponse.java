package com.proyecto.servicios.model.cliente;

import com.proyecto.servicios.model.cuenta.CuentaResponse;
import com.proyecto.servicios.model.usuario.UsuarioResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta integral con datos personales, domicilio, cuentas y usuario de acceso")
public class ClienteResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Identificador único del cliente", example = "1")
    private Integer id;

    @Schema(description = "Nombre", example = "Mariana")
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

    @Schema(description = "Sexo", example = "FEMENINO")
    private String sexo;

    @Schema(description = "Nacionalidad", example = "Mexicana")
    private String nacionalidad;

    @Schema(description = "Estado civil", example = "SOLTERO")
    private String estadoCivil;

    @Schema(description = "Correo electrónico", example = "mariana.hernandez@example.com")
    private String correo;

    @Schema(description = "Teléfono móvil a 10 dígitos", example = "5512345678")
    private String telefonoMovil;

    @Schema(description = "Teléfono alternativo", example = "5587654321")
    private String telefonoAlternativo;

    @Schema(description = "Domicilio asociado")
    private DomicilioDTO domicilio;

    @Schema(description = "Ocupación laboral", example = "Ingeniera de Software")
    private String ocupacion;

    @Schema(description = "Empresa", example = "Tecnologías Financieras S.A.")
    private String empresa;

    @Schema(description = "Ingreso mensual", example = "45000.00")
    private BigDecimal ingresoMensual;

    @Schema(description = "Estatus activo del cliente", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha de creación del registro", example = "2026-10-06T19:00:00")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de última modificación", example = "2026-10-06T19:00:00")
    private LocalDateTime fechaActualizacion;

    @Schema(description = "Lista de cuentas bancarias asociadas al cliente")
    @Builder.Default
    private List<CuentaResponse> cuentas = new ArrayList<>();

    @Schema(description = "Usuario de acceso al sistema (sin password)")
    private UsuarioResponse usuario;
}
