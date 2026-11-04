package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import java.util.Optional;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.VerificarVersionesPendientesUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.CatalogoPublicadoClient;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VerificarVersionesPendientesUseCaseImpl implements VerificarVersionesPendientesUseCase {

    private final VersionAplicadaRepository versionAplicadaRepository;
    private final CatalogoPublicadoClient catalogoPublicadoClient;

    // D-07: Redis decide. Sin copia local siempre hay algo pendiente (la completa).
    @Override
    public boolean hayVersionesPendientes() {
        Optional<VersionAplicada> local = versionAplicadaRepository.find();
        return local.isEmpty() || local.get().getVersion() < catalogoPublicadoClient.getVersiones().getActual();
    }
}
