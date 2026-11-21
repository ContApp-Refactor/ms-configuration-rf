package co.unicauca.edu.co.contables.configuration.unit.thirds.infrastructure.adapters.output.persistence.messageBroker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.ThirdUsedEvenDto;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.IThirdUsagePort;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.SpringEventsThirdUsageListener;

@ExtendWith(MockitoExtension.class)
class SpringEventsThirdUsageListenerTest {

    private static final Long THIRD_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 5;

    @Mock
    private IThirdUsagePort thirdUsagePort;

    @InjectMocks
    private SpringEventsThirdUsageListener listener;

    private ThirdUsedEvenDto validEvent;

    @BeforeEach
    void setUp() {
        validEvent = new ThirdUsedEvenDto(THIRD_ID, ENTERPRISE_ID, QUANTITY_USED);
    }

    @Test
    @DisplayName("Evento válido - incrementa el uso del tercero")
    void eventoValido_incrementaUso() {
        listener.onThirdUsed(validEvent);

        verify(thirdUsagePort).incrementUsageCount(THIRD_ID);
    }

    @Test
    @DisplayName("Evento válido - incrementa exactamente una vez")
    void eventoValido_incrementaUnaVez() {
        listener.onThirdUsed(validEvent);

        verify(thirdUsagePort, times(1)).incrementUsageCount(THIRD_ID);
    }

    @Test
    @DisplayName("Evento nulo - no procesa y no lanza")
    void eventoNulo_noProcesa() {
        assertDoesNotThrow(() -> listener.onThirdUsed(null));

        verify(thirdUsagePort, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("thirdId nulo - no procesa")
    void thirdIdNulo_noProcesa() {
        validEvent.setThirdId(null);

        listener.onThirdUsed(validEvent);

        verify(thirdUsagePort, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("quantityUsed nulo - no procesa")
    void quantityUsedNulo_noProcesa() {
        validEvent.setQuantityUsed(null);

        listener.onThirdUsed(validEvent);

        verify(thirdUsagePort, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("quantityUsed cero o negativo - no procesa")
    void quantityUsedNoPositivo_noProcesa() {
        validEvent.setQuantityUsed(0);
        listener.onThirdUsed(validEvent);

        validEvent.setQuantityUsed(-1);
        listener.onThirdUsed(validEvent);

        verify(thirdUsagePort, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("Fallo del caso de uso - se registra y no se propaga")
    void falloDelCasoDeUso_noPropaga() {
        doThrow(new RuntimeException("Error en servicio"))
                .when(thirdUsagePort).incrementUsageCount(THIRD_ID);

        assertDoesNotThrow(() -> listener.onThirdUsed(validEvent));

        verify(thirdUsagePort).incrementUsageCount(THIRD_ID);
    }

    @Test
    @DisplayName("Eventos con distintos IDs - cada uno incrementa su propio contador")
    void eventosDistintos_incrementanCadaContador() {
        listener.onThirdUsed(new ThirdUsedEvenDto(1L, ENTERPRISE_ID, 1));
        listener.onThirdUsed(new ThirdUsedEvenDto(2L, ENTERPRISE_ID, 3));

        verify(thirdUsagePort).incrementUsageCount(1L);
        verify(thirdUsagePort).incrementUsageCount(2L);
    }

    @Test
    @DisplayName("quantityUsed positivo - procesa")
    void quantityUsedPositivo_procesa() {
        validEvent.setQuantityUsed(100);

        listener.onThirdUsed(validEvent);

        verify(thirdUsagePort).incrementUsageCount(THIRD_ID);
    }
}
