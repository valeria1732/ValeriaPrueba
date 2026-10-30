package com.proyecto.servicios.service.Impl;

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
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@Slf4j
public class PagoServicioServiceImpl implements PagoServicioService {

    private static final int VENTANA_DUPLICADOS_MINUTOS = 5;
    private static final List<String> ESTATUS_NO_DUPLICABLES = List.of("EN_PROCESO", "PENDIENTE_CONCILIACION", "EXITOSA");

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
            @Autowired(required = false) GestoPagoProductoRepository gestoPagoProductoRepository,
            @Autowired(required = false) SecurityConfig securityConfig,
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
        if (transactionManager != null) {
            this.transactionTemplate = new TransactionTemplate(transactionManager);
            this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        } else {
            this.transactionTemplate = null;
        }
    }

    @Override
    public PagoServicioResponse procesarPagoServicio(PagoServicioRequest request) {
        return procesarPagoServicio(request, null, null);
    }

    @Override
    public PagoServicioResponse procesarPagoServicio(
            PagoServicioRequest request,
            String authorizationHeader,
            String headerIdempotencyKey) {

        // 1. Validación estricta de parámetros de entrada
        validarParametrosEntrada(request);

        String claveIdempotencia = StringUtils.isNotBlank(headerIdempotencyKey)
                ? headerIdempotencyKey.trim()
                : (StringUtils.isNotBlank(request.getIdempotencyKey()) ? request.getIdempotencyKey().trim() : null);

        log.info("Iniciando operación de pago de servicio: cuenta={}, idServicio={}, idProducto={}, monto={}, referencia={}",
                enmascararCuenta(request.getNumCuenta()), request.getIdServicio(),
                request.getIdProducto(), request.getPrecio(), enmascararReferencia(request.getTelefonoReferencia()));

        // 2. Validar existencia del producto en el catálogo y restricciones de monto
        validarCatalogoYMonto(request);

        // 3. Construcción y cifrado AES del payload antes de iniciar reserva
        String firmaAes = construirYCifrarPayload(request);

        // 4. Obtener y validar Bearer Token seguro (sin exponerlo en logs)
        String codigoDispositivo = StringUtils.isNotBlank(defaultCodigoDispositivo) ? defaultCodigoDispositivo : "DEV_TEST_001";
        String bearerToken = resolverBearerTokenSeguro(request.getDistribuidor(), codigoDispositivo);
        String authHeaderProveedor = formatearBearerHeader(bearerToken);

        // 5. FASE 1 (Transacción Atómica Local): Bloqueo Pesimista de Cuenta, Validación de Cliente/Saldo/Idempotencia y Reserva/Débito Preventivo
        PagoServicioEntity transaccionEnProceso = ejecutarEnTransaccion(() ->
                reservarSaldoYRegistrarTransaccionEnProceso(request, authorizationHeader, claveIdempotencia, firmaAes)
        );

        // 6. FASE 2 (Comunicación Externa): Invocación a POST /sistema/service/sendTx.do
        GestoPagoTxRequest txRequest = GestoPagoTxRequest.builder()
                .payloadFirmado(firmaAes)
                .token(limpiarPrefijoBearer(bearerToken))
                .idDistribuidor(request.getDistribuidor())
                .codigoDispositivo(codigoDispositivo)
                .bandera(StringUtils.isNotBlank(request.getBandera()) ? request.getBandera() : "0")
                .build();

        GestoPagoTxResponse txResponse;
        try {
            log.info("Enviando transacción ID={} (folio interno={}) al proveedor GestoPago",
                    transaccionEnProceso.getId(), transaccionEnProceso.getFolioAutorizacion());
            txResponse = transactionClient.sendTx(authHeaderProveedor, txRequest);
        } catch (GestoPagoTimeoutException | RetryableException ex) {
            // REGLA FINANCIERA CRÍTICA: Ante timeout NO asumir que la operación falló ni reintentar a ciegas.
            // Se conserva la reserva del saldo y se marca como PENDIENTE_CONCILIACION.
            marcarPendienteConciliacion(transaccionEnProceso.getId(),
                    "Timeout al esperar respuesta de GestoPago. Operación retenida para conciliación");
            throw new TransaccionPendienteConciliacionException(
                    "Tiempo de espera agotado con el proveedor externo. La operación con folio "
                            + transaccionEnProceso.getFolioAutorizacion()
                            + " quedó en estado PENDIENTE_CONCILIACION. No reintente el cobro antes de conciliar.",
                    transaccionEnProceso.getFolioAutorizacion(),
                    ex
            );
        } catch (GestoPagoAuthException | FeignException.Unauthorized | FeignException.Forbidden ex) {
            // El proveedor rechazó la autenticación antes de procesar la transacción -> Reverso seguro del saldo
            revertirSaldoPorRechazoConfirmado(transaccionEnProceso.getId(), 401,
                    "Rechazo de autenticación en proveedor externo");
            throw new GestoPagoAuthException("Error de autenticación con el proveedor de pagos externo", ex);
        } catch (GestoPagoIntegrationException ex) {
            if (esEstadoIndeterminado(ex.getCodigo())) {
                marcarPendienteConciliacion(transaccionEnProceso.getId(),
                        "Respuesta indeterminada del proveedor (HTTP " + ex.getCodigo() + "). Requiere conciliación");
                throw new TransaccionPendienteConciliacionException(
                        "El proveedor devolvió un estado indeterminado (HTTP " + ex.getCodigo()
                                + "). La transacción con folio " + transaccionEnProceso.getFolioAutorizacion()
                                + " se encuentra PENDIENTE_CONCILIACION.",
                        transaccionEnProceso.getFolioAutorizacion(),
                        ex
                );
            }
            revertirSaldoPorRechazoConfirmado(transaccionEnProceso.getId(),
                    ex.getCodigo() != null ? ex.getCodigo() : 502,
                    "Error determinístico reportado por proveedor externo");
            throw ex;
        } catch (FeignException ex) {
            if (esEstadoIndeterminado(ex.status())) {
                marcarPendienteConciliacion(transaccionEnProceso.getId(),
                        "Fallo de red/gateway (HTTP " + ex.status() + "). Requiere conciliación");
                throw new TransaccionPendienteConciliacionException(
                        "Error de comunicación con el proveedor (HTTP " + ex.status()
                                + "). La transacción con folio " + transaccionEnProceso.getFolioAutorizacion()
                                + " se encuentra PENDIENTE_CONCILIACION.",
                        transaccionEnProceso.getFolioAutorizacion(),
                        ex
                );
            }
            revertirSaldoPorRechazoConfirmado(transaccionEnProceso.getId(),
                    ex.status() > 0 ? ex.status() : 502,
                    "Rechazo HTTP en invocación al proveedor externo");
            throw new GestoPagoIntegrationException("Error al procesar la solicitud con el proveedor externo", ex.status(), ex);
        } catch (Exception ex) {
            // Ante cualquier error de red inesperado post-envío, proteger la consistencia dejando pendiente de conciliación
            marcarPendienteConciliacion(transaccionEnProceso.getId(),
                    "Error de comunicación durante la invocación al proveedor. Requiere conciliación");
            throw new TransaccionPendienteConciliacionException(
                    "No fue posible confirmar el resultado de la operación con el proveedor. Folio "
                            + transaccionEnProceso.getFolioAutorizacion() + " en estado PENDIENTE_CONCILIACION.",
                    transaccionEnProceso.getFolioAutorizacion(),
                    ex
            );
        }

        // 7. FASE 3 (Resolución de Respuesta de Negocio del Proveedor)
        if (txResponse != null && txResponse.getCodigo() != null && txResponse.getCodigo() == 0) {
            PagoServicioEntity confirmada = ejecutarEnTransaccion(() ->
                    confirmarTransaccionExitosa(transaccionEnProceso.getId(), txResponse)
            );
            log.info("Transacción ID={} completada exitosamente con folio={}",
                    confirmada.getId(), confirmada.getFolioAutorizacion());
            return mapearRespuesta(confirmada, true);
        } else {
            Integer codigoRechazo = (txResponse != null && txResponse.getCodigo() != null) ? txResponse.getCodigo() : 99;
            String mensajeRechazo = (txResponse != null && StringUtils.isNotBlank(txResponse.getMensaje()))
                    ? txResponse.getMensaje()
                    : "Rechazado por el proveedor de servicios";
            log.warn("Proveedor rechazó explícitamente la transacción ID={} con código {}. Ejecutando reverso automático de saldo.",
                    transaccionEnProceso.getId(), codigoRechazo);
            revertirSaldoPorRechazoConfirmado(transaccionEnProceso.getId(), codigoRechazo, mensajeRechazo);
            throw new ClienteBusinessException("Transacción rechazada por el proveedor (Código " + codigoRechazo + "): " + mensajeRechazo);
        }
    }

    @Override
    @Transactional
    public PagoServicioResponse conciliarTransaccion(Integer idTransaccion, ConciliacionRequest request) {
        if (idTransaccion == null || request == null || request.getConfirmadaPorProveedor() == null) {
            throw new IllegalArgumentException("Los datos de conciliación son obligatorios");
        }

        PagoServicioEntity tx = pagoServicioRepository.findById(idTransaccion)
                .orElseThrow(() -> new ClienteBusinessException("No se encontró la transacción con ID: " + idTransaccion));

        if (!"PENDIENTE_CONCILIACION".equalsIgnoreCase(tx.getEstatusTransaccion())
                && !"EN_PROCESO".equalsIgnoreCase(tx.getEstatusTransaccion())) {
            throw new ClienteBusinessException("La transacción con ID " + idTransaccion
                    + " no requiere conciliación (estatus actual: " + tx.getEstatusTransaccion() + ")");
        }

        if (Boolean.TRUE.equals(request.getConfirmadaPorProveedor())) {
            tx.setEstatusTransaccion("EXITOSA");
            tx.setCodigoRespuesta(0);
            if (StringUtils.isNotBlank(request.getFolioProveedor())) {
                tx.setFolioAutorizacion(request.getFolioProveedor().trim());
            }
            tx.setMensajeRespuesta(StringUtils.isNotBlank(request.getMotivo())
                    ? request.getMotivo()
                    : "Transacción confirmada mediante conciliación con proveedor");
            PagoServicioEntity guardada = pagoServicioRepository.save(tx);
            log.info("Conciliación completada: Transacción ID={} confirmada como EXITOSA (folio={})",
                    guardada.getId(), guardada.getFolioAutorizacion());
            return mapearRespuesta(guardada, true);
        } else {
            CuentaEntity cuenta = obtenerCuentaConBloqueo(tx.getNumeroCuenta());
            BigDecimal saldoRestaurado = cuenta.getSaldo().add(tx.getPrecio());
            cuenta.setSaldo(saldoRestaurado);
            cuentaRepository.save(cuenta);

            tx.setEstatusTransaccion("REVERSADA");
            tx.setCodigoRespuesta(99);
            tx.setSaldoPosterior(saldoRestaurado);
            tx.setMensajeRespuesta(StringUtils.isNotBlank(request.getMotivo())
                    ? request.getMotivo()
                    : "Transacción no aplicada en proveedor; saldo reintegrado por conciliación");
            PagoServicioEntity guardada = pagoServicioRepository.save(tx);
            log.info("Conciliación completada: Transacción ID={} marcada como REVERSADA y saldo reintegrado a la cuenta {}",
                    guardada.getId(), enmascararCuenta(cuenta.getNumeroCuenta()));
            return mapearRespuesta(guardada, false);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagoServicioResponse consultarEstadoTransaccion(Integer idTransaccion) {
        PagoServicioEntity tx = pagoServicioRepository.findById(idTransaccion)
                .orElseThrow(() -> new ClienteBusinessException("No se encontró la transacción con ID: " + idTransaccion));
        return mapearRespuesta(tx, "EXITOSA".equalsIgnoreCase(tx.getEstatusTransaccion()));
    }

    // =========================================================================
    // Métodos Privados de Validación y Consistencia Transaccional
    // =========================================================================

    private void validarParametrosEntrada(PagoServicioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de pago no puede ser nula");
        }
        if (StringUtils.isBlank(request.getNumCuenta()) || !request.getNumCuenta().matches("^\\d{10}$")) {
            throw new IllegalArgumentException("El número de cuenta debe contener exactamente 10 dígitos numéricos");
        }
        if (request.getIdServicio() == null || request.getIdServicio() <= 0) {
            throw new MontoInvalidoException("El identificador del servicio (idServicio) es inválido");
        }
        if (request.getIdProducto() == null || request.getIdProducto() <= 0) {
            throw new MontoInvalidoException("El identificador del producto (idProducto) es inválido");
        }
        if (request.getPrecio() == null || request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoInvalidoException("El monto de la operación debe ser mayor a cero");
        }
        if (request.getPrecio().scale() > 2) {
            throw new MontoInvalidoException("El monto de la operación no puede tener más de 2 decimales");
        }
        if (request.getDistribuidor() == null || request.getDistribuidor() <= 0) {
            throw new IllegalArgumentException("El identificador de distribuidor es inválido");
        }
        if (StringUtils.isBlank(request.getTelefonoReferencia())
                || !request.getTelefonoReferencia().trim().matches("^[0-9A-Za-z\\-]{6,30}$")) {
            throw new IllegalArgumentException("La referencia o teléfono de pago tiene un formato inválido");
        }
        if (StringUtils.isBlank(request.getPin())) {
            throw new ContrasenaInvalidaException("El PIN o Pass Seguro es obligatorio para autorizar la transacción");
        }
    }

    private void validarCatalogoYMonto(PagoServicioRequest request) {
        if (gestoPagoProductoRepository == null) {
            return;
        }

        GestoPagoProductoEntity producto = gestoPagoProductoRepository.findByIdProducto(request.getIdProducto())
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "El producto con ID " + request.getIdProducto() + " no existe en el catálogo autorizado"));

        if (Boolean.FALSE.equals(producto.getActivo())) {
            throw new MontoInvalidoException(
                    "El producto solicitado (" + producto.getCodigo() + ") no se encuentra activo para operaciones");
        }

        if (producto.getIdServicio() != null && !producto.getIdServicio().equals(request.getIdServicio())) {
            throw new MontoInvalidoException(
                    "El idServicio (" + request.getIdServicio() + ") no corresponde con el servicio registrado para el producto ("
                            + producto.getIdServicio() + ")");
        }

        if (producto.getMontoMinimo() != null && request.getPrecio().compareTo(producto.getMontoMinimo()) < 0) {
            throw new MontoInvalidoException(
                    "El monto solicitado (" + request.getPrecio() + ") es inferior al monto mínimo permitido ("
                            + producto.getMontoMinimo() + ") para el producto " + producto.getCodigo());
        }

        if (producto.getMontoMaximo() != null && request.getPrecio().compareTo(producto.getMontoMaximo()) > 0) {
            throw new MontoInvalidoException(
                    "El monto solicitado (" + request.getPrecio() + ") excede el monto máximo permitido ("
                            + producto.getMontoMaximo() + ") para el producto " + producto.getCodigo());
        }
    }

    private PagoServicioEntity reservarSaldoYRegistrarTransaccionEnProceso(
            PagoServicioRequest request,
            String authorizationHeader,
            String claveIdempotencia,
            String firmaAes) {

        // 1. Adquirir bloqueo pesimista (SELECT ... FOR UPDATE) sobre la cuenta para serializar concurrencia
        CuentaEntity cuenta = obtenerCuentaConBloqueo(request.getNumCuenta());

        // 2. Verificar que la cuenta esté ACTIVA
        if (!"ACTIVA".equalsIgnoreCase(cuenta.getEstatus())) {
            log.warn("Intento de operación en cuenta no activa: cuenta={}, estatus={}",
                    enmascararCuenta(cuenta.getNumeroCuenta()), cuenta.getEstatus());
            throw new ClienteBusinessException("La cuenta bancaria no se encuentra activa (estatus actual: " + cuenta.getEstatus() + ")");
        }

        // 3. Validar identidad del cliente titular, estado activo y autorización
        validarIdentidadYAutorizacionCliente(cuenta, request, authorizationHeader);

        // 4. Validar Pass Seguro / PIN del cliente
        validarPassSeguro(cuenta, request.getPin());

        // 5. Verificar que no existan solicitudes duplicadas (Idempotencia y ventana temporal)
        validarNoDuplicidad(request, claveIdempotencia);

        // 6. Verificar saldo disponible suficiente
        if (cuenta.getSaldo() == null || cuenta.getSaldo().compareTo(request.getPrecio()) < 0) {
            log.warn("Saldo insuficiente para la cuenta {}", enmascararCuenta(cuenta.getNumeroCuenta()));
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente en la cuenta para realizar el cargo solicitado de " + request.getPrecio());
        }

        // 7. Descuento preventivo del saldo (Reserva en Fase 1)
        BigDecimal saldoAnterior = cuenta.getSaldo();
        BigDecimal saldoPosterior = saldoAnterior.subtract(request.getPrecio());
        cuenta.setSaldo(saldoPosterior);
        cuentaRepository.save(cuenta);

        String folioInterno = "TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        PagoServicioEntity registroInicial = PagoServicioEntity.builder()
                .idempotencyKey(claveIdempotencia)
                .numeroCuenta(cuenta.getNumeroCuenta())
                .clienteId(cuenta.getCliente() != null ? cuenta.getCliente().getId() : request.getClienteId())
                .idServicio(request.getIdServicio())
                .idProducto(request.getIdProducto())
                .precio(request.getPrecio())
                .telefonoReferencia(request.getTelefonoReferencia().trim())
                .distribuidor(request.getDistribuidor())
                .bandera(StringUtils.isNotBlank(request.getBandera()) ? request.getBandera() : "0")
                .estatusTransaccion("EN_PROCESO")
                .codigoRespuesta(102)
                .mensajeRespuesta("Fondos reservados; transacción en proceso de envío a proveedor")
                .folioAutorizacion(folioInterno)
                .firmaAes(firmaAes)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .build();

        return pagoServicioRepository.save(registroInicial);
    }

    private void validarIdentidadYAutorizacionCliente(
            CuentaEntity cuenta,
            PagoServicioRequest request,
            String authorizationHeader) {

        ClienteEntity cliente = cuenta.getCliente();
        if (cliente == null) {
            throw new OperacionNoAutorizadaException("La cuenta bancaria no tiene un cliente titular válido asociado");
        }

        if (Boolean.FALSE.equals(cliente.getActivo())) {
            throw new OperacionNoAutorizadaException("El cliente titular de la cuenta se encuentra inactivo y no está autorizado para operar");
        }

        UsuarioEntity usuario = cliente.getUsuario();
        if (usuario != null && Boolean.FALSE.equals(usuario.getActivo())) {
            throw new UsuarioInactivoException("El usuario asociado al cliente se encuentra inactivo");
        }

        // Si se envía clienteId en el request, debe coincidir con el titular real de la cuenta
        if (request.getClienteId() != null && !request.getClienteId().equals(cliente.getId())) {
            log.warn("Intento de operación no autorizada: clienteId={} intentó operar cuenta del titular={}",
                    request.getClienteId(), cliente.getId());
            throw new OperacionNoAutorizadaException("El cliente no tiene autorización para realizar operaciones sobre esta cuenta");
        }

        // Si se envía JWT Bearer del cliente en el header Authorization, validar firma y titularidad de la cuenta
        if (StringUtils.isNotBlank(authorizationHeader) && securityConfig != null) {
            String rawJwt = limpiarPrefijoBearer(authorizationHeader);
            try {
                DecodedJWT decodedJWT = securityConfig.validarYDecodificarToken(rawJwt);
                Integer jwtClienteId = decodedJWT.getClaim("clienteId").asInt();
                if (jwtClienteId != null && !jwtClienteId.equals(cliente.getId())) {
                    log.warn("Token JWT pertenece al clienteId={}, pero la cuenta pertenece al clienteId={}",
                            jwtClienteId, cliente.getId());
                    throw new OperacionNoAutorizadaException("El cliente autenticado no es titular ni tiene autorización sobre la cuenta");
                }
            } catch (OperacionNoAutorizadaException e) {
                throw e;
            } catch (Exception e) {
                throw new CredencialesInvalidasException("El token de sesión del cliente es inválido o ha expirado");
            }
        }
    }

    private void validarNoDuplicidad(PagoServicioRequest request, String claveIdempotencia) {
        if (StringUtils.isNotBlank(claveIdempotencia)) {
            Optional<PagoServicioEntity> existentePorKey = pagoServicioRepository.findByIdempotencyKey(claveIdempotencia);
            if (existentePorKey.isPresent()) {
                PagoServicioEntity previo = existentePorKey.get();
                throw new TransaccionDuplicadaException(
                        "Solicitud duplicada detectada (idempotencyKey ya registrada con folio "
                                + previo.getFolioAutorizacion() + " en estado " + previo.getEstatusTransaccion() + ")");
            }
        }

        LocalDateTime limiteVentana = LocalDateTime.now().minusMinutes(VENTANA_DUPLICADOS_MINUTOS);
        boolean existeOperacionReciente = pagoServicioRepository
                .existsByNumeroCuentaAndIdServicioAndIdProductoAndTelefonoReferenciaAndPrecioAndEstatusTransaccionInAndFechaCreacionAfter(
                        request.getNumCuenta(),
                        request.getIdServicio(),
                        request.getIdProducto(),
                        request.getTelefonoReferencia().trim(),
                        request.getPrecio(),
                        ESTATUS_NO_DUPLICABLES,
                        limiteVentana
                );

        if (existeOperacionReciente) {
            throw new TransaccionDuplicadaException(
                    "Se detectó una operación idéntica reciente o en curso para la misma cuenta, producto, referencia y monto. "
                            + "Por seguridad se bloqueó el cargo duplicado.");
        }
    }

    private void validarPassSeguro(CuentaEntity cuenta, String pin) {
        if (StringUtils.isBlank(pin)) {
            throw new ContrasenaInvalidaException("El PIN o Pass Seguro es obligatorio para autorizar la transacción");
        }

        if (cuenta.getCliente() != null && cuenta.getCliente().getUsuario() != null) {
            String hashPassword = cuenta.getCliente().getUsuario().getPassword();
            if (StringUtils.isNotBlank(hashPassword) && passwordEncoder.matches(pin, hashPassword)) {
                return;
            }
            throw new ContrasenaInvalidaException("Pass Seguro / PIN inválido. Transacción no autorizada");
        }

        throw new OperacionNoAutorizadaException("El titular de la cuenta no cuenta con credenciales de autorización activas");
    }

    private PagoServicioEntity confirmarTransaccionExitosa(Integer idTransaccion, GestoPagoTxResponse txResponse) {
        PagoServicioEntity tx = pagoServicioRepository.findById(idTransaccion)
                .orElseThrow(() -> new IllegalStateException("No se encontró el registro de transacción en proceso: " + idTransaccion));

        String folioFinal = StringUtils.isNotBlank(txResponse.getFolio()) ? txResponse.getFolio()
                : (StringUtils.isNotBlank(txResponse.getAutorizacion()) ? txResponse.getAutorizacion() : tx.getFolioAutorizacion());

        tx.setEstatusTransaccion("EXITOSA");
        tx.setCodigoRespuesta(0);
        tx.setMensajeRespuesta(StringUtils.isNotBlank(txResponse.getMensaje())
                ? txResponse.getMensaje()
                : "Pago procesado y saldo debitado exitosamente");
        tx.setFolioAutorizacion(folioFinal);

        return pagoServicioRepository.save(tx);
    }

    private void revertirSaldoPorRechazoConfirmado(Integer idTransaccion, Integer codigoError, String motivo) {
        if (idTransaccion == null) {
            return;
        }
        ejecutarEnTransaccion(() -> {
            pagoServicioRepository.findById(idTransaccion).ifPresent(tx -> {
                if ("EN_PROCESO".equalsIgnoreCase(tx.getEstatusTransaccion())) {
                    CuentaEntity cuenta = obtenerCuentaConBloqueo(tx.getNumeroCuenta());
                    BigDecimal saldoRestaurado = cuenta.getSaldo().add(tx.getPrecio());
                    cuenta.setSaldo(saldoRestaurado);
                    cuentaRepository.save(cuenta);

                    tx.setEstatusTransaccion("REVERSADA");
                    tx.setCodigoRespuesta(codigoError != null ? codigoError : 99);
                    tx.setMensajeRespuesta(StringUtils.abbreviate(motivo, 250));
                    tx.setSaldoPosterior(saldoRestaurado);
                    pagoServicioRepository.save(tx);

                    log.info("Compensación aplicada: Se reintegró el monto {} a la cuenta {} tras rechazo confirmado del proveedor",
                            tx.getPrecio(), enmascararCuenta(cuenta.getNumeroCuenta()));
                }
            });
            return null;
        });
    }

    private void marcarPendienteConciliacion(Integer idTransaccion, String detalle) {
        if (idTransaccion == null) {
            return;
        }
        ejecutarEnTransaccion(() -> {
            pagoServicioRepository.findById(idTransaccion).ifPresent(tx -> {
                tx.setEstatusTransaccion("PENDIENTE_CONCILIACION");
                tx.setCodigoRespuesta(504);
                tx.setMensajeRespuesta(StringUtils.abbreviate(detalle, 250));
                pagoServicioRepository.save(tx);
            });
            return null;
        });
    }

    private CuentaEntity obtenerCuentaConBloqueo(String numeroCuenta) {
        Optional<CuentaEntity> conLock = cuentaRepository.findByNumeroCuentaWithLock(numeroCuenta);
        if (conLock.isPresent()) {
            return conLock.get();
        }
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("No se encontró la cuenta bancaria solicitada"));
    }

    private String construirYCifrarPayload(PagoServicioRequest request) {
        String horaLocal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        GestoPagoTxPayload payload = GestoPagoTxPayload.builder()
                .idProducto(request.getIdProducto())
                .idServicio(request.getIdServicio())
                .telefonoReferencia(request.getTelefonoReferencia().trim())
                .horaLocal(horaLocal)
                .montoPago(request.getPrecio())
                .upc(String.format("%012d", request.getIdProducto()))
                .unidad(1)
                .build();

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            return aesEncryptionService.cifrar(jsonPayload);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar el payload de transacción");
            throw new IllegalStateException("Error interno al preparar la firma criptográfica de la operación", e);
        }
    }

    private String resolverBearerTokenSeguro(Integer distribuidor, String codigoDispositivo) {
        if (gestoPagoTokenService != null) {
            try {
                Optional<GestoPagoToken> tokenOpt = gestoPagoTokenService.obtenerTokenActivo(distribuidor, codigoDispositivo);
                if (tokenOpt.isPresent() && StringUtils.isNotBlank(tokenOpt.get().getToken())) {
                    return tokenOpt.get().getToken().trim();
                }
                GestoPagoToken asegurado = gestoPagoTokenService.asegurarTokenActivo(distribuidor, codigoDispositivo);
                if (asegurado != null && StringUtils.isNotBlank(asegurado.getToken())) {
                    return asegurado.getToken().trim();
                }
            } catch (Exception e) {
                log.warn("No fue posible recuperar token desde repositorio para distribuidor {}: se evaluará configuración segura", distribuidor);
            }
        }

        if (StringUtils.isNotBlank(configuredBearerToken)) {
            return configuredBearerToken.trim();
        }

        throw new GestoPagoAuthException("No se encontró un Bearer Token válido configurado para autenticar con el proveedor externo");
    }

    private String formatearBearerHeader(String token) {
        String clean = token.trim();
        return clean.toLowerCase().startsWith("bearer ") ? clean : "Bearer " + clean;
    }

    private String limpiarPrefijoBearer(String token) {
        if (token == null) return "";
        String clean = token.trim();
        return clean.toLowerCase().startsWith("bearer ") ? clean.substring(7).trim() : clean;
    }

    private boolean esEstadoIndeterminado(Integer httpStatus) {
        if (httpStatus == null) return true;
        return httpStatus == 408 || httpStatus == 502 || httpStatus == 503 || httpStatus == 504 || httpStatus < 0;
    }

    private <T> T ejecutarEnTransaccion(Supplier<T> action) {
        if (transactionTemplate != null) {
            return transactionTemplate.execute(status -> action.get());
        }
        return action.get();
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

    private String enmascararCuenta(String numCuenta) {
        if (numCuenta == null || numCuenta.length() < 4) {
            return "****";
        }
        return "******" + numCuenta.substring(numCuenta.length() - 4);
    }

    private String enmascararReferencia(String ref) {
        if (ref == null || ref.length() < 4) {
            return "****";
        }
        return "***" + ref.substring(ref.length() - 4);
    }
}
