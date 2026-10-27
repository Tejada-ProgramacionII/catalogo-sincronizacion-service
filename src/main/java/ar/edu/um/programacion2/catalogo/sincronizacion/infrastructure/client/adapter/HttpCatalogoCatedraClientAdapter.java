package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.adapter;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraRechazoException;
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
        try {
            SnapshotCatalogoResponse response = catedraRestClient.get()
                    .uri(SNAPSHOT_PATH)
                    .retrieve()
                    .body(SnapshotCatalogoResponse.class);
            return snapshotCatalogoClientMapper.toDomainModel(response);
        } catch (HttpClientErrorException e) {
            throw new CatedraRechazoException(
                    "La cátedra rechazó el pedido del snapshot (HTTP " + e.getStatusCode().value() + ")", e);
        } catch (HttpServerErrorException e) {
            throw new CatedraNoDisponibleException(
                    "La cátedra respondió con un error al pedir el snapshot (HTTP " + e.getStatusCode().value() + ")", e);
        } catch (ResourceAccessException e) {
            throw new CatedraNoDisponibleException("No se pudo conectar con la cátedra para pedir el snapshot", e);
        }
    }
}
