package co.unicauca.edu.co.contables.configuration.unit.costCenters.domain.messageBroker;

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

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.CostCenterUsedDto;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.SpringEventsCostCenterUsageListener;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.IUsageCostCenter;

@ExtendWith(MockitoExtension.class)
class SpringEventsCostCenterUsageListenerTest {

    private static final Long COST_CENTER_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 5;

    @Mock
    private IUsageCostCenter usageCostCenter;

    @InjectMocks
    private SpringEventsCostCenterUsageListener listener;

    private CostCenterUsedDto validEvent;

    @BeforeEach
    void setUp() {
        validEvent = new CostCenterUsedDto(COST_CENTER_ID, ENTERPRISE_ID, QUANTITY_USED);
    }

    @Test
    @DisplayName("Evento válido - incrementa el uso del centro de costo")
    void eventoValido_incrementaUso() {
        listener.onCostCenterUsed(validEvent);

        verify(usageCostCenter).incrementUsageCount(COST_CENTER_ID);
    }

    @Test
    @DisplayName("Evento válido - incrementa exactamente una vez")
    void eventoValido_incrementaUnaVez() {
        listener.onCostCenterUsed(validEvent);

        verify(usageCostCenter, times(1)).incrementUsageCount(COST_CENTER_ID);
    }

    @Test
    @DisplayName("Evento nulo - no procesa y no lanza")
    void eventoNulo_noProcesa() {
        assertDoesNotThrow(() -> listener.onCostCenterUsed(null));

        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("costCenterId nulo - no procesa")
    void costCenterIdNulo_noProcesa() {
        validEvent.setCostCenterId(null);

        listener.onCostCenterUsed(validEvent);

        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("quantityUsed nulo - no procesa")
    void quantityUsedNulo_noProcesa() {
        validEvent.setQuantityUsed(null);

        listener.onCostCenterUsed(validEvent);

        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("quantityUsed cero o negativo - no procesa")
    void quantityUsedNoPositivo_noProcesa() {
        validEvent.setQuantityUsed(0);
        listener.onCostCenterUsed(validEvent);

        validEvent.setQuantityUsed(-1);
        listener.onCostCenterUsed(validEvent);

        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("Fallo del caso de uso - se registra y no se propaga")
    void falloDelCasoDeUso_noPropaga() {
        doThrow(new RuntimeException("Error en servicio"))
                .when(usageCostCenter).incrementUsageCount(COST_CENTER_ID);

        assertDoesNotThrow(() -> listener.onCostCenterUsed(validEvent));

        verify(usageCostCenter).incrementUsageCount(COST_CENTER_ID);
    }

    @Test
    @DisplayName("Eventos con distintos IDs - cada uno incrementa su propio contador")
    void eventosDistintos_incrementanCadaContador() {
        listener.onCostCenterUsed(new CostCenterUsedDto(1L, ENTERPRISE_ID, 1));
        listener.onCostCenterUsed(new CostCenterUsedDto(2L, ENTERPRISE_ID, 3));

        verify(usageCostCenter).incrementUsageCount(1L);
        verify(usageCostCenter).incrementUsageCount(2L);
    }

    @Test
    @DisplayName("Evento válido completo - quantityUsed positivo procesa")
    void quantityUsedPositivo_procesa() {
        validEvent.setQuantityUsed(100);

        listener.onCostCenterUsed(validEvent);

        verify(usageCostCenter).incrementUsageCount(COST_CENTER_ID);
    }
}
