package ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;

public interface SincronizarCatalogoIncrementalUseCase {
    VersionAplicada sincronizar();
}
