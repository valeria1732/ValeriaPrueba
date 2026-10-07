package com.proyecto.servicios.service;

import com.proyecto.servicios.config.SecurityConfig;
import com.proyecto.servicios.entity.sf.ClienteEntity;
import com.proyecto.servicios.entity.sf.UsuarioEntity;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.usuario.LoginRequest;
import com.proyecto.servicios.model.usuario.LoginResponse;
import com.proyecto.servicios.model.usuario.UsuarioCreacionRequest;
import com.proyecto.servicios.model.usuario.UsuarioResponse;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.service.Impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityConfig securityConfig;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private ClienteEntity clienteEntity;
    private UsuarioEntity usuarioActivo;
    private UsuarioEntity usuarioInactivo;

    @BeforeEach
    void setUp() {
        clienteEntity = ClienteEntity.builder()
                .id(1)
                .nombre("Mariana")
                .activo(true)
                .build();

        usuarioActivo = UsuarioEntity.builder()
                .id(10)
                .cliente(clienteEntity)
                .correo("mariana.hernandez@example.com")
                .password("$2a$10$hashedPassword")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        usuarioInactivo = UsuarioEntity.builder()
                .id(11)
                .cliente(clienteEntity)
                .correo("carlos.inactivo@example.com")
                .password("$2a$10$hashedPassword")
                .activo(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Debe iniciar sesión exitosamente y retornar token JWT")
    void testLogin_Exito() {
        LoginRequest request = LoginRequest.builder()
                .correo("mariana.hernandez@example.com")
                .password("Segura123!")
                .build();

        when(usuarioRepository.findByCorreo("mariana.hernandez@example.com")).thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("Segura123!", "$2a$10$hashedPassword")).thenReturn(true);
        when(securityConfig.generarToken(anyString(), any(), any())).thenReturn("mocked.jwt.token");

        LoginResponse response = usuarioService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("mariana.hernandez@example.com", response.getCorreo());
    }

    @Test
    @DisplayName("Debe lanzar UsuarioNotFoundException si el usuario no existe")
    void testLogin_UsuarioNoExiste() {
        LoginRequest request = LoginRequest.builder()
                .correo("noexiste@example.com")
                .password("Segura123!")
                .build();

        when(usuarioRepository.findByCorreo("noexiste@example.com")).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () -> usuarioService.login(request));
    }

    @Test
    @DisplayName("Debe lanzar UsuarioInactivoException si el usuario está inactivo")
    void testLogin_UsuarioInactivo() {
        LoginRequest request = LoginRequest.builder()
                .correo("carlos.inactivo@example.com")
                .password("Segura123!")
                .build();

        when(usuarioRepository.findByCorreo("carlos.inactivo@example.com")).thenReturn(Optional.of(usuarioInactivo));

        assertThrows(UsuarioInactivoException.class, () -> usuarioService.login(request));
    }

    @Test
    @DisplayName("Debe lanzar CredencialesInvalidasException si la contraseña no coincide")
    void testLogin_PasswordIncorrecto() {
        LoginRequest request = LoginRequest.builder()
                .correo("mariana.hernandez@example.com")
                .password("PasswordIncorrecto1!")
                .build();

        when(usuarioRepository.findByCorreo("mariana.hernandez@example.com")).thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("PasswordIncorrecto1!", "$2a$10$hashedPassword")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> usuarioService.login(request));
    }

    @Test
    @DisplayName("Debe agregar un usuario nuevo exitosamente")
    void testAgregarUsuario_Exito() {
        UsuarioCreacionRequest request = UsuarioCreacionRequest.builder()
                .clienteId(1)
                .correo("mariana.hernandez@example.com")
                .password("Segura123!")
                .build();

        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEntity));
        when(usuarioRepository.findByClienteId(1)).thenReturn(Optional.empty());
        when(usuarioRepository.existsByCorreo("mariana.hernandez@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Segura123!")).thenReturn("$2a$10$hashedPassword");
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(usuarioActivo);

        UsuarioResponse response = usuarioService.agregarUsuario(request);

        assertNotNull(response);
        assertEquals("mariana.hernandez@example.com", response.getCorreo());
        assertTrue(response.getActivo());
    }

    @Test
    @DisplayName("Debe lanzar CorreoDuplicadoException si el correo ya existe")
    void testAgregarUsuario_CorreoDuplicado() {
        UsuarioCreacionRequest request = UsuarioCreacionRequest.builder()
                .clienteId(1)
                .correo("mariana.hernandez@example.com")
                .password("Segura123!")
                .build();

        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteEntity));
        when(usuarioRepository.findByClienteId(1)).thenReturn(Optional.empty());
        when(usuarioRepository.existsByCorreo("mariana.hernandez@example.com")).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class, () -> usuarioService.agregarUsuario(request));
    }

    @Test
    @DisplayName("Debe consultar usuarios con filtro")
    void testConsultarUsuariosConFiltro() {
        when(usuarioRepository.findAll(any(Specification.class))).thenReturn(List.of(usuarioActivo));

        List<UsuarioResponse> list = usuarioService.consultarUsuariosConFiltro("mariana", true);

        assertNotNull(list);
        assertEquals(1, list.size());
    }
}
