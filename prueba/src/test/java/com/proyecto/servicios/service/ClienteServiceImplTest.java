package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.ClienteEntity;
import com.proyecto.servicios.entity.sf.CuentaBancariaEntity;
import com.proyecto.servicios.exception.ClienteBusinessException;
import com.proyecto.servicios.exception.ClienteNotFoundException;
import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClientePatchRequest;
import com.proyecto.servicios.model.cliente.ClienteRegistroRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaBancariaRepository;
import com.proyecto.servicios.service.Impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaBancariaRepository cuentaBancariaRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteRegistroRequest registroRequest;
    private ClienteEntity clienteEntity;
    private CuentaBancariaEntity cuentaEntity;

    @BeforeEach
    void setUp() {
        registroRequest = ClienteRegistroRequest.builder()
                .nombre("Mariana")
                .segundoNombre("Sofia")
                .apellidoPaterno("Hernandez")
                .apellidoMaterno("Torres")
                .fechaNacimiento(LocalDate.of(1994, 8, 22))
                .curp("HETM940822MDFRRN03")
                .rfc("HETM9408228K4")
                .telefono("5512345678")
                .email("mariana.hernandez@example.com")
                .calle("Av. Insurgentes Sur")
                .numeroExterior("1200")
                .colonia("Del Valle")
                .codigoPostal("03100")
                .ciudad("Ciudad de México")
                .estado("CDMX")
                .puestoLaboral("Desarrollador de Software")
                .ingresoMensual(new BigDecimal("35000.00"))
                .saldoInicial(new BigDecimal("1000.00"))
                .build();

        clienteEntity = ClienteEntity.builder()
                .id(1)
                .nombre("Mariana")
                .segundoNombre("Sofia")
                .apellidoPaterno("Hernandez")
                .apellidoMaterno("Torres")
                .fechaNacimiento(LocalDate.of(1994, 8, 22))
                .curp("HETM940822MDFRRN03")
                .rfc("HETM9408228K4")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        cuentaEntity = CuentaBancariaEntity.builder()
                .id(10)
                .cliente(clienteEntity)
                .numeroCuenta("1234567890")
                .clabe("012180123456789001")
                .saldo(new BigDecimal("1000.00"))
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        clienteEntity.setCuentaBancaria(cuentaEntity);
    }

    @Test
    @DisplayName("Debe registrar cliente exitosamente y crear cuenta bancaria asociada")
    void testRegistrarCliente_Exito() {
        when(clienteRepository.existsByCurp("HETM940822MDFRRN03")).thenReturn(false);
        when(clienteRepository.existsByRfc("HETM9408228K4")).thenReturn(false);
        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteEntity);
        when(cuentaBancariaRepository.existsByNumeroCuenta(anyString())).thenReturn(false);
        when(cuentaBancariaRepository.save(any(CuentaBancariaEntity.class))).thenReturn(cuentaEntity);

        ClienteResponse response = clienteService.registrarCliente(registroRequest);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Mariana", response.getNombre());
        assertEquals("HETM940822MDFRRN03", response.getCurp());
        assertNotNull(response.getCuentaBancaria());
        assertEquals("1234567890", response.getCuentaBancaria().getNumeroCuenta());
        verify(clienteRepository, times(1)).save(any(ClienteEntity.class));
        verify(cuentaBancariaRepository, times(1)).save(any(CuentaBancariaEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar ClienteBusinessException si la CURP ya existe")
    void testRegistrarCliente_CurpDuplicada() {
        when(clienteRepository.existsByCurp("HETM940822MDFRRN03")).thenReturn(true);

        ClienteBusinessException ex = assertThrows(ClienteBusinessException.class,
                () -> clienteService.registrarCliente(registroRequest));
        assertTrue(ex.getMessage().contains("Ya existe un cliente registrado con la CURP"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar ClienteBusinessException si el RFC ya existe")
    void testRegistrarCliente_RfcDuplicado() {
        when(clienteRepository.existsByCurp("HETM940822MDFRRN03")).thenReturn(false);
        when(clienteRepository.existsByRfc("HETM9408228K4")).thenReturn(true);

        ClienteBusinessException ex = assertThrows(ClienteBusinessException.class,
                () -> clienteService.registrarCliente(registroRequest));
        assertTrue(ex.getMessage().contains("Ya existe un cliente registrado con el RFC"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar ClienteBusinessException si el cliente es menor de edad")
    void testRegistrarCliente_MenorDeEdad() {
        registroRequest.setFechaNacimiento(LocalDate.now().minusYears(17));
        when(clienteRepository.existsByCurp(anyString())).thenReturn(false);
        when(clienteRepository.existsByRfc(anyString())).thenReturn(false);

        ClienteBusinessException ex = assertThrows(ClienteBusinessException.class,
                () -> clienteService.registrarCliente(registroRequest));
        assertTrue(ex.getMessage().contains("mayor de edad"));
    }

    @Test
    @DisplayName("Debe consultar cliente por ID exitosamente")
    void testConsultarPorId_Exito() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEntity));

        ClienteResponse response = clienteService.consultarPorId(1);
        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Mariana", response.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar ClienteNotFoundException si el cliente no existe")
    void testConsultarPorId_NoExiste() {
        when(clienteRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.consultarPorId(999));
    }

    @Test
    @DisplayName("Debe actualizar información del cliente con PUT")
    void testActualizarCliente_Put() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEntity));
        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteEntity);

        ClienteActualizaRequest putRequest = ClienteActualizaRequest.builder()
                .nombre("Mariana")
                .apellidoPaterno("Hernandez")
                .puestoLaboral("Arquitecta de Software")
                .build();

        ClienteResponse response = clienteService.actualizarCliente(1, putRequest);
        assertNotNull(response);
        verify(clienteRepository, times(1)).save(clienteEntity);
    }

    @Test
    @DisplayName("Debe actualizar parcialmente información del cliente con PATCH")
    void testActualizarParcial_Patch() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEntity));
        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteEntity);

        ClientePatchRequest patchRequest = ClientePatchRequest.builder()
                .telefono("5599887766")
                .build();

        ClienteResponse response = clienteService.actualizarParcial(1, patchRequest);
        assertNotNull(response);
        verify(clienteRepository, times(1)).save(clienteEntity);
    }

    @Test
    @DisplayName("Debe realizar baja lógica desactivando cliente y cuenta")
    void testBajaLogica_Delete() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEntity));

        clienteService.bajaLogica(1);

        assertFalse(clienteEntity.getActivo());
        assertFalse(cuentaEntity.getActivo());
        verify(clienteRepository, times(1)).save(clienteEntity);
    }

    @Test
    @DisplayName("Debe consultar clientes con filtros")
    void testConsultarClientes() {
        when(clienteRepository.findAll(any(Specification.class))).thenReturn(List.of(clienteEntity));

        List<ClienteResponse> lista = clienteService.consultarClientes("Mariana", null, null, true);
        assertNotNull(lista);
        assertEquals(1, lista.size());
    }
}
