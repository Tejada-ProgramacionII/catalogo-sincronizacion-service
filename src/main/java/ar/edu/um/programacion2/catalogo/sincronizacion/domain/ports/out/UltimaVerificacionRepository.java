package ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out;

import java.time.Instant;
import java.util.Optional;

public interface UltimaVerificacionRepository {
    Optional<Instant> find();

    void save(Instant verificadaEn);
}
