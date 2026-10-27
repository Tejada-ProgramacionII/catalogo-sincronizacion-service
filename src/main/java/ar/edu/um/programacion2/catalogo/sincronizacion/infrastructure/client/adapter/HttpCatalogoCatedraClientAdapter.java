package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.adapter;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.SnapshotCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.CatalogoCatedraClient;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.SnapshotCatalogoResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.mapper.SnapshotCatalogoClientMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HttpCatalogoCatedraClientAdapter implements CatalogoCatedraClient {

    private static final String SNAPSHOT_PATH = "/api/synchronization/snapshot";

    private final RestClient catedraRestClient;
    private final SnapshotCatalogoClientMapper snapshotCatalogoClientMapper;

    @Override
    public SnapshotCatalogo getSnapshot() {
        SnapshotCatalogoResponse response = catedraRestClient.get()
                .uri(SNAPSHOT_PATH)
                .retrieve()
                .body(SnapshotCatalogoResponse.class);
        return snapshotCatalogoClientMapper.toDomainModel(response);
    }
}
