package ar.edu.um.programacion2.catalogo.profesional.domain.ports.out;

import java.util.List;
import java.util.Optional;

import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;

public interface ProfesionalRepository {
    Profesional save(Profesional profesional);

    Optional<Profesional> findById(Long id);

    List<Profesional> findAll();

    Pagina<Profesional> search(FiltroBusquedaProfesionales filtro, int numeroPagina, int tamanioPagina);
}
