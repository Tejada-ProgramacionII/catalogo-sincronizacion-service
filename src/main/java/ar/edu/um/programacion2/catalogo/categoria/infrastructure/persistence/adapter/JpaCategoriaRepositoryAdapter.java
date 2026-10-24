package ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.categoria.domain.ports.out.CategoriaRepository;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.entity.CategoriaEntity;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.mapper.CategoriaMapper;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.repository.JpaCategoriaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaCategoriaRepositoryAdapter implements CategoriaRepository {

    private final JpaCategoriaRepository jpaCategoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    public Categoria save(Categoria categoria) {
        CategoriaEntity guardada = jpaCategoriaRepository.save(categoriaMapper.toEntity(categoria));
        return categoriaMapper.toDomainModel(guardada);
    }

    @Override
    public Optional<Categoria> findById(Long id) {
        return jpaCategoriaRepository.findById(id).map(categoriaMapper::toDomainModel);
    }

    @Override
    public List<Categoria> findAll() {
        return jpaCategoriaRepository.findAll().stream()
                .map(categoriaMapper::toDomainModel)
                .collect(Collectors.toList());
    }
}
