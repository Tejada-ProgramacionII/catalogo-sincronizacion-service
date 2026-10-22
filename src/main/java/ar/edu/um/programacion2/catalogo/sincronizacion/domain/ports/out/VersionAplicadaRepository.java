package ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out;

import java.util.Optional;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;

public interface VersionAplicadaRepository {
    Optional<VersionAplicada> find();

    VersionAplicada save(VersionAplicada versionAplicada);
}
