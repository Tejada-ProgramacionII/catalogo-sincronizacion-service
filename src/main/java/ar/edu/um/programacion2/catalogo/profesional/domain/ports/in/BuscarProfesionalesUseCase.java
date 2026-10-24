package ar.edu.um.programacion2.catalogo.profesional.domain.ports.in;

import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;

public interface BuscarProfesionalesUseCase {
    Pagina<Profesional> buscarProfesionales(FiltroBusquedaProfesionales filtro, int numeroPagina, int tamanioPagina);
}
