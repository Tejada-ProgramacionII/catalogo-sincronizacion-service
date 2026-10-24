package ar.edu.um.programacion2.catalogo.profesional.domain.model;

import java.time.Instant;

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
public class Profesional {
    private Long id;
    private Long categoriaId;
    private String nombre;
    private String apellido;
    private boolean habilitado;
    private Instant creadoEn;
    private Instant actualizadoEn;
}
