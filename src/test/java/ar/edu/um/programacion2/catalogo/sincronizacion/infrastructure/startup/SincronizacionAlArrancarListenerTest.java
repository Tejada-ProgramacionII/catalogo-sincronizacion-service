package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.startup;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import ar.edu.um.programacion2.catalogo.config.SincronizacionProperties;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.service.SincronizacionService;

@ExtendWith(MockitoExtension.class)
class SincronizacionAlArrancarListenerTest {

    @Mock
    private SincronizacionService sincronizacionService;

    private SincronizacionProperties sincronizacionProperties;

    private SincronizacionAlArrancarListener listener;

    @BeforeEach
    void setUp() {
        sincronizacionProperties = new SincronizacionProperties();
        listener = new SincronizacionAlArrancarListener(sincronizacionService, sincronizacionProperties);
    }

    @Test
    void alArrancarCierraLosIntentosColgadosYDespuesVerifica() {
        listener.sincronizarAlArrancar();

        InOrder orden = inOrder(sincronizacionService);
        orden.verify(sincronizacionService).cerrarIntentosInterrumpidos();
        orden.verify(sincronizacionService).verificar();
    }

    @Test
    void conAlArrancarDesactivadoSoloCierraLosIntentosColgados() {
        sincronizacionProperties.setAlArrancar(false);

        listener.sincronizarAlArrancar();

        verify(sincronizacionService).cerrarIntentosInterrumpidos();
        verify(sincronizacionService, never()).verificar();
    }

    @Test
    void unErrorAlSincronizarNoTumbaLaApp() {
        doThrow(new DataIntegrityViolationException("violación de clave foránea"))
                .when(sincronizacionService).verificar();

        assertThatCode(listener::sincronizarAlArrancar).doesNotThrowAnyException();
    }
}
