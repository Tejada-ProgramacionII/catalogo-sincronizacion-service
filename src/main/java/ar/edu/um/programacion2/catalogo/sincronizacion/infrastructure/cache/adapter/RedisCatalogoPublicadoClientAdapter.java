package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.adapter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraRechazoException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.CambiosVersion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionesPublicadas;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.CatalogoPublicadoClient;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.dto.CambiosVersionCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.mapper.CambiosVersionCacheMapper;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.CategoriaCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.HorarioSemanalCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.ProfesionalCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.mapper.SnapshotCatalogoClientMapper;
import io.lettuce.core.RedisCommandExecutionException;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class RedisCatalogoPublicadoClientAdapter implements CatalogoPublicadoClient {

    static final String VERSION_ACTUAL = "catedra:sync:current-version";
    static final String VERSION_MAS_ANTIGUA = "catedra:sync:oldest-available-version";
    static final String CAMBIOS = "catedra:sync:changes:";
    static final String CATEGORIAS = "catedra:sync:professional-categories";
    static final String PROFESIONALES = "catedra:sync:professionals";
    static final String HORARIOS = "catedra:sync:weekly-schedules";

    // Tipos de error de RESP (la primera palabra del error) que indican credenciales o permisos inválidos.
    private static final List<String> ERRORES_DE_RECHAZO = List.of("WRONGPASS", "NOPERM");

    private final StringRedisTemplate catedraRedisTemplate;
    private final JsonMapper jsonMapper;
    private final CambiosVersionCacheMapper cambiosVersionCacheMapper;
    private final SnapshotCatalogoClientMapper snapshotCatalogoClientMapper;

    @Override
    public VersionesPublicadas getVersiones() {
        List<String> valores = leer(() -> catedraRedisTemplate.opsForValue()
                .multiGet(List.of(VERSION_ACTUAL, VERSION_MAS_ANTIGUA)));
        if (valores == null || valores.stream().anyMatch(Objects::isNull)) {
            throw new CatedraNoDisponibleException("Redis no publica las versiones del catálogo");
        }
        return VersionesPublicadas.builder()
                .actual(Long.valueOf(valores.get(0)))
                .masAntiguaDisponible(Long.valueOf(valores.get(1)))
                .build();
    }

    @Override
    public Optional<CambiosVersion> getCambios(Long version) {
        String json = leer(() -> catedraRedisTemplate.opsForValue().get(CAMBIOS + version));
        return Optional.ofNullable(json)
                .map(texto -> cambiosVersionCacheMapper.toDomainModel(
                        jsonMapper.readValue(texto, CambiosVersionCatedraResponse.class)));
    }

    @Override
    public List<Categoria> getCategorias(List<Long> ids) {
        return leerHash(CATEGORIAS, ids, CategoriaCatedraResponse.class, snapshotCatalogoClientMapper::toCategoria);
    }

    @Override
    public List<Profesional> getProfesionales(List<Long> ids) {
        return leerHash(PROFESIONALES, ids, ProfesionalCatedraResponse.class,
                snapshotCatalogoClientMapper::toProfesional);
    }

    @Override
    public List<HorarioSemanal> getHorarios(List<Long> ids) {
        return leerHash(HORARIOS, ids, HorarioSemanalCatedraResponse.class, snapshotCatalogoClientMapper::toHorario);
    }

    // HMGET devuelve null en la posición de cada id que no está en el Hash: esos se descartan.
    private <D, M> List<M> leerHash(String clave, List<Long> ids, Class<D> tipo, Function<D, M> toDomainModel) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Object> campos = ids.stream().<Object>map(String::valueOf).toList();
        List<Object> valores = leer(() -> catedraRedisTemplate.opsForHash().multiGet(clave, campos));
        return valores.stream()
                .filter(Objects::nonNull)
                .map(valor -> toDomainModel.apply(jsonMapper.readValue((String) valor, tipo)))
                .toList();
    }

    private static <T> T leer(Supplier<T> lectura) {
        try {
            return lectura.get();
        } catch (DataAccessException e) {
            if (esRechazo(e)) {
                throw new CatedraRechazoException("Redis rechazó las credenciales o los permisos de la integración", e);
            }
            throw new CatedraNoDisponibleException("No se pudo leer el catálogo publicado en Redis", e);
        }
    }

    private static boolean esRechazo(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof RedisCommandExecutionException && causa.getMessage() != null
                    && ERRORES_DE_RECHAZO.stream().anyMatch(causa.getMessage()::startsWith)) {
                return true;
            }
        }
        return false;
    }
}
