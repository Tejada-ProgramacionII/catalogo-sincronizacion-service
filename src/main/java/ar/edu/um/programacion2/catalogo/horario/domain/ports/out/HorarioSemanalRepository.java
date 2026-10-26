package ar.edu.um.programacion2.catalogo.horario.domain.ports.out;

import java.util.List;
import java.util.Optional;

import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;

public interface HorarioSemanalRepository {
    HorarioSemanal save(HorarioSemanal horarioSemanal);

    Optional<HorarioSemanal> findById(Long id);

    List<HorarioSemanal> findAll();

    void deleteAll();
}
