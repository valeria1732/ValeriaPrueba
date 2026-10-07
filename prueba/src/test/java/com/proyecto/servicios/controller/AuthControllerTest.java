package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.exception.CredencialesInvalidasException;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.exception.UsuarioInactivoException;
import com.proyecto.servicios.exception.UsuarioNotFoundException;
import com.proyecto.servicios.model.usuario.LoginRequest;
import com.proyecto.servicios.model.usuario.LoginResponse;
import com.proyecto.servicios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AuthController authController;

    private LoginResponse mockLoginResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockLoginResponse = LoginResponse.builder()
                .token("jwt.sample.token")
                .tipoToken("Bearer")
                .correo("mariana.hernandez@example.com")
                .clienteId(1)
                .expiraEnMs(86400000L)
                .build();
    }

    @Test
    @DisplayName("POST /auth/login debe responder 200 OK con token JWT")
    void testLogin_200_OK() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .correo("mariana.hernandez@example.com")
                .password("Segura123!")
                .build();

        when(usuarioService.login(any(LoginRequest.class))).thenReturn(mockLoginResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.sample.token"))
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.correo").value("mariana.hernandez@example.com"));
    }

    @Test
    @DisplayName("POST /auth/login debe responder 401 Unauthorized si las credenciales son inválidas")
    void testLogin_401_Unauthorized() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .correo("mariana.hernandez@example.com")
                .password("Incorrecto123!")
                .build();

        when(usuarioService.login(any(LoginRequest.class)))
                .thenThrow(new CredencialesInvalidasException("Credenciales incorrectas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(401));
    }

    @Test
    @DisplayName("POST /auth/login debe responder 403 Forbidden si el usuario está inactivo")
    void testLogin_403_Forbidden() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .correo("inactivo@example.com")
                .password("Segura123!")
                .build();

        when(usuarioService.login(any(LoginRequest.class)))
                .thenThrow(new UsuarioInactivoException("El usuario se encuentra inactivo"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(403));
    }

    @Test
    @DisplayName("POST /auth/login debe responder 404 Not Found si el usuario no existe")
    void testLogin_404_NotFound() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .correo("noexiste@example.com")
                .password("Segura123!")
                .build();

        when(usuarioService.login(any(LoginRequest.class)))
                .thenThrow(new UsuarioNotFoundException("No se encontró el usuario"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }
}
