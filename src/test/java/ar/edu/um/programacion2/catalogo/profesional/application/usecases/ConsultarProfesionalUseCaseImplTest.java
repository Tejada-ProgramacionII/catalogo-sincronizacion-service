package ar.edu.um.programacion2.catalogo.profesional.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.domain.ports.out.HorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.AgendaProfesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatalogoNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;

@ExtendWith(MockitoExtension.class)
class ConsultarProfesionalUseCaseImplTest {

    @Mock
    private ProfesionalRepository profesionalRepository;

    @Mock
    private HorarioSemanalRepository horarioSemanalRepository;

    @Mock
    private VersionAplicadaRepository versionAplicadaRepository;

    @InjectMocks
    private ConsultarProfesionalUseCaseImpl useCase;

    private void conVersionAplicada(Long version) {
        when(versionAplicadaRepository.find())
                .thenReturn(Optional.of(VersionAplicada.builder().version(version).aplicadaEn(Instant.now()).build()));
    }

    private static HorarioSemanal horario(Long id, DayOfWeek dia, int hora, int minuto) {
        return HorarioSemanal.builder().id(id).profesionalId(10L).diaSemana(dia)
                .horaInicio(LocalTime.of(hora, minuto)).build();
    }

    @Test
    void devuelveElProfesionalConSusHorariosHabilitadosYLaVersion() {
        conVersionAplicada(7L);
        Profesional profesional = Profesional.builder().id(10L).habilitado(true).build();
        HorarioSemanal lunes = HorarioSemanal.builder().id(100L).profesionalId(10L).diaSemana(DayOfWeek.MONDAY).build();
        when(profesionalRepository.findById(10L)).thenReturn(Optional.of(profesional));
        when(horarioSemanalRepository.findHabilitadosByProfesionalId(10L)).thenReturn(List.of(lunes));

        Optional<AgendaProfesional> agenda = useCase.consultarProfesional(10L);

        assertThat(agenda).hasValueSatisfying(a -> {
            assertThat(a.getProfesional()).isSameAs(profesional);
            assertThat(a.getHorarios()).containsExactly(lunes);
            assertThat(a.getVersionCatalogo()).isEqualTo(7L);
        });
    }

    @Test
    void ordenaLosHorariosPorDiaYHoraDeInicio() {
        conVersionAplicada(7L);
        when(profesionalRepository.findById(10L)).thenReturn(Optional.of(Profesional.builder().id(10L).build()));
        when(horarioSemanalRepository.findHabilitadosByProfesionalId(10L)).thenReturn(List.of(
                horario(1L, DayOfWeek.THURSDAY, 18, 0),
                horario(2L, DayOfWeek.TUESDAY, 11, 30),
                horario(3L, DayOfWeek.MONDAY, 14, 0),
                horario(4L, DayOfWeek.TUESDAY, 9, 0)));

        assertThat(useCase.consultarProfesional(10L))
                .hasValueSatisfying(a -> assertThat(a.getHorarios())
                        .extracting(HorarioSemanal::getId)
                        .containsExactly(3L, 4L, 2L, 1L));
    }

    @Test
    void unProfesionalDeshabilitadoSeDevuelveIgual() {
        conVersionAplicada(7L);
        when(profesionalRepository.findById(10L))
                .thenReturn(Optional.of(Profesional.builder().id(10L).habilitado(false).build()));
        when(horarioSemanalRepository.findHabilitadosByProfesionalId(10L)).thenReturn(List.of());

        assertThat(useCase.consultarProfesional(10L))
                .hasValueSatisfying(a -> assertThat(a.getProfesional().isHabilitado()).isFalse());
    }

    @Test
    void sinHorariosHabilitadosDevuelveLaListaVacia() {
        conVersionAplicada(7L);
        when(profesionalRepository.findById(10L)).thenReturn(Optional.of(Profesional.builder().id(10L).build()));
        when(horarioSemanalRepository.findHabilitadosByProfesionalId(10L)).thenReturn(List.of());

        assertThat(useCase.consultarProfesional(10L))
                .hasValueSatisfying(a -> assertThat(a.getHorarios()).isEmpty());
    }

    @Test
    void siElProfesionalNoExisteDevuelveVacioSinBuscarHorarios() {
        conVersionAplicada(7L);
        when(profesionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(useCase.consultarProfesional(99L)).isEmpty();
        verifyNoInteractions(horarioSemanalRepository);
    }

    @Test
    void sinVersionAplicadaElCatalogoNoEstaDisponible() {
        when(versionAplicadaRepository.find()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.consultarProfesional(10L))
                .isInstanceOf(CatalogoNoDisponibleException.class);
        verifyNoInteractions(profesionalRepository, horarioSemanalRepository);
    }
}
