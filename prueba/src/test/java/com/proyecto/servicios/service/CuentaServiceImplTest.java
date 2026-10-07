package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.ClienteEntity;
import com.proyecto.servicios.entity.sf.CuentaEntity;
import com.proyecto.servicios.exception.ClienteBusinessException;
import com.proyecto.servicios.exception.ClienteNotFoundException;
import com.proyecto.servicios.exception.CuentaNotFoundException;
import com.proyecto.servicios.model.cuenta.CuentaActualizaRequest;
import com.proyecto.servicios.model.cuenta.CuentaCreacionRequest;
import com.proyecto.servicios.model.cuenta.CuentaResponse;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.service.Impl.CuentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private CuentaServiceImpl cuentaService;

    private ClienteEntity clienteActivo;
    private ClienteEntity clienteInactivo;
    private CuentaEntity cuentaActiva;

    @BeforeEach
    void setUp() {
        clienteActivo = ClienteEntity.builder()
                .id(1)
                .nombre("Mariana")
                .activo(true)
                .build();

        clienteInactivo = ClienteEntity.builder()
                .id(2)
                .nombre("Carlos")
                .activo(false)
                .build();

        cuentaActiva = CuentaEntity.builder()
                .id(10)
                .cliente(clienteActivo)
                .numeroCuenta("1234567890")
                .clabe("012180123456789001")
                .saldo(new BigDecimal("1500.00"))
                .estatus("ACTIVA")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Debe crear una cuenta exitosamente para un cliente activo")
    void testCrearCuenta_Exito() {
        CuentaCreacionRequest request = CuentaCreacionRequest.builder()
                .clienteId(1)
                .saldoInicial(new BigDecimal("2000.00"))
                .build();

        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteActivo));
        when(cuentaRepository.existsByNumeroCuenta(anyString())).thenReturn(false);
        when(cuentaRepository.save(any(CuentaEntity.class))).thenReturn(cuentaActiva);

        CuentaResponse response = cuentaService.crearCuenta(request);

        assertNotNull(response);
        assertEquals("1234567890", response.getNumeroCuenta());
        verify(cuentaRepository, times(1)).save(any(CuentaEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar ClienteBusinessException al intentar crear cuenta para cliente inactivo")
    void testCrearCuenta_ClienteInactivo() {
        CuentaCreacionRequest request = CuentaCreacionRequest.builder()
                .clienteId(2)
                .saldoInicial(new BigDecimal("500.00"))
                .build();

        when(clienteRepository.findById(2)).thenReturn(Optional.of(clienteInactivo));

        assertThrows(ClienteBusinessException.class, () -> cuentaService.crearCuenta(request));
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar ClienteNotFoundException si el cliente no existe al crear cuenta")
    void testCrearCuenta_ClienteNoExiste() {
        CuentaCreacionRequest request = CuentaCreacionRequest.builder()
                .clienteId(999)
                .saldoInicial(new BigDecimal("500.00"))
                .build();

        when(clienteRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> cuentaService.crearCuenta(request));
    }

    @Test
    @DisplayName("Debe consultar cuenta por número de cuenta exitosamente")
    void testConsultarPorNumeroCuenta_Exito() {
        when(cuentaRepository.findByNumeroCuenta("1234567890")).thenReturn(Optional.of(cuentaActiva));

        CuentaResponse response = cuentaService.consultarPorNumeroCuenta("1234567890");

        assertNotNull(response);
        assertEquals("1234567890", response.getNumeroCuenta());
        assertEquals("ACTIVA", response.getEstatus());
    }

    @Test
    @DisplayName("Debe lanzar CuentaNotFoundException si la cuenta no existe")
    void testConsultarPorNumeroCuenta_NotFound() {
        when(cuentaRepository.findByNumeroCuenta("0000000000")).thenReturn(Optional.empty());

        assertThrows(CuentaNotFoundException.class, () -> cuentaService.consultarPorNumeroCuenta("0000000000"));
    }

    @Test
    @DisplayName("Debe consultar cuentas por ID de cliente")
    void testConsultarPorClienteId() {
        when(clienteRepository.existsById(1)).thenReturn(true);
        when(cuentaRepository.findByClienteId(1)).thenReturn(List.of(cuentaActiva));

        List<CuentaResponse> lista = cuentaService.consultarPorClienteId(1);

        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Debe consultar cuentas por estatus")
    void testConsultarPorEstatus() {
        when(cuentaRepository.findByEstatus("ACTIVA")).thenReturn(List.of(cuentaActiva));

        List<CuentaResponse> lista = cuentaService.consultarPorEstatus("ACTIVA");

        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Debe consultar saldo de una cuenta")
    void testConsultarSaldo() {
        when(cuentaRepository.findByNumeroCuenta("1234567890")).thenReturn(Optional.of(cuentaActiva));

        BigDecimal saldo = cuentaService.consultarSaldo("1234567890");

        assertNotNull(saldo);
        assertEquals(new BigDecimal("1500.00"), saldo);
    }

    @Test
    @DisplayName("Debe actualizar parcialmente el estatus y saldo de una cuenta")
    void testActualizarParcial() {
        when(cuentaRepository.findByNumeroCuenta("1234567890")).thenReturn(Optional.of(cuentaActiva));
        when(cuentaRepository.save(any(CuentaEntity.class))).thenReturn(cuentaActiva);

        CuentaActualizaRequest request = CuentaActualizaRequest.builder()
                .estatus("BLOQUEADA")
                .saldo(new BigDecimal("2500.00"))
                .build();

        CuentaResponse response = cuentaService.actualizarParcial("1234567890", request);

        assertNotNull(response);
        verify(cuentaRepository, times(1)).save(cuentaActiva);
    }
}
