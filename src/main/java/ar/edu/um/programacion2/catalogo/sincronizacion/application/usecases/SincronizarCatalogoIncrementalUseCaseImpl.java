package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.categoria.domain.ports.out.CategoriaRepository;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.domain.ports.out.HorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatalogoNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.DiscontinuidadException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.CambiosVersion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionesPublicadas;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoIncrementalUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.CatalogoPublicadoClient;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SincronizarCatalogoIncrementalUseCaseImpl implements SincronizarCatalogoIncrementalUseCase {

    private final CatalogoPublicadoClient catalogoPublicadoClient;
    private final CategoriaRepository categoriaRepository;
    private final ProfesionalRepository profesionalRepository;
    private final HorarioSemanalRepository horarioSemanalRepository;
    private final VersionAplicadaRepository versionAplicadaRepository;
    // D-06: una transacción por versión. @Transactional no aplica en llamadas dentro de la misma clase.
    private final TransactionOperations transactionOperations;

    @Override
    public VersionAplicada sincronizar() {
        VersionAplicada local = versionAplicadaRepository.find().orElseThrow(CatalogoNoDisponibleException::new);
        VersionesPublicadas publicadas = catalogoPublicadoClient.getVersiones();
        // D-05: ya incorporada, no hay efectos nuevos.
        if (local.getVersion() >= publicadas.getActual()) {
            return local;
        }
        if (local.getVersion() < publicadas.getMasAntiguaDisponible()) {
            throw new DiscontinuidadException(String.format(
                    "La versión local %d es anterior a la más antigua disponible (%d)",
                    local.getVersion(), publicadas.getMasAntiguaDisponible()));
        }
        VersionAplicada ultima = local;
        for (long version = local.getVersion() + 1; version <= publicadas.getActual(); version++) {
            ultima = aplicarVersion(version);
        }
        return ultima;
    }

    // Lee todo antes de escribir: si falta algo, la versión no toca la base.
    private VersionAplicada aplicarVersion(Long version) {
        CambiosVersion cambios = catalogoPublicadoClient.getCambios(version)
                .orElseThrow(() -> new DiscontinuidadException("Faltan los cambios de la versión " + version));
        List<Categoria> categorias = todasPublicadas(
                catalogoPublicadoClient.getCategorias(cambios.getCategoriaIds()), cambios.getCategoriaIds(), version);
        List<Profesional> profesionales = todasPublicadas(
                catalogoPublicadoClient.getProfesionales(cambios.getProfesionalIds()), cambios.getProfesionalIds(),
                version);
        List<HorarioSemanal> horarios = todasPublicadas(
                catalogoPublicadoClient.getHorarios(cambios.getHorarioIds()), cambios.getHorarioIds(), version);
        VersionAplicada aplicada = VersionAplicada.builder().version(version).aplicadaEn(Instant.now()).build();

        transactionOperations.executeWithoutResult(estado -> {
            categorias.forEach(categoriaRepository::save);
            profesionales.forEach(profesionalRepository::save);
            horarios.forEach(horarioSemanalRepository::save);
            versionAplicadaRepository.save(aplicada);
        });
        return aplicada;
    }

    private static <T> List<T> todasPublicadas(List<T> publicadas, List<Long> ids, Long version) {
        if (publicadas.size() < ids.size()) {
            throw new DiscontinuidadException(
                    "La versión " + version + " cambia entidades que no están publicadas");
        }
        return publicadas;
    }
}
