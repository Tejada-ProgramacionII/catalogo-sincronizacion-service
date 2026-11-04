package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.categoria.domain.ports.out.CategoriaRepository;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.repository.JpaCategoriaRepository;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.domain.ports.out.HorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.repository.JpaHorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.repository.JpaProfesionalRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.CambiosVersion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionesPublicadas;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoIncrementalUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.CatalogoPublicadoClient;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository.JpaVersionAplicadaRepository;

@SpringBootTest
class SincronizarCatalogoIncrementalIntegrationTest {

    private static final Instant FECHA = Instant.parse("2026-10-01T12:00:00Z");

    @MockitoBean
    private CatalogoPublicadoClient catalogoPublicadoClient;

    @Autowired
    private SincronizarCatalogoIncrementalUseCase useCase;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProfesionalRepository profesionalRepository;

    @Autowired
    private HorarioSemanalRepository horarioSemanalRepository;

    @Autowired
    private VersionAplicadaRepository versionAplicadaRepository;

    @Autowired
    private JpaHorarioSemanalRepository jpaHorarioSemanalRepository;

    @Autowired
    private JpaProfesionalRepository jpaProfesionalRepository;

    @Autowired
    private JpaCategoriaRepository jpaCategoriaRepository;

    @Autowired
    private JpaVersionAplicadaRepository jpaVersionAplicadaRepository;

    // Copia local en la versión 1: categoría 1, profesional 10 y su horario 100.
    @BeforeEach
    void cargarVersionUno() {
        categoriaRepository.save(categoria(1L, "Clinica"));
        profesionalRepository.save(profesional(10L, 1L, "Perez"));
        horarioSemanalRepository.save(horario(100L, 10L));
        versionAplicadaRepository.save(VersionAplicada.builder().version(1L).aplicadaEn(FECHA).build());
    }

    @AfterEach
    void vaciarTablas() {
        jpaHorarioSemanalRepository.deleteAllInBatch();
        jpaProfesionalRepository.deleteAllInBatch();
        jpaCategoriaRepository.deleteAllInBatch();
        jpaVersionAplicadaRepository.deleteAllInBatch();
    }

    @Test
    void unaVersionInsertaYActualizaYUnaVersionQueFallaSeDeshaceEntera() {
        when(catalogoPublicadoClient.getVersiones())
                .thenReturn(VersionesPublicadas.builder().actual(3L).masAntiguaDisponible(1L).build());
        // v2: actualiza al profesional 10 y agrega la categoría 2, el profesional 11 y su horario 101.
        when(catalogoPublicadoClient.getCambios(2L))
                .thenReturn(Optional.of(cambios(2L, List.of(2L), List.of(10L, 11L), List.of(101L))));
        when(catalogoPublicadoClient.getCategorias(List.of(2L))).thenReturn(List.of(categoria(2L, "Pediatria")));
        when(catalogoPublicadoClient.getProfesionales(List.of(10L, 11L)))
                .thenReturn(List.of(profesional(10L, 1L, "Perez Garcia"), profesional(11L, 2L, "Gomez")));
        when(catalogoPublicadoClient.getHorarios(List.of(101L))).thenReturn(List.of(horario(101L, 11L)));
        // v3: renombra la categoría 1, pero su horario apunta a un profesional que no existe.
        when(catalogoPublicadoClient.getCambios(3L))
                .thenReturn(Optional.of(cambios(3L, List.of(1L), List.of(), List.of(102L))));
        when(catalogoPublicadoClient.getCategorias(List.of(1L))).thenReturn(List.of(categoria(1L, "Clinica medica")));
        when(catalogoPublicadoClient.getProfesionales(List.of())).thenReturn(List.of());
        when(catalogoPublicadoClient.getHorarios(List.of(102L))).thenReturn(List.of(horario(102L, 99L)));

        assertThatThrownBy(useCase::sincronizar).isInstanceOf(DataIntegrityViolationException.class);

        // La v2 quedó entera; de la v3 no quedó nada (la categoría 1 conserva su nombre).
        assertThat(versionAplicadaRepository.find()).hasValueSatisfying(
                version -> assertThat(version.getVersion()).isEqualTo(2L));
        assertThat(categoriaRepository.findAll()).extracting(Categoria::getId, Categoria::getNombre)
                .containsExactlyInAnyOrder(tuple(1L, "Clinica"), tuple(2L, "Pediatria"));
        assertThat(profesionalRepository.findAll()).extracting(Profesional::getId, Profesional::getApellido)
                .containsExactlyInAnyOrder(tuple(10L, "Perez Garcia"), tuple(11L, "Gomez"));
        assertThat(horarioSemanalRepository.findAll()).extracting(HorarioSemanal::getId)
                .containsExactlyInAnyOrder(100L, 101L);
    }

    private static CambiosVersion cambios(Long version, List<Long> categorias, List<Long> profesionales,
            List<Long> horarios) {
        return CambiosVersion.builder().version(version).categoriaIds(categorias)
                .profesionalIds(profesionales).horarioIds(horarios).build();
    }

    private static Categoria categoria(Long id, String nombre) {
        return Categoria.builder().id(id).nombre(nombre).habilitada(true)
                .creadaEn(FECHA).actualizadaEn(FECHA).build();
    }

    private static Profesional profesional(Long id, Long categoriaId, String apellido) {
        return Profesional.builder().id(id).categoriaId(categoriaId).nombre("Ana").apellido(apellido)
                .habilitado(true).creadoEn(FECHA).actualizadoEn(FECHA).build();
    }

    private static HorarioSemanal horario(Long id, Long profesionalId) {
        return HorarioSemanal.builder().id(id).profesionalId(profesionalId).diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(12, 0)).duracionTurnoMinutos(30)
                .habilitado(true).creadoEn(FECHA).actualizadoEn(FECHA).build();
    }
}
