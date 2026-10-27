package ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.profesional.application.exception.FiltroBusquedaInvalidoException;
import ar.edu.um.programacion2.catalogo.profesional.application.exception.ProfesionalNoEncontradoException;
import ar.edu.um.programacion2.catalogo.profesional.application.service.ProfesionalService;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.AgendaProfesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.mapper.ProfesionalDtoMapper;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatalogoNoDisponibleException;

@ExtendWith(MockitoExtension.class)
class ProfesionalControllerTest {

    private static final String URL = "/api/profesionales";

    @Mock
    private ProfesionalService profesionalService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProfesionalController(profesionalService, new ProfesionalDtoMapper()))
                .build();
    }

    @Test
    void buscaConLosFiltrosYDevuelveLaPagina() throws Exception {
        Pagina<Profesional> pagina = Pagina.<Profesional>builder()
                .contenido(List.of(Profesional.builder()
                        .id(15L).categoriaId(1L).nombre("Ana").apellido("Gomez").habilitado(true).build()))
                .numero(0)
                .tamanio(20)
                .totalElementos(1)
                .totalPaginas(1)
                .build();
        when(profesionalService.buscarProfesionales(any(), eq(0), eq(20))).thenReturn(pagina);

        mockMvc.perform(get(URL)
                        .param("categoriaId", "1")
                        .param("nombre", "ana")
                        .param("habilitado", "true")
                        .param("fecha", "2026-09-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(15))
                .andExpect(jsonPath("$.contenido[0].apellido").value("Gomez"))
                .andExpect(jsonPath("$.totalElementos").value(1));

        ArgumentCaptor<FiltroBusquedaProfesionales> filtro = ArgumentCaptor.forClass(FiltroBusquedaProfesionales.class);
        verify(profesionalService).buscarProfesionales(filtro.capture(), eq(0), eq(20));
        assertThat(filtro.getValue().getCategoriaId()).isEqualTo(1L);
        assertThat(filtro.getValue().getNombre()).isEqualTo("ana");
        assertThat(filtro.getValue().getHabilitado()).isTrue();
        assertThat(filtro.getValue().getFecha()).isEqualTo(LocalDate.of(2026, 9, 14));
    }

    @Test
    void usaLaPaginaYElTamanioPedidos() throws Exception {
        when(profesionalService.buscarProfesionales(any(), eq(2), eq(5)))
                .thenReturn(Pagina.<Profesional>builder().contenido(List.of()).numero(2).tamanio(5).build());

        mockMvc.perform(get(URL).param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isEmpty())
                .andExpect(jsonPath("$.numero").value(2));
    }

    @Test
    void unFiltroInvalidoResponde400ConElMotivo() throws Exception {
        when(profesionalService.buscarProfesionales(any(), anyInt(), anyInt()))
                .thenThrow(new FiltroBusquedaInvalidoException("La categoría 99 no existe"));

        mockMvc.perform(get(URL).param("categoriaId", "99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La categoría 99 no existe"));
    }

    @Test
    void sinCatalogoResponde503() throws Exception {
        when(profesionalService.buscarProfesionales(any(), anyInt(), anyInt()))
                .thenThrow(new CatalogoNoDisponibleException());

        mockMvc.perform(get(URL))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void unaFechaConFormatoIncorrectoResponde400SinBuscar() throws Exception {
        mockMvc.perform(get(URL).param("fecha", "14/09/2026"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(profesionalService);
    }

    @Test
    void unTamanioFueraDeRangoResponde400SinBuscar() throws Exception {
        mockMvc.perform(get(URL).param("size", "500"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(profesionalService);
    }

    @Test
    void consultarProfesionalDevuelveSusDatosHorariosYVersion() throws Exception {
        when(profesionalService.consultarProfesional(15L)).thenReturn(AgendaProfesional.builder()
                .profesional(Profesional.builder()
                        .id(15L).categoriaId(1L).nombre("Ana").apellido("Gomez").habilitado(true).build())
                .horarios(List.of(HorarioSemanal.builder()
                        .diaSemana(DayOfWeek.MONDAY).horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(12, 0))
                        .duracionTurnoMinutos(30).build()))
                .versionCatalogo(7L)
                .build());

        mockMvc.perform(get(URL + "/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.apellido").value("Gomez"))
                .andExpect(jsonPath("$.habilitado").value(true))
                .andExpect(jsonPath("$.horarios[0].diaSemana").value("MONDAY"))
                .andExpect(jsonPath("$.horarios[0].duracionTurnoMinutos").value(30))
                .andExpect(jsonPath("$.versionCatalogo").value(7));
    }

    @Test
    void consultarUnProfesionalInexistenteResponde404() throws Exception {
        when(profesionalService.consultarProfesional(99L)).thenThrow(new ProfesionalNoEncontradoException(99L));

        mockMvc.perform(get(URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el profesional 99"));
    }

    @Test
    void consultarSinCatalogoResponde503() throws Exception {
        when(profesionalService.consultarProfesional(15L)).thenThrow(new CatalogoNoDisponibleException());

        mockMvc.perform(get(URL + "/15"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}
