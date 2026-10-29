package ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out;

import java.util.List;
import java.util.Optional;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.CambiosVersion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionesPublicadas;

// Lo que la cátedra publica para la sincronización incremental (contrato, sección 14).
public interface CatalogoPublicadoClient {
    VersionesPublicadas getVersiones();

    // Vacío si la cátedra ya no tiene los cambios de esa versión.
    Optional<CambiosVersion> getCambios(Long version);

    // Devuelven solo los ids publicados: los que faltan no vienen en la lista.
    List<Categoria> getCategorias(List<Long> ids);

    List<Profesional> getProfesionales(List<Long> ids);

    List<HorarioSemanal> getHorarios(List<Long> ids);
}
