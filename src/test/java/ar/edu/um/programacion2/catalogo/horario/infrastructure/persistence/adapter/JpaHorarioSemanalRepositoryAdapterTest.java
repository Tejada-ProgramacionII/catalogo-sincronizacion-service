package ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.entity.HorarioSemanalEntity;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.mapper.HorarioSemanalMapper;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.repository.JpaHorarioSemanalRepository;

@ExtendWith(MockitoExtension.class)
class JpaHorarioSemanalRepositoryAdapterTest {

    private static final Instant CREADO_EN = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant ACTUALIZADO_EN = Instant.parse("2026-09-10T13:40:00Z");

    @Mock
    private JpaHorarioSemanalRepository jpaHorarioSemanalRepository;

    private JpaHorarioSemanalRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaHorarioSemanalRepositoryAdapter(jpaHorarioSemanalRepository, new HorarioSemanalMapper());
    }

    private static HorarioSemanalEntity entidad(Long id, String diaSemana) {
        return HorarioSemanalEntity.builder()
                .id(id)
                .profesionalId(15L)
                .diaSemana(diaSemana)
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(12, 0))
                .duracionTurnoMinutos(30)
                .habilitado(true)
                .creadoEn(CREADO_EN)
                .actualizadoEn(ACTUALIZADO_EN)
                .build();
    }

    @Test
    void saveGuardaElHorarioConSuProfesional() {
        HorarioSemanal horario = HorarioSemanal.builder()
                .id(21L)
                .profesionalId(15L)
                .diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(12, 0))
                .duracionTurnoMinutos(30)
                .habilitado(true)
                .creadoEn(CREADO_EN)
                .actualizadoEn(ACTUALIZADO_EN)
                .build();
        when(jpaHorarioSemanalRepository.save(any(HorarioSemanalEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        HorarioSemanal guardado = adapter.save(horario);

        verify(jpaHorarioSemanalRepository).save(any(HorarioSemanalEntity.class));
        assertThat(guardado).usingRecursiveComparison().isEqualTo(horario);
    }

    @Test
    void findByIdDevuelveElHorarioConElDiaConvertido() {
        when(jpaHorarioSemanalRepository.findById(21L)).thenReturn(Optional.of(entidad(21L, "MONDAY")));

        Optional<HorarioSemanal> encontrado = adapter.findById(21L);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getDiaSemana()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void findByIdDevuelveVacioCuandoNoExiste() {
        when(jpaHorarioSemanalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAllConvierteTodasLasEntidades() {
        when(jpaHorarioSemanalRepository.findAll())
                .thenReturn(List.of(entidad(21L, "MONDAY"), entidad(22L, "FRIDAY")));

        List<HorarioSemanal> horarios = adapter.findAll();

        assertThat(horarios).extracting(HorarioSemanal::getId).containsExactly(21L, 22L);
    }

    @Test
    void findHabilitadosByProfesionalIdDevuelveLosHorariosHabilitadosDelProfesional() {
        when(jpaHorarioSemanalRepository.findByProfesionalIdAndHabilitadoTrue(10L))
                .thenReturn(List.of(entidad(100L, "MONDAY")));

        List<HorarioSemanal> horarios = adapter.findHabilitadosByProfesionalId(10L);

        assertThat(horarios).extracting(HorarioSemanal::getId).containsExactly(100L);
        assertThat(horarios.get(0).getDiaSemana()).isEqualTo(DayOfWeek.MONDAY);
    }
}
