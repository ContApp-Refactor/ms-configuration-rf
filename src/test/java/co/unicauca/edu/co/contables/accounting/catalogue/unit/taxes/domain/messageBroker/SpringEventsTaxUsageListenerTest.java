package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.domain.messageBroker;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxUsageInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.SpringEventsTaxUsageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.dto.TaxUsageDto;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.enums.EventUsageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpringEventsTaxUsageListenerTest {

    @Mock
    private ITaxUsageInputPort taxUsageInputPort;

    @InjectMocks
    private SpringEventsTaxUsageListener taxUsageListener;

    private EventDto<TaxUsageDto, EventUsageType> validEvent;
    private TaxUsageDto validUsageDto;

    private static final Long TAX_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 1;

    @BeforeEach
    void setUp() {
        validUsageDto = new TaxUsageDto();
        validUsageDto.setTaxId(TAX_ID);
        validUsageDto.setEnterpriseId(ENTERPRISE_ID);
        validUsageDto.setQuantityUsed(QUANTITY_USED);

        validEvent = new EventDto<>();
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);
    }

    @Test
    @DisplayName("Debe procesar evento válido correctamente")
    void testHandleTaxEventWithValidEventProcessesSuccessfully() throws Exception {

        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount con parámetros correctos")
    void testHandleTaxEventInvokesIncrementUsageCountWithCorrectParameters() throws Exception {

        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe rechazar evento nulo")
    void testIsValidEventWithNullEventDoesNotProcessEvent() throws Exception {

        EventDto<TaxUsageDto, EventUsageType> nullEvent = null;

        taxUsageListener.onTaxUsed(null);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con taxId nulo")
    void testIsValidEventWithNullTaxIdDoesNotProcessEvent() throws Exception {

        validUsageDto.setTaxId(null);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId nulo")
    void testIsValidEventWithNullEnterpriseIdDoesNotProcessEvent() throws Exception {

        validUsageDto.setEnterpriseId(null);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId vacío")
    void testIsValidEventWithEmptyEnterpriseIdDoesNotProcessEvent() throws Exception {

        validUsageDto.setEnterpriseId("");

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId en blanco")
    void testIsValidEventWithBlankEnterpriseIdDoesNotProcessEvent() throws Exception {

        validUsageDto.setEnterpriseId("   ");

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed nulo")
    void testIsValidEventWithNullQuantityUsedDoesNotProcessEvent() throws Exception {

        validUsageDto.setQuantityUsed(null);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed cero")
    void testIsValidEventWithZeroQuantityUsedDoesNotProcessEvent() throws Exception {

        validUsageDto.setQuantityUsed(0);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed negativo")
    void testIsValidEventWithNegativeQuantityUsedDoesNotProcessEvent() throws Exception {

        validUsageDto.setQuantityUsed(-1);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe procesar evento USED correctamente")
    void testProcessEventWithUsedTypeProcessesCorrectly() throws Exception {

        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe manejar diferentes taxId correctamente")
    void testHandleTaxEventWithDifferentTaxIds() throws Exception {

        Long differentTaxId = 999L;
        validUsageDto.setTaxId(differentTaxId);
        doNothing().when(taxUsageInputPort).incrementUsageCount(differentTaxId, ENTERPRISE_ID);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort).incrementUsageCount(differentTaxId, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe manejar diferentes enterpriseId correctamente")
    void testHandleTaxEventWithDifferentEnterpriseIds() throws Exception {

        String differentEnterpriseId = "ENT-002";
        validUsageDto.setEnterpriseId(differentEnterpriseId);
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, differentEnterpriseId);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, differentEnterpriseId);
    }

    @Test
    @DisplayName("Debe aceptar evento con quantityUsed mayor a 1")
    void testHandleTaxEventWithQuantityGreaterThanOne() throws Exception {

        validUsageDto.setQuantityUsed(5);
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar evento de impuesto")
    void testOnTaxUsedProcessesTax() throws Exception {

        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe tragar la excepción cuando el input port falla")
    void testHandleTaxEventSwallowsExceptionWhenInputPortFails() throws Exception {

        RuntimeException expectedException = new RuntimeException("Error de servicio");
        doThrow(expectedException).when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        assertDoesNotThrow(() -> taxUsageListener.onTaxUsed(validUsageDto));
        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar múltiples eventos secuencialmente")
    void testHandleMultipleEventsSequentially() throws Exception {

        doNothing().when(taxUsageInputPort).incrementUsageCount(anyLong(), anyString());
        EventDto<TaxUsageDto, EventUsageType> event1 = createValidEvent(1L, "ENT-001");
        EventDto<TaxUsageDto, EventUsageType> event2 = createValidEvent(2L, "ENT-002");

        taxUsageListener.onTaxUsed(event1.getData());
        taxUsageListener.onTaxUsed(event2.getData());

        verify(taxUsageInputPort).incrementUsageCount(1L, "ENT-001");
        verify(taxUsageInputPort).incrementUsageCount(2L, "ENT-002");
    }

    @Test
    @DisplayName("Debe manejar taxId con valor máximo de Long")
    void testHandleTaxEventWithMaxLongTaxId() throws Exception {

        Long maxTaxId = Long.MAX_VALUE;
        validUsageDto.setTaxId(maxTaxId);
        doNothing().when(taxUsageInputPort).incrementUsageCount(maxTaxId, ENTERPRISE_ID);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort).incrementUsageCount(maxTaxId, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe manejar enterpriseId con caracteres especiales")
    void testHandleTaxEventWithSpecialCharactersInEnterpriseId() throws Exception {

        String specialEnterpriseId = "ENT-001_TEST-SPECIAL";
        validUsageDto.setEnterpriseId(specialEnterpriseId);
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, specialEnterpriseId);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, specialEnterpriseId);
    }

    @Test
    @DisplayName("Debe retornar sin procesar cuando taxId es null - cubre branch isValidEvent en case USED")
    void testProcessEventReturnsEarlyWhenTaxIdIsNullInUsedCase() throws Exception {

        validUsageDto.setTaxId(null);
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe retornar sin procesar cuando enterpriseId es vacío - cubre branch isValidEvent en case USED")
    void testProcessEventReturnsEarlyWhenEnterpriseIdIsEmptyInUsedCase() throws Exception {

        validUsageDto.setEnterpriseId("");
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe retornar sin procesar cuando quantityUsed es cero - cubre branch isValidEvent en case USED")
    void testProcessEventReturnsEarlyWhenQuantityUsedIsZeroInUsedCase() throws Exception {

        validUsageDto.setQuantityUsed(0);
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe retornar sin procesar cuando quantityUsed es negativo - cubre branch isValidEvent en case USED")
    void testProcessEventReturnsEarlyWhenQuantityUsedIsNegativeInUsedCase() throws Exception {

        validUsageDto.setQuantityUsed(-5);
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);

        taxUsageListener.onTaxUsed(validUsageDto);

        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
    }

    private EventDto<TaxUsageDto, EventUsageType> createValidEvent(Long taxId, String enterpriseId) {
        TaxUsageDto dto = new TaxUsageDto();
        dto.setTaxId(taxId);
        dto.setEnterpriseId(enterpriseId);
        dto.setQuantityUsed(1);

        EventDto<TaxUsageDto, EventUsageType> event = new EventDto<>();
        event.setData(dto);
        event.setType(EventUsageType.USED);
        return event;
    }
}
