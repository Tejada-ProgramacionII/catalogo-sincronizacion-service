package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.VersionAplicadaEntity;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.mapper.VersionAplicadaMapper;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository.JpaVersionAplicadaRepository;

@ExtendWith(MockitoExtension.class)
class JpaVersionAplicadaRepositoryAdapterTest {

    private static final Instant APLICADA_EN = Instant.parse("2026-09-10T13:44:59Z");

    @Mock
    private JpaVersionAplicadaRepository jpaVersionAplicadaRepository;

    private JpaVersionAplicadaRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaVersionAplicadaRepositoryAdapter(jpaVersionAplicadaRepository, new VersionAplicadaMapper());
    }

    @Test
    void findDevuelveVacioCuandoNuncaSeSincronizo() {
        when(jpaVersionAplicadaRepository.findById(VersionAplicadaEntity.ID_UNICO)).thenReturn(Optional.empty());

        assertThat(adapter.find()).isEmpty();
    }

    @Test
    void findDevuelveLaVersionGuardada() {
        VersionAplicadaEntity entity = VersionAplicadaEntity.builder()
                .id(VersionAplicadaEntity.ID_UNICO)
                .version(7L)
                .aplicadaEn(APLICADA_EN)
                .build();
        when(jpaVersionAplicadaRepository.findById(VersionAplicadaEntity.ID_UNICO)).thenReturn(Optional.of(entity));

        Optional<VersionAplicada> encontrada = adapter.find();

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getVersion()).isEqualTo(7L);
    }

    @Test
    void saveGuardaSiempreConElIdUnico() {
        VersionAplicada versionAplicada = VersionAplicada.builder()
                .version(8L)
                .aplicadaEn(APLICADA_EN)
                .build();
        when(jpaVersionAplicadaRepository.save(any(VersionAplicadaEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        ArgumentCaptor<VersionAplicadaEntity> captor = ArgumentCaptor.forClass(VersionAplicadaEntity.class);

        VersionAplicada guardada = adapter.save(versionAplicada);

        verify(jpaVersionAplicadaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(VersionAplicadaEntity.ID_UNICO);
        assertThat(guardada.getVersion()).isEqualTo(8L);
        assertThat(guardada.getAplicadaEn()).isEqualTo(APLICADA_EN);
    }
}
