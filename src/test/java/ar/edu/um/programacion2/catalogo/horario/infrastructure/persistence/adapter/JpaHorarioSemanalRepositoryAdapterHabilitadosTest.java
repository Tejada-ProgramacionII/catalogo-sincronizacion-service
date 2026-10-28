package ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.entity.CategoriaEntity;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.repository.JpaCategoriaRepository;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.entity.HorarioSemanalEntity;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.repository.JpaHorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.entity.ProfesionalEntity;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.repository.JpaProfesionalRepository;

@SpringBootTest
@Transactional
class JpaHorarioSemanalRepositoryAdapterHabilitadosTest {

    private static final Instant AUDITORIA = Instant.parse("2026-07-01T12:00:00Z");

    @Autowired
    private JpaHorarioSemanalRepositoryAdapter adapter;

    @Autowired
    private JpaCategoriaRepository jpaCategoriaRepository;

    @Autowired
    private JpaProfesionalRepository jpaProfesionalRepository;

    @Autowired
    private JpaHorarioSemanalRepository jpaHorarioSemanalRepository;

    @BeforeEach
    void cargarCatalogo() {
        jpaCategoriaRepository.save(CategoriaEntity.builder().id(1L).nombre("Clinica medica").habilitada(true)
                .creadaEn(AUDITORIA).actualizadaEn(AUDITORIA).build());
        profesional(10L);
        profesional(11L);

        horario(100L, 10L, "MONDAY", true);
        horario(101L, 10L, "TUESDAY", false);
        horario(102L, 10L, "FRIDAY", true);
        horario(103L, 11L, "MONDAY", true);
    }

    private void profesional(Long id) {
        jpaProfesionalRepository.save(ProfesionalEntity.builder().id(id).categoriaId(1L).nombre("Ana")
                .apellido("Gomez").habilitado(true).creadoEn(AUDITORIA).actualizadoEn(AUDITORIA).build());
    }

    private void horario(Long id, Long profesionalId, String diaSemana, boolean habilitado) {
        jpaHorarioSemanalRepository.save(HorarioSemanalEntity.builder().id(id).profesionalId(profesionalId)
                .diaSemana(diaSemana).horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(12, 0))
                .duracionTurnoMinutos(30).habilitado(habilitado).creadoEn(AUDITORIA).actualizadoEn(AUDITORIA)
                .build());
    }

    @Test
    void devuelveSoloLosHorariosHabilitadosDelProfesional() {
        assertThat(adapter.findHabilitadosByProfesionalId(10L))
                .extracting(HorarioSemanal::getId)
                .containsExactlyInAnyOrder(100L, 102L);
    }

    @Test
    void devuelveVacioSiElProfesionalNoTieneHorariosHabilitados() {
        assertThat(adapter.findHabilitadosByProfesionalId(99L)).isEmpty();
    }
}
