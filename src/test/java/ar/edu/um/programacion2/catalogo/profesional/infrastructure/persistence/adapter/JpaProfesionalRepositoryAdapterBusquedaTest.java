package ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.entity.CategoriaEntity;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.repository.JpaCategoriaRepository;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.entity.HorarioSemanalEntity;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.repository.JpaHorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.entity.ProfesionalEntity;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.repository.JpaProfesionalRepository;

// Prueba la consulta real contra la base (H2 con las migraciones de Flyway); cada test se revierte.
@SpringBootTest
@Transactional
class JpaProfesionalRepositoryAdapterBusquedaTest {

    private static final Instant AUDITORIA = Instant.parse("2026-07-01T12:00:00Z");
    private static final LocalDate UN_LUNES = LocalDate.of(2026, 9, 14);

    @Autowired
    private JpaProfesionalRepositoryAdapter adapter;

    @Autowired
    private JpaCategoriaRepository jpaCategoriaRepository;

    @Autowired
    private JpaProfesionalRepository jpaProfesionalRepository;

    @Autowired
    private JpaHorarioSemanalRepository jpaHorarioSemanalRepository;

    @BeforeEach
    void cargarCatalogo() {
        categoria(1L, "Clinica medica");
        categoria(2L, "Pediatria");

        profesional(10L, 1L, "Ana", "Gomez", true);
        profesional(11L, 1L, "Bruno", "Alvarez", true);
        profesional(12L, 2L, "Carla", "Gomez", true);
        profesional(13L, 2L, "Diego", "Zapata", false);

        horario(100L, 10L, "MONDAY", true);
        horario(101L, 11L, "MONDAY", false);
        horario(102L, 11L, "TUESDAY", true);
        horario(103L, 12L, "MONDAY", true);
        horario(104L, 13L, "MONDAY", true);
    }

    @Test
    void sinFiltrosDevuelveTodosOrdenadosPorApellidoYNombre() {
        assertThat(ids(buscar(new FiltroBusquedaProfesionales()))).containsExactly(11L, 10L, 12L, 13L);
    }

    @Test
    void filtraPorCategoria() {
        assertThat(ids(buscar(FiltroBusquedaProfesionales.builder().categoriaId(2L).build())))
                .containsExactly(12L, 13L);
    }

    @Test
    void filtraPorEstadoHabilitado() {
        assertThat(ids(buscar(FiltroBusquedaProfesionales.builder().habilitado(false).build())))
                .containsExactly(13L);
    }

    @Test
    void filtraPorNombreOApellidoParcialSinDistinguirMayusculas() {
        assertThat(ids(buscar(FiltroBusquedaProfesionales.builder().nombre("GOM").build())))
                .containsExactly(10L, 12L);
        assertThat(ids(buscar(FiltroBusquedaProfesionales.builder().nombre("bru").build())))
                .containsExactly(11L);
    }

    @Test
    void nombreEnBlancoNoFiltra() {
        assertThat(ids(buscar(FiltroBusquedaProfesionales.builder().nombre("  ").build())))
                .containsExactly(11L, 10L, 12L, 13L);
    }

    @Test
    void filtraPorDisponibilidadSegunElDiaDeLaFecha() {
        // 11 tiene el lunes deshabilitado y 13 es un profesional deshabilitado.
        assertThat(ids(buscar(FiltroBusquedaProfesionales.builder().fecha(UN_LUNES).build())))
                .containsExactly(10L, 12L);
    }

    @Test
    void combinaLosFiltros() {
        FiltroBusquedaProfesionales filtro = FiltroBusquedaProfesionales.builder()
                .categoriaId(1L)
                .fecha(UN_LUNES)
                .build();

        assertThat(ids(buscar(filtro))).containsExactly(10L);
    }

    @Test
    void sinResultadosDevuelveUnaPaginaVacia() {
        Pagina<Profesional> pagina = buscar(FiltroBusquedaProfesionales.builder().nombre("inexistente").build());

        assertThat(pagina.getContenido()).isEmpty();
        assertThat(pagina.getTotalElementos()).isZero();
    }

    @Test
    void paginaLosResultados() {
        Pagina<Profesional> pagina = adapter.search(new FiltroBusquedaProfesionales(), 1, 2);

        assertThat(ids(pagina)).containsExactly(12L, 13L);
        assertThat(pagina.getNumero()).isEqualTo(1);
        assertThat(pagina.getTamanio()).isEqualTo(2);
        assertThat(pagina.getTotalElementos()).isEqualTo(4);
        assertThat(pagina.getTotalPaginas()).isEqualTo(2);
    }

    private Pagina<Profesional> buscar(FiltroBusquedaProfesionales filtro) {
        return adapter.search(filtro, 0, 20);
    }

    private static List<Long> ids(Pagina<Profesional> pagina) {
        return pagina.getContenido().stream().map(Profesional::getId).toList();
    }

    private void categoria(Long id, String nombre) {
        jpaCategoriaRepository.save(CategoriaEntity.builder()
                .id(id)
                .nombre(nombre)
                .descripcion(nombre)
                .habilitada(true)
                .creadaEn(AUDITORIA)
                .actualizadaEn(AUDITORIA)
                .build());
    }

    private void profesional(Long id, Long categoriaId, String nombre, String apellido, boolean habilitado) {
        jpaProfesionalRepository.save(ProfesionalEntity.builder()
                .id(id)
                .categoriaId(categoriaId)
                .nombre(nombre)
                .apellido(apellido)
                .habilitado(habilitado)
                .creadoEn(AUDITORIA)
                .actualizadoEn(AUDITORIA)
                .build());
    }

    private void horario(Long id, Long profesionalId, String diaSemana, boolean habilitado) {
        jpaHorarioSemanalRepository.save(HorarioSemanalEntity.builder()
                .id(id)
                .profesionalId(profesionalId)
                .diaSemana(diaSemana)
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(12, 0))
                .duracionTurnoMinutos(30)
                .habilitado(habilitado)
                .creadoEn(AUDITORIA)
                .actualizadoEn(AUDITORIA)
                .build());
    }
}
