package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.sun.net.httpserver.HttpServer;

import ar.edu.um.programacion2.catalogo.config.CatedraClientConfig;
import ar.edu.um.programacion2.catalogo.config.CatedraProperties;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraRechazoException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.SnapshotCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.mapper.SnapshotCatalogoClientMapper;

class HttpCatalogoCatedraClientAdapterTest {

    private static final String JWT = "jwt-de-prueba";

    // Ejemplo de la sección 7 del contrato, con una hora sin segundos y campos que no conocemos.
    private static final String SNAPSHOT_JSON = """
            {
              "snapshotVersion": 7,
              "generatedAt": "2026-09-10T13:44:59Z",
              "campoNuevo": "se ignora",
              "professionalCategories": [
                {
                  "id": 1,
                  "name": "Clinica medica",
                  "description": "Atencion clinica general",
                  "enabled": true,
                  "createdAt": "2026-07-01T12:00:00Z",
                  "updatedAt": "2026-09-10T13:40:00Z"
                }
              ],
              "professionals": [
                {
                  "id": 15,
                  "categoryId": 1,
                  "firstName": "Ana",
                  "lastName": "Gomez",
                  "enabled": false,
                  "createdAt": "2026-07-01T12:00:00Z",
                  "updatedAt": "2026-09-10T13:40:00Z",
                  "otroCampoNuevo": 42
                }
              ],
              "weeklySchedules": [
                {
                  "id": 21,
                  "professionalId": 15,
                  "dayOfWeek": "MONDAY",
                  "startTime": "09:00:00",
                  "endTime": "12:00",
                  "slotDurationMinutes": 30,
                  "enabled": true,
                  "createdAt": "2026-07-01T12:00:00Z",
                  "updatedAt": "2026-09-10T13:40:00Z"
                }
              ]
            }
            """;

    private final AtomicReference<String> pathRecibido = new AtomicReference<>();
    private final AtomicInteger statusRespuesta = new AtomicInteger(200);
    private HttpServer servidor;

    @BeforeEach
    void levantarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/", exchange -> {
            pathRecibido.set(exchange.getRequestURI().getPath());
            byte[] cuerpo = SNAPSHOT_JSON.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            exchange.sendResponseHeaders(statusRespuesta.get(), cuerpo.length);
            try (OutputStream salida = exchange.getResponseBody()) {
                salida.write(cuerpo);
            }
        });
        servidor.start();
    }

    @AfterEach
    void detenerServidor() {
        servidor.stop(0);
    }

    @Test
    void getSnapshotPideElSnapshotYLoTraduceAlDominio() {
        SnapshotCatalogo snapshot = crearAdapter(servidor.getAddress().getPort(), Duration.ofSeconds(5)).getSnapshot();

        assertThat(pathRecibido.get()).isEqualTo("/api/synchronization/snapshot");
        assertThat(snapshot.getVersion()).isEqualTo(7L);
        assertThat(snapshot.getGeneradoEn()).isEqualTo(Instant.parse("2026-09-10T13:44:59Z"));
        assertThat(snapshot.getCategorias()).singleElement()
                .satisfies(categoria -> assertThat(categoria.getNombre()).isEqualTo("Clinica medica"));
        assertThat(snapshot.getProfesionales()).singleElement()
                .satisfies(profesional -> assertThat(profesional.isHabilitado()).isFalse());
        assertThat(snapshot.getHorarios()).singleElement().satisfies(horario -> {
            assertThat(horario.getDiaSemana()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(horario.getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
            assertThat(horario.getHoraFin()).isEqualTo(LocalTime.of(12, 0));
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 404})
    void getSnapshotLanzaRechazoSiLaCatedraRespondeUn4xx(int status) {
        statusRespuesta.set(status);
        HttpCatalogoCatedraClientAdapter adapter = crearAdapter(servidor.getAddress().getPort(), Duration.ofSeconds(5));

        assertThatThrownBy(adapter::getSnapshot)
                .isInstanceOf(CatedraRechazoException.class)
                .hasMessageContaining("HTTP " + status)
                .hasMessageNotContaining(JWT);
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 503})
    void getSnapshotLanzaNoDisponibleSiLaCatedraRespondeUn5xx(int status) {
        statusRespuesta.set(status);
        HttpCatalogoCatedraClientAdapter adapter = crearAdapter(servidor.getAddress().getPort(), Duration.ofSeconds(5));

        assertThatThrownBy(adapter::getSnapshot)
                .isInstanceOf(CatedraNoDisponibleException.class)
                .hasMessageContaining("HTTP " + status)
                .hasMessageNotContaining(JWT);
    }

    @Test
    void getSnapshotLanzaNoDisponibleSiNoHayNadieEscuchando() throws IOException {
        int puertoLibre;
        try (ServerSocket socket = new ServerSocket(0)) {
            puertoLibre = socket.getLocalPort();
        }
        HttpCatalogoCatedraClientAdapter adapter = crearAdapter(puertoLibre, Duration.ofSeconds(5));

        assertThatThrownBy(adapter::getSnapshot)
                .isInstanceOf(CatedraNoDisponibleException.class)
                .hasMessageNotContaining(JWT);
    }

    @Test
    void getSnapshotLanzaNoDisponibleSiLaCatedraNoRespondeATiempo() throws IOException {
        // Acepta la conexión pero nunca responde: la lectura corta por timeout.
        try (ServerSocket servidorMudo = new ServerSocket(0)) {
            HttpCatalogoCatedraClientAdapter adapter = crearAdapter(servidorMudo.getLocalPort(), Duration.ofMillis(200));

            assertThatThrownBy(adapter::getSnapshot)
                    .isInstanceOf(CatedraNoDisponibleException.class)
                    .hasMessageNotContaining(JWT);
        }
    }

    private HttpCatalogoCatedraClientAdapter crearAdapter(int puerto, Duration readTimeout) {
        CatedraProperties properties = new CatedraProperties();
        properties.setBaseUrl("http://localhost:" + puerto);
        properties.setJwt(JWT);
        properties.setReadTimeout(readTimeout);
        RestClient restClient = new CatedraClientConfig().catedraRestClient(RestClient.builder(), properties);
        return new HttpCatalogoCatedraClientAdapter(restClient, new SnapshotCatalogoClientMapper());
    }
}
