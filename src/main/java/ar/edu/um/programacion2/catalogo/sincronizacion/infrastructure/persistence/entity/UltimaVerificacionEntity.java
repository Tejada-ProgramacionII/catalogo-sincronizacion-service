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
@Table(name = "ultima_verificacion")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UltimaVerificacionEntity {

    public static final Long ID_UNICO = 1L;

    @Id
    private Long id;
    private Instant verificadaEn;
}
