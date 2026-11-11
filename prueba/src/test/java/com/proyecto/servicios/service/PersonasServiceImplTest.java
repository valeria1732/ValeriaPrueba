package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.Personas;
import com.proyecto.servicios.model.EliminaPersonaRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.PersonaResponse;
import com.proyecto.servicios.model.PersonasRequest;
import com.proyecto.servicios.repositorys.sf.PersonasRepository;
import com.proyecto.servicios.service.impl.PersonasServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonasServiceImplTest {

    @Mock
    private PersonasRepository personasRepository;

    @InjectMocks
    private PersonasServiceImpl personasService;

    @Test
    @DisplayName("creaPersona debe persistir la persona y retornar codigo 1 con mensaje Exito")
    void testCreaPersona_Exito() {
        PersonasRequest request = PersonasRequest.builder()
                .nombre("Carlos")
                .apellidoP("Ramirez")
                .apellidoMaterno("Gomez")
                .build();

        when(personasRepository.save(any(Personas.class))).thenAnswer(inv -> inv.getArgument(0));

        PersonaResponse response = personasService.creaPersona(request);

        assertNotNull(response);
        assertEquals(1, response.getCodigo());
        assertEquals("Exito", response.getMensaje());
        assertEquals("Carlos", response.getNombre());
        verify(personasRepository).save(any(Personas.class));
    }

    @Test
    @DisplayName("actualizaPersona debe actualizar apellidos cuando la persona existe")
    void testActualizaPersona_Encontrada() {
        Personas existente = new Personas();
        existente.setId(1);
        existente.setNombre("Carlos");

        PersonasRequest request = PersonasRequest.builder()
                .nombre("Carlos")
                .apellidoP("Lopez")
                .apellidoMaterno("Mendez")
                .build();

        when(personasRepository.findByNombre("Carlos")).thenReturn(Optional.of(existente));
        when(personasRepository.save(any(Personas.class))).thenAnswer(inv -> inv.getArgument(0));

        GenericResponse response = personasService.actualizaPersona(request);

        assertEquals(0, response.getCodigo());
        assertEquals("Lopez", existente.getApellidoP());
        assertEquals("Mendez", existente.getApellidoMaterno());
        verify(personasRepository).save(existente);
    }

    @Test
    @DisplayName("actualizaPersona debe regresar código 1 cuando la persona no existe")
    void testActualizaPersona_NoEncontrada() {
        PersonasRequest request = PersonasRequest.builder()
                .nombre("Desconocido")
                .apellidoP("Lopez")
                .apellidoMaterno("Mendez")
                .build();

        when(personasRepository.findByNombre("Desconocido")).thenReturn(Optional.empty());

        GenericResponse response = personasService.actualizaPersona(request);

        assertEquals(1, response.getCodigo());
        verify(personasRepository, never()).save(any());
    }

    @Test
    @DisplayName("eliminaPersona debe eliminar cuando existe y reportar código distinto de 0 cuando no existe")
    void testEliminaPersona_AmbosCasos() {
        Personas existente = new Personas();
        existente.setId(7);
        existente.setNombre("Carlos");

        when(personasRepository.findByNombre("Carlos")).thenReturn(Optional.of(existente));
        GenericResponse ok = personasService.eliminaPersona(new EliminaPersonaRequest("Carlos"));
        assertEquals(0, ok.getCodigo());
        verify(personasRepository).delete(existente);

        when(personasRepository.findByNombre("Nadie")).thenReturn(Optional.empty());
        GenericResponse notFound = personasService.eliminaPersona(new EliminaPersonaRequest("Nadie"));
        assertNotEquals(0, notFound.getCodigo());
    }
}
