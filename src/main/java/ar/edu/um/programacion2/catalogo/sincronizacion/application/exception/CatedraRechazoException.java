package ar.edu.um.programacion2.catalogo.sincronizacion.application.exception;

public class CatedraRechazoException extends RuntimeException {
    public CatedraRechazoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
