package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.startup;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void alArrancarPideSincronizarSiNoHayCopia() {
        listener.sincronizarAlArrancar();

        verify(sincronizacionService).sincronizarSiNoHayCopia();
    }

    @Test
    void conAlArrancarDesactivadoNoSincroniza() {
        sincronizacionProperties.setAlArrancar(false);

        listener.sincronizarAlArrancar();

        verifyNoInteractions(sincronizacionService);
    }

    @Test
    void unErrorAlSincronizarNoTumbaLaApp() {
        doThrow(new DataIntegrityViolationException("violación de clave foránea"))
                .when(sincronizacionService).sincronizarSiNoHayCopia();

        assertThatCode(listener::sincronizarAlArrancar).doesNotThrowAnyException();
    }
}
