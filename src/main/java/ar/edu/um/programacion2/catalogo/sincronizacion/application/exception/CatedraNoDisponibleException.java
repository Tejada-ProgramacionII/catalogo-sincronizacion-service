package ar.edu.um.programacion2.catalogo.sincronizacion.application.exception;

public class CatedraNoDisponibleException extends RuntimeException {
    public CatedraNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
