package ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.entity.CategoriaEntity;

@Repository
public interface JpaCategoriaRepository extends JpaRepository<CategoriaEntity, Long> {
}
