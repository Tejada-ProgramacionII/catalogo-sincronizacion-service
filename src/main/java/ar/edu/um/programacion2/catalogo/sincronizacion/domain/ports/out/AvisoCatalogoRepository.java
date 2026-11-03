package ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.AvisoCatalogo;

public interface AvisoCatalogoRepository {
    boolean existsByEventId(String eventId);

    void save(AvisoCatalogo aviso);
}
