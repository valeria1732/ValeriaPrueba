package com.proyecto.servicios.service;

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
import com.proyecto.servicios.model.pago.ConciliacionRequest;
import com.proyecto.servicios.model.pago.GestoPagoTxRequest;
import com.proyecto.servicios.model.pago.GestoPagoTxResponse;
import com.proyecto.servicios.model.pago.PagoServicioRequest;
import com.proyecto.servicios.model.pago.PagoServicioResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.repositorys.sf.PagoServicioRepository;
import com.proyecto.servicios.service.Impl.PagoServicioServiceImpl;
import com.proyecto.servicios.util.AesEncryptionService;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServicioServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private GestoPagoTokenService gestoPagoTokenService;

    @Mock
    private GestoPagoTransactionClient transactionClient;

    @Mock
    private PagoServicioRepository pagoServicioRepository;

    @Mock
    private GestoPagoProductoRepository gestoPagoProductoRepository;

    private AesEncryptionService aesEncryptionService;
    private PasswordEncoder passwordEncoder;
    private ObjectMapper objectMapper;
    private SecurityConfig securityConfig;
    private PagoServicioServiceImpl pagoServicioService;

    private CuentaEntity cuentaActiva;
    private ClienteEntity clienteActivo;
    private UsuarioEntity usuarioActivo;
    private GestoPagoProductoEntity productoCatalogo;
    private PagoServicioRequest validRequest;
    private GestoPagoToken tokenActivo;

    @BeforeEach
    void setUp() {
        aesEncryptionService = new AesEncryptionService(
                "GestoPagoAESSecretKey2026Secure32",
                "AES/CBC/PKCS5Padding",
                "1234567890123456"
        );
        passwordEncoder = new BCryptPasswordEncoder();
        objectMapper = new ObjectMapper();
        securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "jwtSecret", "SuperSecretJwtSecretKeyForFinancialOnboarding2026!");
        ReflectionTestUtils.setField(securityConfig, "jwtExpirationMs", 86400000L);

        pagoServicioService = new PagoServicioServiceImpl(
                cuentaRepository,
                gestoPagoTokenService,
                transactionClient,
                aesEncryptionService,
                passwordEncoder,
                objectMapper,
                pagoServicioRepository,
                gestoPagoProductoRepository,
                securityConfig,
                null
        );
        ReflectionTestUtils.setField(pagoServicioService, "defaultCodigoDispositivo", "DEV_TEST_001");
        ReflectionTestUtils.setField(pagoServicioService, "configuredBearerToken", "env_fallback_token");

        usuarioActivo = UsuarioEntity.builder()
                .id(10)
                .correo("cliente@banco.com")
                .password(passwordEncoder.encode("Password123!"))
                .activo(true)
                .build();

        clienteActivo = ClienteEntity.builder()
                .id(1)
                .nombre("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("López")
                .activo(true)
                .usuario(usuarioActivo)
                .build();
        usuarioActivo.setCliente(clienteActivo);

        cuentaActiva = CuentaEntity.builder()
                .id(100)
                .cliente(clienteActivo)
                .numeroCuenta("0019547596")
                .clabe("012180001954759601")
                .saldo(new BigDecimal("1500.00"))
                .estatus("ACTIVA")
                .build();

        productoCatalogo = GestoPagoProductoEntity.builder()
                .id(1)
                .idProducto(202)
                .idServicio(101)
                .codigo("PROD-202")
                .descripcion("Pago de Servicio CFE")
                .montoMinimo(new BigDecimal("10.00"))
                .montoMaximo(new BigDecimal("5000.00"))
                .activo(true)
                .build();

        validRequest = PagoServicioRequest.builder()
                .numCuenta("0019547596")
                .clienteId(1)
                .idServicio(101)
                .idProducto(202)
                .precio(new BigDecimal("350.00"))
                .distribuidor(1001)
                .telefonoReferencia("5512345678")
                .bandera("0")
                .idempotencyKey("IDEMP-0001")
                .pin("Password123!")
                .build();

        tokenActivo = new GestoPagoToken();
        tokenActivo.setId(1);
        tokenActivo.setIdDistribuidor(1001);
        tokenActivo.setCodigoDispositivo("DEV_TEST_001");
        tokenActivo.setToken("secure_bearer_token_2026");
        tokenActivo.setActivo(true);
    }

    @Test
    @DisplayName("Flujo completo exitoso: reserva saldo, invoca SendTx con Bearer Token y confirma transacción EXITOSA")
    void testProcesarPagoServicio_Exito() {
        when(gestoPagoProductoRepository.findByIdProducto(202)).thenReturn(Optional.of(productoCatalogo));
        when(gestoPagoTokenService.obtenerTokenActivo(1001, "DEV_TEST_001")).thenReturn(Optional.of(tokenActivo));
        when(cuentaRepository.findByNumeroCuentaWithLock("0019547596")).thenReturn(Optional.of(cuentaActiva));
        when(pagoServicioRepository.findByIdempotencyKey("IDEMP-0001")).thenReturn(Optional.empty());
        when(pagoServicioRepository.existsByNumeroCuentaAndIdServicioAndIdProductoAndTelefonoReferenciaAndPrecioAndEstatusTransaccionInAndFechaCreacionAfter(
                anyString(), anyInt(), anyInt(), anyString(), any(), anyCollection(), any()
        )).thenReturn(false);

        when(pagoServicioRepository.save(any(PagoServicioEntity.class))).thenAnswer(inv -> {
            PagoServicioEntity entity = inv.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(500);
            }
            return entity;
        });
        when(pagoServicioRepository.findById(500)).thenAnswer(inv -> Optional.of(
                PagoServicioEntity.builder()
                        .id(500)
                        .numeroCuenta("0019547596")
                        .clienteId(1)
                        .idServicio(101)
                        .idProducto(202)
                        .precio(new BigDecimal("350.00"))
                        .telefonoReferencia("5512345678")
                        .distribuidor(1001)
                        .estatusTransaccion("EN_PROCESO")
                        .codigoRespuesta(102)
                        .folioAutorizacion("TX-INTERNO")
                        .saldoAnterior(new BigDecimal("1500.00"))
                        .saldoPosterior(new BigDecimal("1150.00"))
                        .build()
        ));

        GestoPagoTxResponse providerResponse = GestoPagoTxResponse.builder()
                .codigo(0)
                .status(200)
                .mensaje("Transacción exitosa")
                .folio("GP-998877")
                .autorizacion("AUT-112233")
                .fecha(LocalDateTime.now())
                .build();

        when(transactionClient.sendTx(eq("Bearer secure_bearer_token_2026"), any(GestoPagoTxRequest.class)))
                .thenReturn(providerResponse);

        PagoServicioResponse response = pagoServicioService.procesarPagoServicio(validRequest);

        assertNotNull(response);
        assertTrue(response.getExitoso());
        assertEquals("EXITOSA", response.getEstatusTransaccion());
        assertEquals(0, response.getCodigo());
        assertEquals("GP-998877", response.getFolioTransaccion());
        assertEquals(new BigDecimal("1500.00"), response.getSaldoAnterior());
        assertEquals(new BigDecimal("1150.00"), response.getSaldoRestante());
        assertEquals(new BigDecimal("1150.00"), cuentaActiva.getSaldo());
        assertNull(response.getTokenUtilizado(), "El token Bearer no debe exponerse en el DTO de respuesta");
    }

    @Test
    @DisplayName("Debe rechazar la operación si el producto no existe en el catálogo")
    void testProcesarPago_ProductoNoExiste() {
        when(gestoPagoProductoRepository.findByIdProducto(202)).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoException.class,
                () -> pagoServicioService.procesarPagoServicio(validRequest));

        verify(transactionClient, never()).sendTx(anyString(), any());
    }

    @Test
    @DisplayName("Debe rechazar la operación si el monto solicitado es menor al mínimo permitido del producto")
    void testProcesarPago_MontoMenorAlMinimo() {
        validRequest.setPrecio(new BigDecimal("5.00"));
        when(gestoPagoProductoRepository.findByIdProducto(202)).thenReturn(Optional.of(productoCatalogo));

        MontoInvalidoException ex = assertThrows(MontoInvalidoException.class,
                () -> pagoServicioService.procesarPagoServicio(validRequest));

        assertTrue(ex.getMessage().contains("inferior al monto mínimo"));
        verify(transactionClient, never()).sendTx(anyString(), any());
    }

    @Test
    @DisplayName("Debe rechazar la operación si el monto solicitado excede el máximo permitido del producto")
    void testProcesarPago_MontoMayorAlMaximo() {
        validRequest.setPrecio(new BigDecimal("9999.00"));
        when(gestoPagoProductoRepository.findByIdProducto(202)).thenReturn(Optional.of(productoCatalogo));

        MontoInvalidoException ex = assertThrows(MontoInvalidoException.class,
                () -> pagoServicioService.procesarPagoServicio(validRequest));

        assertTrue(ex.getMessage().contains("excede el monto máximo"));
        verify(transactionClient, never()).sendTx(anyString(), any());
    }

    @Test
    @DisplayName("Debe rechazar la operación si el idServicio no coincide con el producto en el catálogo")
    void testProcesarPago_IdServicioNoCoincideConProducto() {
        validRequest.setIdServicio(999);
        when(gestoPagoProductoRepository.findByIdProducto(202)).thenReturn(Optional.of(productoCatalogo));

        MontoInvalidoException ex = assertThrows(MontoInvalidoException.class,
                () -> pagoServicioService.procesarPagoServicio(validRequest));

        assertTrue(ex.getMessage().contains("idServicio"));
        verify(transactionClient, never()).sendTx(anyString(), any());
    }

    @Test
    @DisplayName("Debe rechazar la operación si el cliente autenticado intenta operar una cuenta ajena")
    void testProcesarPago_ClienteSinAutorizacionSobreCuenta() {
        String tokenDeOtroCliente = securityConfig.generarToken("otro@banco.com", 999, 888);

        when(gestoPagoProductoRepository.findByIdProducto(202)).thenReturn(Optional.of(productoCatalogo));
        when(gestoPagoTokenService.obtenerTokenActivo(1001, "DEV_TEST_001")).thenReturn(Optional.of(tokenActivo));
        when(cuentaRepository.findByNumeroCuentaWithLock("0019547596")).thenReturn(Optional.of(cuentaActiva));

        assertThrows(OperacionNoAutorizadaException.class,
                () -> pagoServicioService.procesarPagoServicio(validRequest, "Bearer " + tokenDeOtroCliente, "IDEMP-0002"));

        verify(transactionClient, never()).sendTx(anyString(), any());
    }

    @Test
    @DisplayName("Debe rechazar la operación cuando existe una solicitud duplicada por idempotencyKey")
    void testProcesarPago_SolicitudDuplicadaPorIdempotencyKey() {
        when(gestoPagoProductoRepository.findByIdProducto(202)).thenReturn(Optional.of(productoCatalogo));
        when(gestoPagoTokenService.obtenerTokenActivo(1001, "DEV_TEST_001")).thenReturn(Optional.of(tokenActivo));
        when(cuentaRepository.findByNumeroCuentaWithLock("0019547596")).thenReturn(Optional.of(cuentaActiva));

        PagoServicioEntity existente = PagoServicioEntity.builder()
                .id(400)
                .idempotencyKey("IDEMP-0001")
                .folioAutorizacion("GP-PREVIO")
                .estatusTransaccion("EXITOSA")
                .build();
        when(pagoServicioRepository.findByIdempotencyKey("IDEMP-0001")).thenReturn(Optional.of(existente));

        assertThrows(TransaccionDuplicadaException.class,
                () -> pagoServicioService.procesarPagoServicio(validRequest));

        verify(transactionClient, never()).sendTx(anyString(), any());
        assertEquals(new BigDecimal("1500.00"), cuentaActiva.getSaldo(), "El saldo no debe descontarse en una petición duplicada");
    }

    @Test
    @DisplayName("Idempotencia: si la misma solicitud con idempKey se reenvía idéntica, retorna la respuesta previa sin duplicar cargo")
    void testProcesarPago_IdempotenciaExitosaReenvioRespuestaPrevia() {
        when(gestoPagoProductoRepository.findByIdProducto(202)).thenReturn(Optional.of(productoCatalogo));

        PagoServicioEntity existente = PagoServicioEntity.builder()
                .id(400)
                .idempotencyKey("IDEMP-0001")
                .numeroCuenta("0019547596")
                .idProducto(202)
                .precio(new BigDecimal("350.00"))
                .folioAutorizacion("GP-PREVIO-OK")
                .estatusTransaccion("EXITOSA")
                .saldoAnterior(new BigDecimal("1500.00"))
                .saldoPosterior(new BigDecimal("1150.00"))
                .build();
        when(pagoServicioRepository.findByIdempotencyKey("IDEMP-0001")).thenReturn(Optional.of(existente));

        PagoServicioResponse response = pagoServicioService.procesarPagoServicio(validRequest);

        assertNotNull(response);
        assertTrue(response.getExitoso());
        assertEquals("EXITOSA", response.getEstatusTransaccion());
        assertEquals("GP-PREVIO-OK", response.getFolioTransaccion());
        verify(cuentaRepository, never()).findByNumeroCuentaWithLock(anyString());
        verify(transactionClient, never()).sendTx(anyString(), any());
    }

    @Test
    @DisplayName("Ante rechazo explícito de GestoPago (código != 0), debe revertir automáticamente el saldo a la cuenta")
    void testProcesarPago_RechazoProveedor_EjecutaReversoDeSaldo() {
        when(gestoPagoProductoRepository.findByIdProducto(202)).thenReturn(Optional.of(productoCatalogo));
        when(gestoPagoTokenService.obtenerTokenActivo(1001, "DEV_TEST_001")).thenReturn(Optional.of(tokenActivo));
        when(cuentaRepository.findByNumeroCuentaWithLock("0019547596")).thenReturn(Optional.of(cuentaActiva));
        when(pagoServicioRepository.findByIdempotencyKey("IDEMP-0001")).thenReturn(Optional.empty());
        when(pagoServicioRepository.existsByNumeroCuentaAndIdServicioAndIdProductoAndTelefonoReferenciaAndPrecioAndEstatusTransaccionInAndFechaCreacionAfter(
                anyString(), anyInt(), anyInt(), anyString(), any(), anyCollection(), any()
        )).thenReturn(false);

        PagoServicioEntity registroEnProceso = PagoServicioEntity.builder()
                .id(501)
                .numeroCuenta("0019547596")
                .precio(new BigDecimal("350.00"))
                .estatusTransaccion("EN_PROCESO")
                .saldoAnterior(new BigDecimal("1500.00"))
                .saldoPosterior(new BigDecimal("1150.00"))
                .build();

        when(pagoServicioRepository.save(any(PagoServicioEntity.class))).thenReturn(registroEnProceso);
        when(pagoServicioRepository.findByIdWithLock(501)).thenReturn(Optional.of(registroEnProceso));
        when(pagoServicioRepository.findById(501)).thenReturn(Optional.of(registroEnProceso));

        GestoPagoTxResponse rechazoResponse = GestoPagoTxResponse.builder()
                .codigo(14)
                .mensaje("Referencia inválida en proveedor")
                .build();
        when(transactionClient.sendTx(anyString(), any(GestoPagoTxRequest.class))).thenReturn(rechazoResponse);

        assertThrows(ClienteBusinessException.class,
                () -> pagoServicioService.procesarPagoServicio(validRequest));

        // El saldo reservado (1500 - 350 = 1150) debe haberse reintegrado a 1500.00
        assertEquals(new BigDecimal("1500.00"), cuentaActiva.getSaldo());
        assertEquals("REVERSADA", registroEnProceso.getEstatusTransaccion());
    }

    @Test
    @DisplayName("Ante Timeout del proveedor, NO debe asumir que la transacción falló ni revertir saldo a ciegas: debe marcar PENDIENTE_CONCILIACION")
    void testProcesarPago_TimeoutProveedor_MarcaPendienteConciliacionYRetieneSaldo() {
        when(gestoPagoProductoRepository.findByIdProducto(202)).thenReturn(Optional.of(productoCatalogo));
        when(gestoPagoTokenService.obtenerTokenActivo(1001, "DEV_TEST_001")).thenReturn(Optional.of(tokenActivo));
        when(cuentaRepository.findByNumeroCuentaWithLock("0019547596")).thenReturn(Optional.of(cuentaActiva));
        when(pagoServicioRepository.findByIdempotencyKey("IDEMP-0001")).thenReturn(Optional.empty());
        when(pagoServicioRepository.existsByNumeroCuentaAndIdServicioAndIdProductoAndTelefonoReferenciaAndPrecioAndEstatusTransaccionInAndFechaCreacionAfter(
                anyString(), anyInt(), anyInt(), anyString(), any(), anyCollection(), any()
        )).thenReturn(false);

        PagoServicioEntity registroEnProceso = PagoServicioEntity.builder()
                .id(502)
                .numeroCuenta("0019547596")
                .precio(new BigDecimal("350.00"))
                .estatusTransaccion("EN_PROCESO")
                .folioAutorizacion("TX-TIMEOUT-01")
                .saldoAnterior(new BigDecimal("1500.00"))
                .saldoPosterior(new BigDecimal("1150.00"))
                .build();

        when(pagoServicioRepository.save(any(PagoServicioEntity.class))).thenReturn(registroEnProceso);
        when(pagoServicioRepository.findById(502)).thenReturn(Optional.of(registroEnProceso));

        Request feignReq = Request.create(Request.HttpMethod.POST, "/sistema/service/sendTx.do",
                new HashMap<>(), Request.Body.empty(), new RequestTemplate());
        RetryableException timeoutEx = new RetryableException(
                504, "Read timed out", Request.HttpMethod.POST, new Date(), feignReq);

        when(transactionClient.sendTx(anyString(), any(GestoPagoTxRequest.class))).thenThrow(timeoutEx);

        TransaccionPendienteConciliacionException ex = assertThrows(
                TransaccionPendienteConciliacionException.class,
                () -> pagoServicioService.procesarPagoServicio(validRequest)
        );

        assertEquals("TX-TIMEOUT-01", ex.getFolioInterno());
        assertEquals("PENDIENTE_CONCILIACION", registroEnProceso.getEstatusTransaccion());
        // El saldo permanece retenido (1150.00) hasta que la conciliación determine el resultado real
        assertEquals(new BigDecimal("1150.00"), cuentaActiva.getSaldo());
    }

    @Test
    @DisplayName("Conciliación: si el proveedor confirma que NO procesó la operación en timeout, debe reintegrar el saldo y marcar REVERSADA")
    void testConciliarTransaccion_NoProcesadaPorProveedor_ReintegraSaldo() {
        cuentaActiva.setSaldo(new BigDecimal("1150.00"));

        PagoServicioEntity txPendiente = PagoServicioEntity.builder()
                .id(502)
                .numeroCuenta("0019547596")
                .precio(new BigDecimal("350.00"))
                .estatusTransaccion("PENDIENTE_CONCILIACION")
                .folioAutorizacion("TX-TIMEOUT-01")
                .saldoAnterior(new BigDecimal("1500.00"))
                .saldoPosterior(new BigDecimal("1150.00"))
                .build();

        when(pagoServicioRepository.findById(502)).thenReturn(Optional.of(txPendiente));
        when(pagoServicioRepository.findByIdWithLock(502)).thenReturn(Optional.of(txPendiente));
        when(cuentaRepository.findByNumeroCuentaWithLock("0019547596")).thenReturn(Optional.of(cuentaActiva));
        when(pagoServicioRepository.save(any(PagoServicioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ConciliacionRequest conciliacion = ConciliacionRequest.builder()
                .confirmadaPorProveedor(false)
                .motivo("Verificado en GestoPago: transacción no ingresó al switch")
                .build();

        PagoServicioResponse response = pagoServicioService.conciliarTransaccion(502, conciliacion);

        assertFalse(response.getExitoso());
        assertEquals("REVERSADA", response.getEstatusTransaccion());
        assertEquals(new BigDecimal("1500.00"), cuentaActiva.getSaldo());
        assertEquals(new BigDecimal("1500.00"), response.getSaldoRestante());
    }
}

