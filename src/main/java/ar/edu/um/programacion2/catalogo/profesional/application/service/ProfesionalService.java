package ar.edu.um.programacion2.catalogo.profesional.application.service;

import org.springframework.stereotype.Service;

import ar.edu.um.programacion2.catalogo.profesional.application.exception.ProfesionalNoEncontradoException;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.AgendaProfesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.in.BuscarProfesionalesUseCase;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.in.ConsultarProfesionalUseCase;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfesionalService {

    private final BuscarProfesionalesUseCase buscarProfesionalesUseCase;
    private final ConsultarProfesionalUseCase consultarProfesionalUseCase;

    public Pagina<Profesional> buscarProfesionales(FiltroBusquedaProfesionales filtro, int numeroPagina, int tamanioPagina) {
        return buscarProfesionalesUseCase.buscarProfesionales(filtro, numeroPagina, tamanioPagina);
    }

    public AgendaProfesional consultarProfesional(Long id) {
        return consultarProfesionalUseCase.consultarProfesional(id)
                .orElseThrow(() -> new ProfesionalNoEncontradoException(id));
    }
}
