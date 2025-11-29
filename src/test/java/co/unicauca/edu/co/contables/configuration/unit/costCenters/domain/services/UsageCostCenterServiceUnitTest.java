package co.unicauca.edu.co.contables.configuration.unit.costCenters.domain.services;

import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.ICostCenterService;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.UsageCostCenterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsageCostCenterServiceUnitTest {

    @Mock
    private ICostCenterService costCenterService;

    @InjectMocks
    private UsageCostCenterService service;

    private static final Long COST_CENTER_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String CODE = "1001";
    private static final String NAME = "Centro de costo";

    private CostCenter costCenter;

    @BeforeEach
    void setUp() {
        costCenter = CostCenter.builder()
                .id(COST_CENTER_ID)
                .idEnterprise(ENTERPRISE_ID)
                .code(CODE)
                .name(NAME)
                .status(true)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("incrementUsageCount - Debe incrementar contador de uso cuando centro existe con usageCount 0")
    void testIncrementUsageCountFromZero() {
        when(costCenterService.findById(COST_CENTER_ID)).thenReturn(costCenter);
        doNothing().when(costCenterService).updateUsageCount(COST_CENTER_ID, 1);

        service.incrementUsageCount(COST_CENTER_ID);

        verify(costCenterService).findById(COST_CENTER_ID);
        verify(costCenterService).updateUsageCount(COST_CENTER_ID, 1);
    }

    @Test
    @DisplayName("incrementUsageCount - Debe incrementar contador de uso cuando ya tiene valor")
    void testIncrementUsageCountFromExistingValue() {
        costCenter.setUsageCount(5);
        when(costCenterService.findById(COST_CENTER_ID)).thenReturn(costCenter);
        doNothing().when(costCenterService).updateUsageCount(COST_CENTER_ID, 6);

        service.incrementUsageCount(COST_CENTER_ID);

        verify(costCenterService).findById(COST_CENTER_ID);
        verify(costCenterService).updateUsageCount(COST_CENTER_ID, 6);
    }

    @Test
    @DisplayName("incrementUsageCount - Debe incrementar a 1 cuando usageCount es null")
    void testIncrementUsageCountWhenUsageCountIsNull() {
        costCenter.setUsageCount(null);
        when(costCenterService.findById(COST_CENTER_ID)).thenReturn(costCenter);
        doNothing().when(costCenterService).updateUsageCount(COST_CENTER_ID, 1);

        service.incrementUsageCount(COST_CENTER_ID);

        verify(costCenterService).findById(COST_CENTER_ID);
        verify(costCenterService).updateUsageCount(COST_CENTER_ID, 1);
    }

    @Test
    @DisplayName("incrementUsageCount - No debe actualizar cuando centro de costo no existe")
    void testIncrementUsageCountWhenCostCenterNotFound() {
        when(costCenterService.findById(COST_CENTER_ID)).thenReturn(null);

        service.incrementUsageCount(COST_CENTER_ID);

        verify(costCenterService).findById(COST_CENTER_ID);
        verify(costCenterService, never()).updateUsageCount(anyLong(), anyInt());
    }

    @Test
    @DisplayName("incrementUsageCount - Debe manejar excepción en findById sin propagar")
    void testIncrementUsageCountHandlesExceptionInFindById() {
        when(costCenterService.findById(COST_CENTER_ID)).thenThrow(new RuntimeException("Error en búsqueda"));

        service.incrementUsageCount(COST_CENTER_ID);

        verify(costCenterService).findById(COST_CENTER_ID);
        verify(costCenterService, never()).updateUsageCount(anyLong(), anyInt());
    }

    @Test
    @DisplayName("incrementUsageCount - Debe manejar excepción en updateUsageCount sin propagar")
    void testIncrementUsageCountHandlesExceptionInUpdate() {
        when(costCenterService.findById(COST_CENTER_ID)).thenReturn(costCenter);
        doThrow(new RuntimeException("Error en actualización")).when(costCenterService).updateUsageCount(COST_CENTER_ID, 1);

        service.incrementUsageCount(COST_CENTER_ID);

        verify(costCenterService).findById(COST_CENTER_ID);
        verify(costCenterService).updateUsageCount(COST_CENTER_ID, 1);
    }

    @Test
    @DisplayName("incrementUsageCount - Debe incrementar correctamente con valor alto de usageCount")
    void testIncrementUsageCountWithHighValue() {
        costCenter.setUsageCount(999);
        when(costCenterService.findById(COST_CENTER_ID)).thenReturn(costCenter);
        doNothing().when(costCenterService).updateUsageCount(COST_CENTER_ID, 1000);

        service.incrementUsageCount(COST_CENTER_ID);

        verify(costCenterService).updateUsageCount(COST_CENTER_ID, 1000);
    }

    @Test
    @DisplayName("incrementUsageCount - Debe llamar findById con el ID correcto")
    void testIncrementUsageCountCallsFindByIdWithCorrectId() {
        Long specificId = 123L;
        CostCenter specificCostCenter = CostCenter.builder()
                .id(specificId)
                .idEnterprise(ENTERPRISE_ID)
                .code("2001")
                .name("Centro específico")
                .status(true)
                .usageCount(10)
                .build();
        when(costCenterService.findById(specificId)).thenReturn(specificCostCenter);
        doNothing().when(costCenterService).updateUsageCount(specificId, 11);

        service.incrementUsageCount(specificId);

        verify(costCenterService).findById(specificId);
        verify(costCenterService).updateUsageCount(specificId, 11);
    }
}
