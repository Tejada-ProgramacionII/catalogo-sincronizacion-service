package ar.edu.um.programacion2.catalogo.profesional.application.service;

import org.springframework.stereotype.Service;

import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.in.BuscarProfesionalesUseCase;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfesionalService {

    private final BuscarProfesionalesUseCase buscarProfesionalesUseCase;

    public Pagina<Profesional> buscarProfesionales(FiltroBusquedaProfesionales filtro, int numeroPagina, int tamanioPagina) {
        return buscarProfesionalesUseCase.buscarProfesionales(filtro, numeroPagina, tamanioPagina);
    }
}
