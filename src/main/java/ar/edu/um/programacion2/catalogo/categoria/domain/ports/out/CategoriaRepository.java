package ar.edu.um.programacion2.catalogo.categoria.domain.ports.out;

import java.util.List;
import java.util.Optional;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;

public interface CategoriaRepository {
    Categoria save(Categoria categoria);

    Optional<Categoria> findById(Long id);

    List<Categoria> findAll();
}
