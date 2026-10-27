package ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.dto;

import java.util.List;

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
public class AgendaProfesionalResponse {
    private Long id;
    private Long categoriaId;
    private String nombre;
    private String apellido;
    private boolean habilitado;
    private List<HorarioSemanalResponse> horarios;
    private Long versionCatalogo;
}
