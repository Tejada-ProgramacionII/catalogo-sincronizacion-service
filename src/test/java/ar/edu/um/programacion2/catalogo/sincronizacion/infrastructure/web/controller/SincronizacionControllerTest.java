package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ar.edu.um.programacion2.catalogo.sincronizacion.application.service.SincronizacionService;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.EstadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResumenSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.mapper.SincronizacionDtoMapper;

@ExtendWith(MockitoExtension.class)
class SincronizacionControllerTest {

    private static final String URL = "/api/sincronizacion/estado";

    @Mock
    private SincronizacionService sincronizacionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SincronizacionController(sincronizacionService, new SincronizacionDtoMapper()))
                .build();
    }

    @Test
    void alDiaDevuelveLaVersionYElUltimoIntento() throws Exception {
        Instant inicio = Instant.parse("2026-10-27T10:00:00Z");
        when(sincronizacionService.consultarEstado()).thenReturn(ResumenSincronizacion.builder()
                .estado(EstadoSincronizacion.AL_DIA)
                .versionAplicada(VersionAplicada.builder().version(7L).aplicadaEn(inicio.plusSeconds(4)).build())
                .ultimoIntento(IntentoSincronizacion.builder().id(3L).inicio(inicio).fin(inicio.plusSeconds(5))
                        .resultado(ResultadoSincronizacion.OK).intentos(2).versionAplicada(7L).build())
                .build());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("AL_DIA"))
                .andExpect(jsonPath("$.versionAplicada").value(7))
                .andExpect(jsonPath("$.aplicadaEn").value("2026-10-27T10:00:04Z"))
                .andExpect(jsonPath("$.ultimoIntento.inicio").value("2026-10-27T10:00:00Z"))
                .andExpect(jsonPath("$.ultimoIntento.fin").value("2026-10-27T10:00:05Z"))
                .andExpect(jsonPath("$.ultimoIntento.resultado").value("OK"))
                .andExpect(jsonPath("$.ultimoIntento.intentos").value(2))
                .andExpect(jsonPath("$.ultimoIntento.id").doesNotExist());
    }

    @Test
    void conErrorDevuelveElMotivo() throws Exception {
        Instant inicio = Instant.parse("2026-10-27T11:00:00Z");
        when(sincronizacionService.consultarEstado()).thenReturn(ResumenSincronizacion.builder()
                .estado(EstadoSincronizacion.CON_ERROR)
                .ultimoIntento(IntentoSincronizacion.builder().inicio(inicio).fin(inicio.plusSeconds(10))
                        .resultado(ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE).intentos(3)
                        .mensaje("La cátedra no respondió después de 3 intentos").build())
                .build());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CON_ERROR"))
                .andExpect(jsonPath("$.ultimoIntento.resultado").value("CATEDRA_NO_DISPONIBLE"))
                .andExpect(jsonPath("$.ultimoIntento.mensaje").value("La cátedra no respondió después de 3 intentos"));
    }

    @Test
    void sinDatosDevuelve200ConLosCamposEnNull() throws Exception {
        when(sincronizacionService.consultarEstado())
                .thenReturn(ResumenSincronizacion.builder().estado(EstadoSincronizacion.SIN_DATOS).build());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SIN_DATOS"))
                .andExpect(jsonPath("$.versionAplicada").isEmpty())
                .andExpect(jsonPath("$.ultimoIntento").isEmpty());
    }
}
