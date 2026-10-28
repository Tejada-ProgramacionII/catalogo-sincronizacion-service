package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionesPublicadas;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.mapper.CambiosVersionCacheMapper;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.mapper.SnapshotCatalogoClientMapper;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class RedisCatalogoPublicadoClientAdapterTest {

    // Ejemplos de la sección 14 del contrato, con un campo que no conocemos.
    private static final String CATEGORIA_1 = """
            {"id": 1, "name": "Clinica medica", "description": "Atencion clinica general", "enabled": true,
             "createdAt": "2026-07-01T12:00:00Z", "updatedAt": "2026-09-10T13:40:00Z", "campoNuevo": "se ignora"}
            """;
    private static final String PROFESIONAL_15 = """
            {"id": 15, "categoryId": 1, "firstName": "Ana", "lastName": "Gomez", "enabled": false,
             "createdAt": "2026-07-01T12:00:00Z", "updatedAt": "2026-09-10T13:40:00Z"}
            """;
    private static final String HORARIO_21 = """
            {"id": 21, "professionalId": 15, "dayOfWeek": "MONDAY", "startTime": "09:00:00", "endTime": "12:00:00",
             "slotDurationMinutes": 30, "enabled": true,
             "createdAt": "2026-07-01T12:00:00Z", "updatedAt": "2026-09-10T13:40:00Z"}
            """;
    private static final String CAMBIOS_7 = """
            {"version": 7, "changes": {"professionalCategories": [], "professionals": [15], "weeklySchedules": [21, 22]}}
            """;

    @Mock
    private StringRedisTemplate catedraRedisTemplate;

    @Mock
    private ValueOperations<String, String> valores;

    @Mock
    private HashOperations<String, Object, Object> hashes;

    private RedisCatalogoPublicadoClientAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RedisCatalogoPublicadoClientAdapter(catedraRedisTemplate, JsonMapper.builder().build(),
                new CambiosVersionCacheMapper(), new SnapshotCatalogoClientMapper());
    }

    @Test
    void getVersionesLeeLaActualYLaMasAntiguaEnUnSoloPedido() {
        when(catedraRedisTemplate.opsForValue()).thenReturn(valores);
        when(valores.multiGet(List.of(RedisCatalogoPublicadoClientAdapter.VERSION_ACTUAL,
                RedisCatalogoPublicadoClientAdapter.VERSION_MAS_ANTIGUA))).thenReturn(List.of("7", "4"));

        VersionesPublicadas versiones = adapter.getVersiones();

        assertThat(versiones.getActual()).isEqualTo(7L);
        assertThat(versiones.getMasAntiguaDisponible()).isEqualTo(4L);
    }

    @Test
    void getVersionesSinLasClavesPublicadasLanzaNoDisponible() {
        when(catedraRedisTemplate.opsForValue()).thenReturn(valores);
        when(valores.multiGet(anyList())).thenReturn(Arrays.asList("7", null));

        assertThatThrownBy(adapter::getVersiones)
                .isInstanceOf(CatedraNoDisponibleException.class)
                .hasMessage("Redis no publica las versiones del catálogo");
    }

    @Test
    void getVersionesSinRespuestaLanzaNoDisponible() {
        when(catedraRedisTemplate.opsForValue()).thenReturn(valores);
        when(valores.multiGet(anyList())).thenReturn(null);

        assertThatThrownBy(adapter::getVersiones).isInstanceOf(CatedraNoDisponibleException.class);
    }

    @Test
    void getCambiosTraduceLosIdsDeLaVersion() {
        when(catedraRedisTemplate.opsForValue()).thenReturn(valores);
        when(valores.get("catedra:sync:changes:7")).thenReturn(CAMBIOS_7);

        assertThat(adapter.getCambios(7L)).hasValueSatisfying(cambios -> {
            assertThat(cambios.getVersion()).isEqualTo(7L);
            assertThat(cambios.getCategoriaIds()).isEmpty();
            assertThat(cambios.getProfesionalIds()).containsExactly(15L);
            assertThat(cambios.getHorarioIds()).containsExactly(21L, 22L);
        });
    }

    @Test
    void getCambiosDeUnaVersionQueYaNoEstaDevuelveVacio() {
        when(catedraRedisTemplate.opsForValue()).thenReturn(valores);
        when(valores.get("catedra:sync:changes:3")).thenReturn(null);

        assertThat(adapter.getCambios(3L)).isEmpty();
    }

    @Test
    void getCategoriasTraduceElJsonDelHash() {
        cuandoElHash(RedisCatalogoPublicadoClientAdapter.CATEGORIAS, List.of("1"), CATEGORIA_1);

        List<Categoria> categorias = adapter.getCategorias(List.of(1L));

        assertThat(categorias).singleElement().satisfies(categoria -> {
            assertThat(categoria.getId()).isEqualTo(1L);
            assertThat(categoria.getNombre()).isEqualTo("Clinica medica");
            assertThat(categoria.isHabilitada()).isTrue();
            assertThat(categoria.getActualizadaEn()).isEqualTo(Instant.parse("2026-09-10T13:40:00Z"));
        });
    }

    @Test
    void getProfesionalesDescartaLosIdsQueNoEstanPublicados() {
        cuandoElHash(RedisCatalogoPublicadoClientAdapter.PROFESIONALES, List.of("15", "99"), PROFESIONAL_15, null);

        List<Profesional> profesionales = adapter.getProfesionales(List.of(15L, 99L));

        assertThat(profesionales).singleElement().satisfies(profesional -> {
            assertThat(profesional.getId()).isEqualTo(15L);
            assertThat(profesional.getApellido()).isEqualTo("Gomez");
            assertThat(profesional.isHabilitado()).isFalse();
        });
    }

    @Test
    void getHorariosTraduceDiaYHoras() {
        cuandoElHash(RedisCatalogoPublicadoClientAdapter.HORARIOS, List.of("21"), HORARIO_21);

        List<HorarioSemanal> horarios = adapter.getHorarios(List.of(21L));

        assertThat(horarios).singleElement().satisfies(horario -> {
            assertThat(horario.getProfesionalId()).isEqualTo(15L);
            assertThat(horario.getDiaSemana()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(horario.getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
            assertThat(horario.getDuracionTurnoMinutos()).isEqualTo(30);
        });
    }

    @Test
    void sinIdsNoConsultaRedis() {
        assertThat(adapter.getProfesionales(List.of())).isEmpty();
        verifyNoInteractions(catedraRedisTemplate);
    }

    @Test
    void siRedisNoRespondeLanzaNoDisponibleSinElDetalle() {
        when(catedraRedisTemplate.opsForValue()).thenReturn(valores);
        when(valores.get("catedra:sync:changes:7"))
                .thenThrow(new RedisConnectionFailureException("Unable to connect to 172.17.201.1:6379"));

        assertThatThrownBy(() -> adapter.getCambios(7L))
                .isInstanceOf(CatedraNoDisponibleException.class)
                .hasMessage("No se pudo leer el catálogo publicado en Redis");
    }

    private void cuandoElHash(String clave, List<Object> campos, String... json) {
        when(catedraRedisTemplate.<Object, Object>opsForHash()).thenReturn(hashes);
        when(hashes.multiGet(clave, campos)).thenReturn(Arrays.asList((Object[]) json));
    }
}
