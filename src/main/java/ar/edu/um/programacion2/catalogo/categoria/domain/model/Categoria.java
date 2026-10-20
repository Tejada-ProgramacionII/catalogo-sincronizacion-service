package ar.edu.um.programacion2.catalogo.categoria.domain.model;

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
public class Categoria {
    private Long id;
    private String nombre;
    private String descripcion;
    private boolean habilitada;
    private Instant creadaEn;
    private Instant actualizadaEn;
}
