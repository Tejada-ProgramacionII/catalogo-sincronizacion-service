package ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.adapter;

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

import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.entity.ProfesionalEntity;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.mapper.ProfesionalMapper;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.repository.JpaProfesionalRepository;

@ExtendWith(MockitoExtension.class)
class JpaProfesionalRepositoryAdapterTest {

    private static final Instant CREADO_EN = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant ACTUALIZADO_EN = Instant.parse("2026-09-10T13:40:00Z");

    @Mock
    private JpaProfesionalRepository jpaProfesionalRepository;

    private JpaProfesionalRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaProfesionalRepositoryAdapter(jpaProfesionalRepository, new ProfesionalMapper());
    }

    private static ProfesionalEntity entidad(Long id, String apellido) {
        return ProfesionalEntity.builder()
                .id(id)
                .categoriaId(1L)
                .nombre("Ana")
                .apellido(apellido)
                .habilitado(true)
                .creadoEn(CREADO_EN)
                .actualizadoEn(ACTUALIZADO_EN)
                .build();
    }

    @Test
    void saveGuardaElProfesionalConSuCategoria() {
        Profesional profesional = Profesional.builder()
                .id(15L)
                .categoriaId(1L)
                .nombre("Ana")
                .apellido("Gomez")
                .habilitado(true)
                .creadoEn(CREADO_EN)
                .actualizadoEn(ACTUALIZADO_EN)
                .build();
        when(jpaProfesionalRepository.save(any(ProfesionalEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Profesional guardado = adapter.save(profesional);

        verify(jpaProfesionalRepository).save(any(ProfesionalEntity.class));
        assertThat(guardado).usingRecursiveComparison().isEqualTo(profesional);
    }

    @Test
    void findByIdDevuelveElProfesionalCuandoExiste() {
        when(jpaProfesionalRepository.findById(15L)).thenReturn(Optional.of(entidad(15L, "Gomez")));

        Optional<Profesional> encontrado = adapter.findById(15L);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getApellido()).isEqualTo("Gomez");
    }

    @Test
    void findByIdDevuelveVacioCuandoNoExiste() {
        when(jpaProfesionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAllConvierteTodasLasEntidades() {
        when(jpaProfesionalRepository.findAll())
                .thenReturn(List.of(entidad(15L, "Gomez"), entidad(16L, "Perez")));

        List<Profesional> profesionales = adapter.findAll();

        assertThat(profesionales).extracting(Profesional::getId).containsExactly(15L, 16L);
    }
}
