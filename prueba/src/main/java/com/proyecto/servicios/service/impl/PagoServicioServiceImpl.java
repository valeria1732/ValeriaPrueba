package com.proyecto.servicios.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.GestoPagoTransactionClient;
import com.proyecto.servicios.config.SecurityConfig;
import com.proyecto.servicios.entity.gestopago.GestoPagoProductoEntity;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.entity.sf.ClienteEntity;
import com.proyecto.servicios.entity.sf.CuentaEntity;
import com.proyecto.servicios.entity.sf.PagoServicioEntity;
import com.proyecto.servicios.entity.sf.UsuarioEntity;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.pago.*;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.repositorys.sf.PagoServicioRepository;
import com.proyecto.servicios.service.GestoPagoTokenService;
import com.proyecto.servicios.service.PagoServicioService;
import com.proyecto.servicios.util.AesEncryptionService;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@Slf4j
public class PagoServicioServiceImpl implements PagoServicioService {

    private static final String ESTATUS_EN_PROCESO = "EN_PROCESO";
    private static final String ESTATUS_PENDIENTE = "PENDIENTE_CONCILIACION";
    private static final String ESTATUS_EXITOSA = "EXITOSA";
    private static final String ESTATUS_REVERSADA = "REVERSADA";
    private static final List<String> ESTATUS_ACTIVOS = List.of(ESTATUS_EN_PROCESO, ESTATUS_PENDIENTE, ESTATUS_EXITOSA);
    private static final String DEFAULT_DEVICE_FALLBACK = "DEV_TEST_001";

    private final CuentaRepository cuentaRepository;
    private final GestoPagoTokenService gestoPagoTokenService;
    private final GestoPagoTransactionClient transactionClient;
    private final AesEncryptionService aesEncryptionService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final PagoServicioRepository pagoServicioRepository;
    private final GestoPagoProductoRepository gestoPagoProductoRepository;
    private final SecurityConfig securityConfig;
    private final TransactionTemplate transactionTemplate;

    @Value("${gestopago.auth.codigo-dispositivo:DEV_TEST_001}")
    private String defaultCodigoDispositivo;

    @Value("${gestopago.transaction.token:}")
    private String configuredBearerToken;

    @Autowired
    public PagoServicioServiceImpl(
            CuentaRepository cuentaRepository,
            GestoPagoTokenService gestoPagoTokenService,
            GestoPagoTransactionClient transactionClient,
            AesEncryptionService aesEncryptionService,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper,
            PagoServicioRepository pagoServicioRepository,
            GestoPagoProductoRepository gestoPagoProductoRepository,
            SecurityConfig securityConfig,
            @Autowired(required = false) PlatformTransactionManager transactionManager) {
        this.cuentaRepository = cuentaRepository;
        this.gestoPagoTokenService = gestoPagoTokenService;
        this.transactionClient = transactionClient;
        this.aesEncryptionService = aesEncryptionService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.pagoServicioRepository = pagoServicioRepository;
        this.gestoPagoProductoRepository = gestoPagoProductoRepository;
        this.securityConfig = securityConfig;
        this.transactionTemplate = transactionManager != null ? crearTxTemplate(transactionManager) : null;
    }

    @Override
    public PagoServicioResponse procesarPagoServicio(PagoServicioRequest request) {
        return procesarPagoServicio(request, null, null);
    }

