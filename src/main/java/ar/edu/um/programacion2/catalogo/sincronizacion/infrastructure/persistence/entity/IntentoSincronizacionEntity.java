package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "intentos_sincronizacion")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentoSincronizacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant inicio;
    private Instant fin;
    private String resultado;
    private int intentos;
    private Long versionAplicada;
    private String mensaje;
}
