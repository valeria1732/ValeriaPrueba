package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.ClienteEntity;
import com.proyecto.servicios.entity.sf.CuentaEntity;
import com.proyecto.servicios.entity.sf.DomicilioEntity;
import com.proyecto.servicios.entity.sf.UsuarioEntity;
import com.proyecto.servicios.exception.ClienteBusinessException;
import com.proyecto.servicios.exception.ClienteNotFoundException;
import com.proyecto.servicios.exception.CorreoDuplicadoException;
import com.proyecto.servicios.exception.CurpDuplicadaException;
import com.proyecto.servicios.exception.RfcDuplicadoException;
import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClientePatchRequest;
import com.proyecto.servicios.model.cliente.ClienteRegistroRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.model.cliente.DomicilioDTO;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.repositorys.sf.DomicilioRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.service.Impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private DomicilioRepository domicilioRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteRegistroRequest registroRequest;
    private ClienteEntity clienteEntity;
    private DomicilioEntity domicilioEntity;
    private CuentaEntity cuentaEntity;
    private UsuarioEntity usuarioEntity;

    @BeforeEach
    void setUp() {
        DomicilioDTO domDTO = DomicilioDTO.builder()
                .calle("Av. Insurgentes Sur")
                .numeroExterior("1200")
                .colonia("Del Valle")
                .municipio("Benito Juarez")
                .estado("CDMX")
                .codigoPostal("03100")
                .pais("Mexico")
                .build();

        registroRequest = ClienteRegistroRequest.builder()
                .nombre("Mariana")
                .segundoNombre("Sofia")
                .apellidoPaterno("Hernandez")
                .apellidoMaterno("Torres")
                .fechaNacimiento(LocalDate.of(1994, 8, 22))
                .curp("HETM940822MDFRRN03")
                .rfc("HETM9408228K4")
                .sexo("FEMENINO")
                .nacionalidad("Mexicana")
                .estadoCivil("SOLTERO")
                .correo("mariana.hernandez@example.com")
                .telefonoMovil("5512345678")
                .domicilio(domDTO)
                .ocupacion("Desarrollador de Software")
                .empresa("Fintech S.A.")
                .ingresoMensual(new BigDecimal("35000.00"))
                .password("Segura123!")
                .saldoInicial(new BigDecimal("1000.00"))
                .build();

        domicilioEntity = DomicilioEntity.builder()
                .id(5)
                .calle("Av. Insurgentes Sur")
                .numeroExterior("1200")
                .colonia("Del Valle")
                .municipio("Benito Juarez")
                .estado("CDMX")
                .codigoPostal("03100")
                .pais("Mexico")
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
                .sexo("FEMENINO")
                .nacionalidad("Mexicana")
                .estadoCivil("SOLTERO")
                .correo("mariana.hernandez@example.com")
                .telefonoMovil("5512345678")
                .domicilio(domicilioEntity)
                .ocupacion("Desarrollador de Software")
                .empresa("Fintech S.A.")
                .ingresoMensual(new BigDecimal("35000.00"))
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .cuentas(new ArrayList<>())
                .build();

        cuentaEntity = CuentaEntity.builder()
                .id(10)
                .cliente(clienteEntity)
                .numeroCuenta("1234567890")
                .clabe("012180123456789001")
                .saldo(new BigDecimal("1000.00"))
                .estatus("ACTIVA")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        usuarioEntity = UsuarioEntity.builder()
                .id(20)
                .cliente(clienteEntity)
                .correo("mariana.hernandez@example.com")
                .password("$2a$10$hashedPassword")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        clienteEntity.getCuentas().add(cuentaEntity);
        clienteEntity.setUsuario(usuarioEntity);
    }

    @Test
    @DisplayName("Debe registrar cliente exitosamente y crear cuenta bancaria y usuario asociados")
    void testRegistrarCliente_Exito() {
        when(clienteRepository.existsByCurp(anyString())).thenReturn(false);
        when(clienteRepository.existsByRfc(anyString())).thenReturn(false);
        when(clienteRepository.existsByCorreo(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(domicilioRepository.save(any(DomicilioEntity.class))).thenReturn(domicilioEntity);
        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteEntity);
        when(cuentaRepository.existsByNumeroCuenta(anyString())).thenReturn(false);
        when(cuentaRepository.save(any(CuentaEntity.class))).thenReturn(cuentaEntity);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(usuarioEntity);

        ClienteResponse response = clienteService.registrarCliente(registroRequest);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Mariana", response.getNombre());
        assertEquals("HETM940822MDFRRN03", response.getCurp());
        assertNotNull(response.getCuentas());
        assertFalse(response.getCuentas().isEmpty());
        assertEquals("1234567890", response.getCuentas().get(0).getNumeroCuenta());
        assertNotNull(response.getUsuario());
        assertEquals("mariana.hernandez@example.com", response.getUsuario().getCorreo());
        verify(clienteRepository, times(1)).save(any(ClienteEntity.class));
        verify(cuentaRepository, times(1)).save(any(CuentaEntity.class));
        verify(usuarioRepository, times(1)).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar CurpDuplicadaException si la CURP ya existe")
    void testRegistrarCliente_CurpDuplicada() {
        when(clienteRepository.existsByCurp("HETM940822MDFRRN03")).thenReturn(true);

        assertThrows(CurpDuplicadaException.class,
                () -> clienteService.registrarCliente(registroRequest));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar RfcDuplicadoException si el RFC ya existe")
    void testRegistrarCliente_RfcDuplicado() {
        when(clienteRepository.existsByCurp("HETM940822MDFRRN03")).thenReturn(false);
        when(clienteRepository.existsByRfc("HETM9408228K4")).thenReturn(true);

        assertThrows(RfcDuplicadoException.class,
                () -> clienteService.registrarCliente(registroRequest));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar CorreoDuplicadoException si el correo ya existe")
    void testRegistrarCliente_CorreoDuplicado() {
        when(clienteRepository.existsByCurp(anyString())).thenReturn(false);
        when(clienteRepository.existsByRfc(anyString())).thenReturn(false);
        when(clienteRepository.existsByCorreo(anyString())).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class,
                () -> clienteService.registrarCliente(registroRequest));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar ClienteBusinessException si el cliente es menor de edad")
    void testRegistrarCliente_MenorDeEdad() {
        registroRequest.setFechaNacimiento(LocalDate.now().minusYears(17));
        when(clienteRepository.existsByCurp(anyString())).thenReturn(false);
        when(clienteRepository.existsByRfc(anyString())).thenReturn(false);
        when(clienteRepository.existsByCorreo(anyString())).thenReturn(false);

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
    @DisplayName("Debe consultar cliente por CURP")
    void testConsultarPorCurp() {
        when(clienteRepository.findByCurp("HETM940822MDFRRN03")).thenReturn(Optional.of(clienteEntity));

        ClienteResponse response = clienteService.consultarPorCurp("HETM940822MDFRRN03");
        assertNotNull(response);
        assertEquals("HETM940822MDFRRN03", response.getCurp());
    }

    @Test
    @DisplayName("Debe consultar cliente por RFC")
    void testConsultarPorRfc() {
        when(clienteRepository.findByRfc("HETM9408228K4")).thenReturn(Optional.of(clienteEntity));

        ClienteResponse response = clienteService.consultarPorRfc("HETM9408228K4");
        assertNotNull(response);
        assertEquals("HETM9408228K4", response.getRfc());
    }

    @Test
    @DisplayName("Debe consultar cliente por Correo")
    void testConsultarPorCorreo() {
        when(clienteRepository.findByCorreo("mariana.hernandez@example.com")).thenReturn(Optional.of(clienteEntity));

        ClienteResponse response = clienteService.consultarPorCorreo("mariana.hernandez@example.com");
        assertNotNull(response);
        assertEquals("mariana.hernandez@example.com", response.getCorreo());
    }

    @Test
    @DisplayName("Debe consultar cliente por Número de Cuenta")
    void testConsultarPorNumeroCuenta() {
        when(clienteRepository.findByNumeroCuenta("1234567890")).thenReturn(Optional.of(clienteEntity));

        ClienteResponse response = clienteService.consultarPorNumeroCuenta("1234567890");
        assertNotNull(response);
        assertEquals(1, response.getId());
    }

    @Test
    @DisplayName("Debe actualizar información del cliente con PUT")
    void testActualizarCliente_Put() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEntity));
        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteEntity);

        ClienteActualizaRequest putRequest = ClienteActualizaRequest.builder()
                .nombre("Mariana")
                .apellidoPaterno("Hernandez")
                .apellidoMaterno("Torres")
                .fechaNacimiento(LocalDate.of(1994, 8, 22))
                .sexo("FEMENINO")
                .nacionalidad("Mexicana")
                .estadoCivil("CASADA")
                .telefonoMovil("5512345678")
                .ocupacion("Arquitecta de Software")
                .empresa("Fintech Innovations")
                .ingresoMensual(new BigDecimal("60000.00"))
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
                .telefonoMovil("5599887766")
                .build();

        ClienteResponse response = clienteService.actualizarParcial(1, patchRequest);
        assertNotNull(response);
        verify(clienteRepository, times(1)).save(clienteEntity);
    }

    @Test
    @DisplayName("Debe realizar baja lógica desactivando cliente, cuenta y usuario")
    void testBajaLogica_Delete() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEntity));

        clienteService.bajaLogica(1);

        assertFalse(clienteEntity.getActivo());
        assertEquals("INACTIVA", cuentaEntity.getEstatus());
        assertFalse(usuarioEntity.getActivo());
        verify(clienteRepository, times(1)).save(clienteEntity);
        verify(cuentaRepository, times(1)).save(cuentaEntity);
        verify(usuarioRepository, times(1)).save(usuarioEntity);
    }

    @Test
    @DisplayName("Debe consultar clientes con filtros")
    void testConsultarClientes() {
        when(clienteRepository.findAll(any(Specification.class))).thenReturn(List.of(clienteEntity));

        List<ClienteResponse> lista = clienteService.consultarClientes(
                "Mariana", null, null, null, null, null, true, null, null);
        assertNotNull(lista);
        assertEquals(1, lista.size());
    }
}
