package ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.SnapshotCatalogo;

public interface CatalogoCatedraClient {
    SnapshotCatalogo getSnapshot();
}
