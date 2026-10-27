package ar.edu.um.programacion2.catalogo.profesional.application.usecases;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.categoria.domain.ports.out.CategoriaRepository;
import ar.edu.um.programacion2.catalogo.profesional.application.exception.FiltroBusquedaInvalidoException;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.in.BuscarProfesionalesUseCase;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatalogoNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BuscarProfesionalesUseCaseImpl implements BuscarProfesionalesUseCase {

    private final ProfesionalRepository profesionalRepository;
    private final CategoriaRepository categoriaRepository;
    private final VersionAplicadaRepository versionAplicadaRepository;

    @Override
    public Pagina<Profesional> buscarProfesionales(FiltroBusquedaProfesionales filtro, int numeroPagina, int tamanioPagina) {
        // D-04: sin una copia completa no se responde como si el catálogo estuviera vacío.
        if (versionAplicadaRepository.find().isEmpty()) {
            throw new CatalogoNoDisponibleException();
        }
        Long categoriaId = filtro.getCategoriaId();
        if (categoriaId != null && categoriaRepository.findById(categoriaId).isEmpty()) {
            throw new FiltroBusquedaInvalidoException("La categoría " + categoriaId + " no existe");
        }
        return profesionalRepository.search(filtro, numeroPagina, tamanioPagina);
    }
}
