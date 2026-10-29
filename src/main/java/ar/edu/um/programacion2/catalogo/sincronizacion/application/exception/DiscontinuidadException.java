package ar.edu.um.programacion2.catalogo.sincronizacion.application.exception;

// CU-03: no se puede comprobar cómo debería quedar la copia a partir de la versión local.
public class DiscontinuidadException extends RuntimeException {
    public DiscontinuidadException(String mensaje) {
        super(mensaje);
    }
}
