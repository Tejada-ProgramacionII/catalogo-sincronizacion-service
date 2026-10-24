package ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.entity;

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
@Table(name = "profesionales")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalEntity {
    @Id
    private Long id;
    private Long categoriaId;
    private String nombre;
    private String apellido;
    private boolean habilitado;
    private Instant creadoEn;
    private Instant actualizadoEn;
}
