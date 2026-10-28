package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
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
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.SnapshotCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoCompletoUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.CatalogoCatedraClient;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository.JpaVersionAplicadaRepository;

@SpringBootTest
class SincronizarCatalogoCompletoIntegrationTest {

    private static final Instant FECHA = Instant.parse("2026-10-01T12:00:00Z");

    @MockitoBean
    private CatalogoCatedraClient catalogoCatedraClient;

    @Autowired
    private SincronizarCatalogoCompletoUseCase useCase;

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

    @AfterEach
    void vaciarTablas() {
        jpaHorarioSemanalRepository.deleteAllInBatch();
        jpaProfesionalRepository.deleteAllInBatch();
        jpaCategoriaRepository.deleteAllInBatch();
        jpaVersionAplicadaRepository.deleteAllInBatch();
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

    private static SnapshotCatalogo snapshot(Long version, List<Categoria> categorias,
            List<Profesional> profesionales, List<HorarioSemanal> horarios) {
        return SnapshotCatalogo.builder().version(version).generadoEn(FECHA)
                .categorias(categorias).profesionales(profesionales).horarios(horarios).build();
    }

    @Test
    void elSegundoSnapshotReemplazaAlPrimeroAunqueRepitaIds() {
        when(catalogoCatedraClient.getSnapshot()).thenReturn(
                snapshot(1L,
                        List.of(categoria(1L, "Clinica"), categoria(2L, "Pediatria")),
                        List.of(profesional(10L, 1L, "Perez"), profesional(11L, 2L, "Gomez")),
                        List.of(horario(100L, 10L), horario(101L, 11L))),
                snapshot(2L,
                        List.of(categoria(1L, "Clinica medica")),
                        List.of(profesional(10L, 1L, "Perez")),
                        List.of(horario(100L, 10L))));

        useCase.sincronizar();
        useCase.sincronizar();

        assertThat(categoriaRepository.findAll()).extracting(Categoria::getId, Categoria::getNombre)
                .containsExactly(tuple(1L, "Clinica medica"));
        assertThat(profesionalRepository.findAll()).extracting(Profesional::getId).containsExactly(10L);
        assertThat(horarioSemanalRepository.findAll()).extracting(HorarioSemanal::getId).containsExactly(100L);
        assertThat(versionAplicadaRepository.find()).hasValueSatisfying(
                version -> assertThat(version.getVersion()).isEqualTo(2L));
    }

    @Test
    void unSnapshotInvalidoDejaIntactaLaCopiaAnteriorYSuVersion() {
        when(catalogoCatedraClient.getSnapshot()).thenReturn(
                snapshot(1L,
                        List.of(categoria(1L, "Clinica")),
                        List.of(profesional(10L, 1L, "Perez")),
                        List.of(horario(100L, 10L))),
                snapshot(2L,
                        List.of(categoria(1L, "Clinica medica")),
                        List.of(profesional(10L, 1L, "Perez")),
                        List.of(horario(100L, 99L))));
        useCase.sincronizar();

        assertThatThrownBy(useCase::sincronizar).isInstanceOf(DataIntegrityViolationException.class);

        assertThat(categoriaRepository.findAll()).extracting(Categoria::getNombre).containsExactly("Clinica");
        assertThat(profesionalRepository.findAll()).extracting(Profesional::getId).containsExactly(10L);
        assertThat(horarioSemanalRepository.findAll()).extracting(HorarioSemanal::getProfesionalId)
                .containsExactly(10L);
        assertThat(versionAplicadaRepository.find()).hasValueSatisfying(
                version -> assertThat(version.getVersion()).isEqualTo(1L));
    }
}
