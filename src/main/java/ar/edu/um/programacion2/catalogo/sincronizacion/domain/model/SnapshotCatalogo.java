package ar.edu.um.programacion2.catalogo.sincronizacion.domain.model;

import java.time.Instant;
import java.util.List;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
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
public class SnapshotCatalogo {
    private Long version;
    private Instant generadoEn;
    private List<Categoria> categorias;
    private List<Profesional> profesionales;
    private List<HorarioSemanal> horarios;
}