    @Override
    public PagoServicioResponse procesarPagoServicio(PagoServicioRequest req, String authHeader, String headerIdempKey) {
        validarEntradaYCatalogo(req);

        String idempKey = StringUtils.defaultIfBlank(headerIdempKey, req.getIdempotencyKey());
        PagoServicioResponse respuestaPrevia = buscarRespuestaIdempotente(req, idempKey);
        if (respuestaPrevia != null) {
            return respuestaPrevia;
        }

        String dispositivo = StringUtils.defaultIfBlank(defaultCodigoDispositivo, DEFAULT_DEVICE_FALLBACK);
        String rawToken = resolverBearerToken(req.getDistribuidor(), dispositivo);
        String firmaAes = cifrarPayload(req);

        // Fase 1: Bloqueo pesimista, validación de identidad/saldo/idempotencia y débito preventivo
        PagoServicioEntity tx = enTransaccion(() -> reservarFondos(req, authHeader, StringUtils.trimToNull(idempKey), firmaAes));

        // Fase 2: Envío a GestoPago POST /sistema/service/sendTx.do
        GestoPagoTxRequest txReq = GestoPagoTxRequest.builder()
                .payloadFirmado(firmaAes)
                .token(limpiarBearer(rawToken))
                .idDistribuidor(req.getDistribuidor())
                .codigoDispositivo(dispositivo)
                .bandera(StringUtils.defaultIfBlank(req.getBandera(), "0"))
                .build();

        GestoPagoTxResponse res;
        try {
            res = transactionClient.sendTx("Bearer " + limpiarBearer(rawToken), txReq);
        } catch (Exception ex) {
            throw manejarErrorProveedor(tx, ex);
        }

        // Fase 3: Confirmación o compensación por rechazo explícito del proveedor
        if (res != null && Integer.valueOf(0).equals(res.getCodigo())) {
            PagoServicioEntity confirmada = enTransaccion(() -> actualizarEstado(
                    tx.getId(), ESTATUS_EXITOSA, 0,
                    StringUtils.defaultIfBlank(res.getMensaje(), "Pago procesado exitosamente"),
                    StringUtils.firstNonBlank(res.getFolio(), res.getAutorizacion(), tx.getFolioAutorizacion())));
            return mapearRespuesta(confirmada, true);
        }

        // Si la respuesta es nula o sin código, no se asume rechazo: pasa a PENDIENTE_CONCILIACION
        if (res == null || res.getCodigo() == null) {
            ejecutarEnTransaccion(() -> actualizarEstado(
                    tx.getId(), ESTATUS_PENDIENTE, 500,
                    "Respuesta indeterminada del proveedor. Saldo retenido para conciliación",
                    tx.getFolioAutorizacion()));
            throw new TransaccionPendienteConciliacionException(
                    "Respuesta indeterminada del proveedor. Operación " + tx.getFolioAutorizacion()
                            + " en estado PENDIENTE_CONCILIACION.",
                    tx.getFolioAutorizacion(), null);
        }

        int cod = res.getCodigo();
        String msg = StringUtils.defaultIfBlank(res.getMensaje(), "Rechazado por proveedor");
        revertirSaldo(tx.getId(), cod, msg);
        throw new ClienteBusinessException("Transacción rechazada por el proveedor (Código " + cod + "): " + msg);
    }

    private PagoServicioResponse buscarRespuestaIdempotente(PagoServicioRequest req, String idempKey) {
        if (StringUtils.isBlank(idempKey)) {
            return null;
        }
        Optional<PagoServicioEntity> txExistente = pagoServicioRepository.findByIdempotencyKey(idempKey.trim());
        if (txExistente.isEmpty()) {
            return null;
        }
        PagoServicioEntity previa = txExistente.get();
        boolean coincideParametros = Objects.equals(previa.getNumeroCuenta(), req.getNumCuenta())
                && Objects.equals(previa.getIdProducto(), req.getIdProducto())
                && previa.getPrecio() != null && req.getPrecio() != null
                && previa.getPrecio().compareTo(req.getPrecio()) == 0;
        if (coincideParametros) {
            return mapearRespuesta(previa, ESTATUS_EXITOSA.equalsIgnoreCase(previa.getEstatusTransaccion()));
        }
        throw new TransaccionDuplicadaException("Clave de idempotencia ya utilizada con parámetros diferentes");
    }

