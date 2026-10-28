package ar.edu.um.programacion2.catalogo.profesional.domain.model;

import java.util.List;

import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendaProfesional {
    private Profesional profesional;
    private List<HorarioSemanal> horarios;
    private Long versionCatalogo;
}
