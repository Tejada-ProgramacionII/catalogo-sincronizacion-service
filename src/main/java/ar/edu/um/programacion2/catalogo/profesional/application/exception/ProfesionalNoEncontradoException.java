package ar.edu.um.programacion2.catalogo.profesional.application.exception;

public class ProfesionalNoEncontradoException extends RuntimeException {
    public ProfesionalNoEncontradoException(Long id) {
        super("No existe el profesional " + id);
    }
}