    @Override
    @Transactional
    public PagoServicioResponse conciliarTransaccion(Integer idTransaccion, ConciliacionRequest req) {
        if (idTransaccion == null || req == null || req.getConfirmadaPorProveedor() == null) {
            throw new IllegalArgumentException("Datos de conciliación incompletos");
        }
        PagoServicioEntity tx = pagoServicioRepository.findByIdWithLock(idTransaccion)
                .orElseThrow(() -> new ClienteBusinessException("Transacción no encontrada: " + idTransaccion));

        if (!ESTATUS_PENDIENTE.equalsIgnoreCase(tx.getEstatusTransaccion())
                && !ESTATUS_EN_PROCESO.equalsIgnoreCase(tx.getEstatusTransaccion())) {
            throw new ClienteBusinessException("La transacción no requiere conciliación (estado: " + tx.getEstatusTransaccion() + ")");
        }

        if (Boolean.TRUE.equals(req.getConfirmadaPorProveedor())) {
            PagoServicioEntity ok = actualizarEstado(idTransaccion, ESTATUS_EXITOSA, 0,
                    StringUtils.defaultIfBlank(req.getMotivo(), "Confirmada por conciliación"),
                    StringUtils.defaultIfBlank(req.getFolioProveedor(), tx.getFolioAutorizacion()));
            return mapearRespuesta(ok, true);
        }

        revertirSaldo(idTransaccion, 99, StringUtils.defaultIfBlank(req.getMotivo(), "Saldo reintegrado por conciliación"));
        return mapearRespuesta(obtenerTransaccion(idTransaccion), false);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoServicioResponse consultarEstadoTransaccion(Integer idTransaccion) {
        PagoServicioEntity tx = obtenerTransaccion(idTransaccion);
        return mapearRespuesta(tx, ESTATUS_EXITOSA.equalsIgnoreCase(tx.getEstatusTransaccion()));
    }

    private void validarEntradaYCatalogo(PagoServicioRequest req) {
        if (req == null || StringUtils.isBlank(req.getNumCuenta()) || !req.getNumCuenta().matches("^\\d{10}$")
                || req.getDistribuidor() == null || req.getDistribuidor() <= 0
                || StringUtils.isBlank(req.getTelefonoReferencia()) || !req.getTelefonoReferencia().trim().matches("^[0-9A-Za-z\\-]{6,30}$")) {
            throw new IllegalArgumentException("Parámetros de transacción inválidos o incompletos");
        }
        if (req.getIdServicio() == null || req.getIdServicio() <= 0
                || req.getIdProducto() == null || req.getIdProducto() <= 0
                || req.getPrecio() == null || req.getPrecio().compareTo(BigDecimal.ZERO) <= 0 || req.getPrecio().scale() > 2) {
            throw new MontoInvalidoException("Servicio, producto o monto inválido");
        }
        if (StringUtils.isBlank(req.getPin())) {
            throw new ContrasenaInvalidaException("El PIN o Pass Seguro es obligatorio");
        }

        GestoPagoProductoEntity prod = gestoPagoProductoRepository.findByIdProducto(req.getIdProducto())
                .orElseThrow(() -> new ProductoNoEncontradoException("El producto " + req.getIdProducto() + " no existe en el catálogo"));
        if (!Boolean.TRUE.equals(prod.getActivo())) {
            throw new MontoInvalidoException("El producto solicitado no se encuentra activo");
        }
        if (prod.getIdServicio() != null && !prod.getIdServicio().equals(req.getIdServicio())) {
            throw new MontoInvalidoException("El idServicio no corresponde al producto solicitado en el catálogo");
        }
        if (prod.getMontoMinimo() != null && req.getPrecio().compareTo(prod.getMontoMinimo()) < 0) {
            throw new MontoInvalidoException("El monto es inferior al mínimo permitido (" + prod.getMontoMinimo() + ")");
        }
        if (prod.getMontoMaximo() != null && req.getPrecio().compareTo(prod.getMontoMaximo()) > 0) {
            throw new MontoInvalidoException("El monto excede el máximo permitido (" + prod.getMontoMaximo() + ")");
        }
    }

    private PagoServicioEntity reservarFondos(PagoServicioRequest req, String authHeader, String idempKey, String firmaAes) {
        CuentaEntity cuenta = obtenerCuentaBloqueada(req.getNumCuenta());
        if (!"ACTIVA".equalsIgnoreCase(cuenta.getEstatus())) {
            throw new ClienteBusinessException("La cuenta no se encuentra activa");
        }
        validarTitularYCredenciales(cuenta, req.getClienteId(), req.getPin(), authHeader);
        validarIdempotencia(req, idempKey);

        if (cuenta.getSaldo() == null || cuenta.getSaldo().compareTo(req.getPrecio()) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente en la cuenta");
        }

        BigDecimal saldoAnterior = cuenta.getSaldo();
        BigDecimal saldoPosterior = saldoAnterior.subtract(req.getPrecio());
        cuenta.setSaldo(saldoPosterior);
        cuentaRepository.save(cuenta);

        return pagoServicioRepository.save(PagoServicioEntity.builder()
                .idempotencyKey(idempKey)
                .numeroCuenta(cuenta.getNumeroCuenta())
                .clienteId(cuenta.getCliente().getId())
                .idServicio(req.getIdServicio())
                .idProducto(req.getIdProducto())
                .precio(req.getPrecio())
                .telefonoReferencia(req.getTelefonoReferencia().trim())
                .distribuidor(req.getDistribuidor())
                .bandera(StringUtils.defaultIfBlank(req.getBandera(), "0"))
                .estatusTransaccion(ESTATUS_EN_PROCESO)
                .codigoRespuesta(102)
                .mensajeRespuesta("Fondos reservados en proceso")
                .folioAutorizacion("TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                .firmaAes(firmaAes)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .build());
    }

    private void validarTitularYCredenciales(CuentaEntity cuenta, Integer reqClienteId, String pin, String authHeader) {
        ClienteEntity cliente = cuenta.getCliente();
        if (cliente == null || !Boolean.TRUE.equals(cliente.getActivo())) {
            throw new OperacionNoAutorizadaException("El cliente titular no está activo o autorizado");
        }
        UsuarioEntity usuario = cliente.getUsuario();
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UsuarioInactivoException("El usuario asociado a la cuenta no está activo");
        }
        if (reqClienteId != null && !reqClienteId.equals(cliente.getId())) {
            throw new OperacionNoAutorizadaException("El cliente no tiene autorización sobre esta cuenta");
        }
        validarJwtTitular(authHeader, cliente.getId());
        if (StringUtils.isBlank(usuario.getPassword()) || !passwordEncoder.matches(pin, usuario.getPassword())) {
            throw new ContrasenaInvalidaException("Pass Seguro / PIN inválido");
        }
    }

    private void validarJwtTitular(String authHeader, Integer clienteId) {
        if (StringUtils.isBlank(authHeader)) {
            return;
        }
        try {
            DecodedJWT jwt = securityConfig.validarYDecodificarToken(limpiarBearer(authHeader));
            Integer jwtClienteId = jwt.getClaim("clienteId").asInt();
            if (jwtClienteId != null && !jwtClienteId.equals(clienteId)) {
                throw new OperacionNoAutorizadaException("El cliente autenticado no es titular de la cuenta");
            }
        } catch (OperacionNoAutorizadaException e) {
            throw e;
        } catch (Exception e) {
            throw new CredencialesInvalidasException("Token de sesión inválido o expirado");
        }
    }

    private void validarIdempotencia(PagoServicioRequest req, String idempKey) {
        if (idempKey != null && pagoServicioRepository.findByIdempotencyKey(idempKey).isPresent()) {
            throw new TransaccionDuplicadaException("Solicitud duplicada para la clave de idempotencia enviada");
        }
        boolean duplicadaReciente = pagoServicioRepository
                .existsByNumeroCuentaAndIdServicioAndIdProductoAndTelefonoReferenciaAndPrecioAndEstatusTransaccionInAndFechaCreacionAfter(
                        req.getNumCuenta(), req.getIdServicio(), req.getIdProducto(),
                        req.getTelefonoReferencia().trim(), req.getPrecio(),
                        ESTATUS_ACTIVOS, LocalDateTime.now().minusMinutes(5));
        if (duplicadaReciente) {
            throw new TransaccionDuplicadaException("Operación idéntica en curso o ejecutada recientemente");
        }
    }

    private RuntimeException manejarErrorProveedor(PagoServicioEntity tx, Exception ex) {
        if (ex instanceof GestoPagoAuthException || ex instanceof FeignException.Unauthorized || ex instanceof FeignException.Forbidden) {
            revertirSaldo(tx.getId(), 401, "Error de autenticación con proveedor");
            return new GestoPagoAuthException("Error de autenticación con el proveedor externo", ex);
        }
        Integer status = extraerCodigoHttp(ex);
        boolean timeoutOIndeterminado = (ex instanceof GestoPagoTimeoutException) || (ex instanceof RetryableException)
                || status == null || status == 408 || status == 502 || status == 503 || status == 504 || status < 0;

        if (timeoutOIndeterminado) {
            ejecutarEnTransaccion(() -> actualizarEstado(tx.getId(), ESTATUS_PENDIENTE, 504,
                    "Timeout en proveedor. Saldo retenido pendiente de conciliación", tx.getFolioAutorizacion()));
            return new TransaccionPendienteConciliacionException(
                    "Timeout con el proveedor. Operación " + tx.getFolioAutorizacion()
                            + " en estado PENDIENTE_CONCILIACION. Concilie antes de reintentar.",
                    tx.getFolioAutorizacion(), ex);
        }

        revertirSaldo(tx.getId(), status, "Error determinístico del proveedor");
        return (ex instanceof GestoPagoIntegrationException g) ? g
                : new GestoPagoIntegrationException("Error en el proveedor externo", status, ex);
    }

    private Integer extraerCodigoHttp(Exception ex) {
        if (ex instanceof GestoPagoIntegrationException g) {
            return g.getCodigo();
        }
        if (ex instanceof FeignException f) {
            return f.status();
        }
        return null;
    }

    private void revertirSaldo(Integer idTx, Integer cod, String motivo) {
        if (idTx == null) {
            return;
        }
        ejecutarEnTransaccion(() -> pagoServicioRepository.findByIdWithLock(idTx).ifPresent(tx -> {
            if (ESTATUS_EN_PROCESO.equalsIgnoreCase(tx.getEstatusTransaccion())
                    || ESTATUS_PENDIENTE.equalsIgnoreCase(tx.getEstatusTransaccion())) {
                CuentaEntity cuenta = obtenerCuentaBloqueada(tx.getNumeroCuenta());
                cuenta.setSaldo(cuenta.getSaldo().add(tx.getPrecio()));
                cuentaRepository.save(cuenta);
                tx.setEstatusTransaccion(ESTATUS_REVERSADA);
                tx.setCodigoRespuesta(cod != null ? cod : 99);
                tx.setMensajeRespuesta(StringUtils.abbreviate(motivo, 250));
                tx.setSaldoPosterior(cuenta.getSaldo());
                pagoServicioRepository.save(tx);
            }
        }));
    }

    private PagoServicioEntity actualizarEstado(Integer idTx, String estatus, int codigo, String msg, String folio) {
        PagoServicioEntity tx = obtenerTransaccion(idTx);
        tx.setEstatusTransaccion(estatus);
        tx.setCodigoRespuesta(codigo);
        tx.setMensajeRespuesta(StringUtils.abbreviate(msg, 250));
        tx.setFolioAutorizacion(folio);
        return pagoServicioRepository.save(tx);
    }

    private CuentaEntity obtenerCuentaBloqueada(String numCuenta) {
        return cuentaRepository.findByNumeroCuentaWithLock(numCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("Cuenta bancaria no encontrada"));
    }

    private PagoServicioEntity obtenerTransaccion(Integer idTx) {
        return pagoServicioRepository.findById(idTx)
                .orElseThrow(() -> new ClienteBusinessException("Transacción no encontrada: " + idTx));
    }

    private String cifrarPayload(PagoServicioRequest req) {
        try {
            GestoPagoTxPayload payload = GestoPagoTxPayload.builder()
                    .idProducto(req.getIdProducto())
                    .idServicio(req.getIdServicio())
                    .telefonoReferencia(req.getTelefonoReferencia().trim())
                    .horaLocal(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                    .montoPago(req.getPrecio())
                    .upc(String.format("%012d", req.getIdProducto()))
                    .unidad(1)
                    .build();
            return aesEncryptionService.cifrar(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error al serializar payload", e);
        }
    }

    private String resolverBearerToken(Integer dist, String disp) {
        if (gestoPagoTokenService != null) {
            try {
                Optional<GestoPagoToken> tok = gestoPagoTokenService.obtenerTokenActivo(dist, disp);
                if (tok.isPresent() && StringUtils.isNotBlank(tok.get().getToken())) return tok.get().getToken().trim();
                GestoPagoToken asegurado = gestoPagoTokenService.asegurarTokenActivo(dist, disp);
                if (asegurado != null && StringUtils.isNotBlank(asegurado.getToken())) return asegurado.getToken().trim();
            } catch (Exception ignored) {
                log.debug("Usando token de configuración para distribuidor={}", dist);
            }
        }
        if (StringUtils.isNotBlank(configuredBearerToken)) return configuredBearerToken.trim();
        throw new GestoPagoAuthException("No existe Bearer Token disponible para autenticar con el proveedor");
    }

    private String limpiarBearer(String token) {
        String clean = StringUtils.trimToEmpty(token);
        return clean.toLowerCase().startsWith("bearer ") ? clean.substring(7).trim() : clean;
    }

    private <T> T enTransaccion(Supplier<T> action) {
        if (transactionTemplate == null) {
            return Objects.requireNonNull(action.get(), "Resultado transaccional nulo");
        }
        return Objects.requireNonNull(transactionTemplate.execute(s -> action.get()), "Resultado transaccional nulo");
    }

    private void ejecutarEnTransaccion(Runnable action) {
        if (transactionTemplate == null) {
            action.run();
            return;
        }
        transactionTemplate.executeWithoutResult(s -> action.run());
    }

    private static TransactionTemplate crearTxTemplate(PlatformTransactionManager tm) {
        TransactionTemplate tt = new TransactionTemplate(tm);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return tt;
    }

    private PagoServicioResponse mapearRespuesta(PagoServicioEntity tx, boolean exitoso) {
        return PagoServicioResponse.builder()
                .idTransaccion(tx.getId())
                .exitoso(exitoso)
                .estatusTransaccion(tx.getEstatusTransaccion())
                .codigo(tx.getCodigoRespuesta())
                .mensaje(tx.getMensajeRespuesta())
                .folioTransaccion(tx.getFolioAutorizacion())
                .numeroCuenta(tx.getNumeroCuenta())
                .precioPagado(tx.getPrecio())
                .saldoAnterior(tx.getSaldoAnterior())
                .saldoRestante(tx.getSaldoPosterior())
                .telefonoReferencia(tx.getTelefonoReferencia())
                .fechaHora(tx.getFechaActualizacion() != null ? tx.getFechaActualizacion() : tx.getFechaCreacion())
                .build();
    }
}
