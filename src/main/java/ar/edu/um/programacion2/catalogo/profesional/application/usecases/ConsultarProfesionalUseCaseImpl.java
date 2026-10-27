package ar.edu.um.programacion2.catalogo.profesional.application.usecases;

import java.util.Comparator;
import java.util.Optional;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.domain.ports.out.HorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.AgendaProfesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.in.ConsultarProfesionalUseCase;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatalogoNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConsultarProfesionalUseCaseImpl implements ConsultarProfesionalUseCase {

    private static final Comparator<HorarioSemanal> POR_DIA_Y_HORA_DE_INICIO =
            Comparator.comparing(HorarioSemanal::getDiaSemana).thenComparing(HorarioSemanal::getHoraInicio);

    private final ProfesionalRepository profesionalRepository;
    private final HorarioSemanalRepository horarioSemanalRepository;
    private final VersionAplicadaRepository versionAplicadaRepository;

    @Override
    public Optional<AgendaProfesional> consultarProfesional(Long id) {
        // D-04: sin una copia completa no se responde como si el profesional no existiera.
        VersionAplicada version = versionAplicadaRepository.find()
                .orElseThrow(CatalogoNoDisponibleException::new);
        return profesionalRepository.findById(id)
                .map(profesional -> AgendaProfesional.builder()
                        .profesional(profesional)
                        .horarios(horarioSemanalRepository.findHabilitadosByProfesionalId(id).stream()
                                .sorted(POR_DIA_Y_HORA_DE_INICIO)
                                .toList())
                        .versionCatalogo(version.getVersion())
                        .build());
    }
}
