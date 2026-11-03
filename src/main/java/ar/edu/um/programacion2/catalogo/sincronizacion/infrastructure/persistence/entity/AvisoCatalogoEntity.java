package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity;

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
@Table(name = "avisos_catalogo")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvisoCatalogoEntity {
    @Id
    private String eventId;
    private Long version;
    private Instant procesadoEn;
}
