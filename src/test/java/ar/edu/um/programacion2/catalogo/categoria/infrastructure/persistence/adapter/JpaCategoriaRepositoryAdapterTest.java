package ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.entity.CategoriaEntity;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.mapper.CategoriaMapper;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.repository.JpaCategoriaRepository;

@ExtendWith(MockitoExtension.class)
class JpaCategoriaRepositoryAdapterTest {

    private static final Instant CREADA_EN = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant ACTUALIZADA_EN = Instant.parse("2026-09-10T13:40:00Z");

    @Mock
    private JpaCategoriaRepository jpaCategoriaRepository;

    private JpaCategoriaRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaCategoriaRepositoryAdapter(jpaCategoriaRepository, new CategoriaMapper());
    }

    private static CategoriaEntity entidad(Long id, String nombre) {
        return CategoriaEntity.builder()
                .id(id)
                .nombre(nombre)
                .habilitada(true)
                .creadaEn(CREADA_EN)
                .actualizadaEn(ACTUALIZADA_EN)
                .build();
    }

    @Test
    void saveGuardaLaCategoriaConElIdDeLaCatedra() {
        Categoria categoria = Categoria.builder()
                .id(1L)
                .nombre("Clinica medica")
                .descripcion("Atencion clinica general")
                .habilitada(true)
                .creadaEn(CREADA_EN)
                .actualizadaEn(ACTUALIZADA_EN)
                .build();
        when(jpaCategoriaRepository.save(any(CategoriaEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Categoria guardada = adapter.save(categoria);

        verify(jpaCategoriaRepository).save(any(CategoriaEntity.class));
        assertThat(guardada).usingRecursiveComparison().isEqualTo(categoria);
    }

    @Test
    void findByIdDevuelveLaCategoriaCuandoExiste() {
        when(jpaCategoriaRepository.findById(1L)).thenReturn(Optional.of(entidad(1L, "Clinica medica")));

        Optional<Categoria> encontrada = adapter.findById(1L);

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNombre()).isEqualTo("Clinica medica");
    }

    @Test
    void findByIdDevuelveVacioCuandoNoExiste() {
        when(jpaCategoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAllConvierteTodasLasEntidades() {
        when(jpaCategoriaRepository.findAll())
                .thenReturn(List.of(entidad(1L, "Clinica medica"), entidad(2L, "Pediatria")));

        List<Categoria> categorias = adapter.findAll();

        assertThat(categorias).extracting(Categoria::getId).containsExactly(1L, 2L);
    }

    @Test
    void deleteAllBorraTodoEnUnaSolaSentencia() {
        adapter.deleteAll();

        verify(jpaCategoriaRepository).deleteAllInBatch();
    }
}
