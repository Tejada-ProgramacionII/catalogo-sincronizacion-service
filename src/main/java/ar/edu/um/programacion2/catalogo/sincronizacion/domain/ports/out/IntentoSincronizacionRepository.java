package ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out;

import java.util.List;
import java.util.Optional;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;

public interface IntentoSincronizacionRepository {
    IntentoSincronizacion save(IntentoSincronizacion intento);

    Optional<IntentoSincronizacion> findUltimo();

    List<IntentoSincronizacion> findAbiertos();
}
