package ar.edu.um.programacion2.catalogo.profesional.domain.ports.in;

import java.util.Optional;

import ar.edu.um.programacion2.catalogo.profesional.domain.model.AgendaProfesional;

public interface ConsultarProfesionalUseCase {
    Optional<AgendaProfesional> consultarProfesional(Long id);
}
