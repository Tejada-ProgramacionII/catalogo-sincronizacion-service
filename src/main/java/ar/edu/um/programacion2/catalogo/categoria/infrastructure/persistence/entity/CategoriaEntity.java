package ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categorias")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaEntity {
    @Id
    private Long id;
    private String nombre;
    private String descripcion;
    private boolean habilitada;
    private Instant creadaEn;
    private Instant actualizadaEn;
}
