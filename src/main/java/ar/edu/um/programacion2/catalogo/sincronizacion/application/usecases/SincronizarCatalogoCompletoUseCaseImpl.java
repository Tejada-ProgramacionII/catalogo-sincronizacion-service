package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.programacion2.catalogo.categoria.domain.ports.out.CategoriaRepository;
import ar.edu.um.programacion2.catalogo.horario.domain.ports.out.HorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.SnapshotCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoCompletoUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.CatalogoCatedraClient;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SincronizarCatalogoCompletoUseCaseImpl implements SincronizarCatalogoCompletoUseCase {

    private final CatalogoCatedraClient catalogoCatedraClient;
    private final CategoriaRepository categoriaRepository;
    private final ProfesionalRepository profesionalRepository;
    private final HorarioSemanalRepository horarioSemanalRepository;
    private final VersionAplicadaRepository versionAplicadaRepository;

    @Override
    @Transactional
    public VersionAplicada sincronizar() {
        SnapshotCatalogo snapshot = catalogoCatedraClient.getSnapshot();

        horarioSemanalRepository.deleteAll();
        profesionalRepository.deleteAll();
        categoriaRepository.deleteAll();

        snapshot.getCategorias().forEach(categoriaRepository::save);
        snapshot.getProfesionales().forEach(profesionalRepository::save);
        snapshot.getHorarios().forEach(horarioSemanalRepository::save);

        return versionAplicadaRepository.save(VersionAplicada.builder()
                .version(snapshot.getVersion())
                .aplicadaEn(Instant.now())
                .build());
    }
}
