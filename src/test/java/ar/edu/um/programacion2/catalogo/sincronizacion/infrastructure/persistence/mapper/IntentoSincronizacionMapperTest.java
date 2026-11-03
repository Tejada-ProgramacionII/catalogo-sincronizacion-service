package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.IntentoSincronizacionEntity;

class IntentoSincronizacionMapperTest {

    private static final Instant INICIO = Instant.parse("2026-10-27T10:00:00Z");
    private static final Instant FIN = Instant.parse("2026-10-27T10:00:05Z");

    private final IntentoSincronizacionMapper mapper = new IntentoSincronizacionMapper();

    @Test
    void toEntityCopiaLosCamposYGuardaElResultadoComoTexto() {
        IntentoSincronizacion intento = IntentoSincronizacion.builder()
                .id(3L).inicio(INICIO).fin(FIN).resultado(ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE)
                .intentos(3).mensaje("La cátedra no respondió después de 3 intentos").build();

        IntentoSincronizacionEntity entity = mapper.toEntity(intento);

        assertThat(entity.getId()).isEqualTo(3L);
        assertThat(entity.getInicio()).isEqualTo(INICIO);
        assertThat(entity.getFin()).isEqualTo(FIN);
        assertThat(entity.getResultado()).isEqualTo("CATEDRA_NO_DISPONIBLE");
        assertThat(entity.getIntentos()).isEqualTo(3);
        assertThat(entity.getVersionAplicada()).isNull();
        assertThat(entity.getMensaje()).isEqualTo("La cátedra no respondió después de 3 intentos");
    }

    @Test
    void toDomainModelCopiaLosCamposYTraduceElResultado() {
        IntentoSincronizacionEntity entity = IntentoSincronizacionEntity.builder()
                .id(4L).inicio(INICIO).fin(FIN).resultado("OK").intentos(1).versionAplicada(7L).build();

        IntentoSincronizacion intento = mapper.toDomainModel(entity);

        assertThat(intento.getId()).isEqualTo(4L);
        assertThat(intento.getInicio()).isEqualTo(INICIO);
        assertThat(intento.getFin()).isEqualTo(FIN);
        assertThat(intento.getResultado()).isEqualTo(ResultadoSincronizacion.OK);
        assertThat(intento.getIntentos()).isEqualTo(1);
        assertThat(intento.getVersionAplicada()).isEqualTo(7L);
        assertThat(intento.getMensaje()).isNull();
    }

    @Test
    void unIntentoAbiertoNoTieneResultado() {
        IntentoSincronizacion abierto = IntentoSincronizacion.builder().inicio(INICIO).build();

        assertThat(mapper.toEntity(abierto).getResultado()).isNull();
        assertThat(mapper.toDomainModel(mapper.toEntity(abierto)).getResultado()).isNull();
    }

    @Test
    void conNullDevuelveNull() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
