package ar.edu.um.programacion2.catalogo.sincronizacion.application.exception;

public class CatalogoNoDisponibleException extends RuntimeException {
    public CatalogoNoDisponibleException() {
        super("El catálogo todavía no está disponible: no hay una sincronización completa aplicada");
    }
}
