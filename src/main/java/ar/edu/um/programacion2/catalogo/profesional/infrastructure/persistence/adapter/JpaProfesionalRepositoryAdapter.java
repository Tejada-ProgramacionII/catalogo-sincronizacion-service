package ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.entity.ProfesionalEntity;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.mapper.ProfesionalMapper;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.repository.JpaProfesionalRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaProfesionalRepositoryAdapter implements ProfesionalRepository {

    // El id desempata para que el orden entre páginas sea estable.
    private static final Sort ORDEN_BUSQUEDA = Sort.by("apellido", "nombre", "id");

    private final JpaProfesionalRepository jpaProfesionalRepository;
    private final ProfesionalMapper profesionalMapper;

    @Override
    public Profesional save(Profesional profesional) {
        ProfesionalEntity guardado = jpaProfesionalRepository.save(profesionalMapper.toEntity(profesional));
        return profesionalMapper.toDomainModel(guardado);
    }

    @Override
    public Optional<Profesional> findById(Long id) {
        return jpaProfesionalRepository.findById(id).map(profesionalMapper::toDomainModel);
    }

    @Override
    public List<Profesional> findAll() {
        return jpaProfesionalRepository.findAll().stream()
                .map(profesionalMapper::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Pagina<Profesional> search(FiltroBusquedaProfesionales filtro, int numeroPagina, int tamanioPagina) {
        Page<ProfesionalEntity> pagina = jpaProfesionalRepository.buscar(
                filtro.getCategoriaId(),
                filtro.getHabilitado(),
                patronNombre(filtro.getNombre()),
                filtro.getDiaSemana() == null ? null : filtro.getDiaSemana().name(),
                PageRequest.of(numeroPagina, tamanioPagina, ORDEN_BUSQUEDA));
        return Pagina.<Profesional>builder()
                .contenido(pagina.getContent().stream().map(profesionalMapper::toDomainModel).toList())
                .numero(pagina.getNumber())
                .tamanio(pagina.getSize())
                .totalElementos(pagina.getTotalElements())
                .totalPaginas(pagina.getTotalPages())
                .build();
    }

    private static String patronNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return "%" + nombre.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
