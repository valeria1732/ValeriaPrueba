package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.EliminaPersonaRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.PersonaResponse;
import com.proyecto.servicios.model.PersonasRequest;
import com.proyecto.servicios.service.PersonaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PersonaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PersonaService personaService;

    @InjectMocks
    private PersonaController personaController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(personaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /personas debe crear persona y responder 200 OK")
    void testCrearPersona_200_OK() throws Exception {
        PersonasRequest req = PersonasRequest.builder()
                .nombre("Carlos")
                .apellidoP("Ramirez")
                .apellidoMaterno("Gomez")
                .build();
        PersonaResponse res = new PersonaResponse();
        res.setCodigo(1);
        res.setMensaje("Exito");
        res.setNombre("Carlos");

        when(personaService.creaPersona(any(PersonasRequest.class))).thenReturn(res);

        mockMvc.perform(post("/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1))
                .andExpect(jsonPath("$.mensaje").value("Exito"));
    }

    @Test
    @DisplayName("PUT /personasActualiza debe actualizar persona y responder 200 OK")
    void testActualizaPersona_200_OK() throws Exception {
        PersonasRequest req = PersonasRequest.builder()
                .nombre("Carlos")
                .apellidoP("Lopez")
                .apellidoMaterno("Mendez")
                .build();
        when(personaService.actualizaPersona(any(PersonasRequest.class)))
                .thenReturn(new GenericResponse(0, "la persona ha sido actualizada correctamente"));

        mockMvc.perform(put("/personasActualiza")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(0));
    }

    @Test
    @DisplayName("PUT /personasElimina debe eliminar persona y responder 200 OK")
    void testEliminaPersona_200_OK() throws Exception {
        EliminaPersonaRequest req = new EliminaPersonaRequest("Carlos");
        when(personaService.eliminaPersona(any(EliminaPersonaRequest.class)))
                .thenReturn(new GenericResponse(0, "La persona ha sido eliminada correctamente"));

        mockMvc.perform(put("/personasElimina")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(0));
    }
}

