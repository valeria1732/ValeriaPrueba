package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.usuario.UsuarioCreacionRequest;
import com.proyecto.servicios.model.usuario.UsuarioResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private UsuarioResponse mockUsuarioResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockUsuarioResponse = UsuarioResponse.builder()
                .id(1)
                .clienteId(1)
                .correo("mariana.hernandez@example.com")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("GET /usuarios/filtro debe responder 200 OK con respuesta paginada de usuarios filtrados")
    void testConsultarFiltro_200_OK() throws Exception {
        com.proyecto.servicios.model.PageResponse<UsuarioResponse> pageResponse =
                com.proyecto.servicios.model.PageResponse.of(List.of(mockUsuarioResponse), 0, 10, 1);
        when(usuarioService.consultarUsuariosConFiltroPaginados("mariana", true, 0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/usuarios/filtro?correo=mariana&activo=true&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].correo").value("mariana.hernandez@example.com"))
                .andExpect(jsonPath("$.contenido[0].activo").value(true))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanoPagina").value(10))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    @DisplayName("PUT /usuarios/agregar debe responder 200 OK al agregar usuario")
    void testAgregarUsuario_200_OK() throws Exception {
        UsuarioCreacionRequest request = UsuarioCreacionRequest.builder()
                .clienteId(1)
                .correo("mariana.hernandez@example.com")
                .password("Segura123!")
                .build();

        when(usuarioService.agregarUsuario(any(UsuarioCreacionRequest.class))).thenReturn(mockUsuarioResponse);

        mockMvc.perform(put("/usuarios/agregar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.correo").value("mariana.hernandez@example.com"));
    }
}
