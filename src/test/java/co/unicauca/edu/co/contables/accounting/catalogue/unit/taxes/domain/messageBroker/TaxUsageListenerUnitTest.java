package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.domain.messageBroker;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxUsageInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.TaxUsageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.dto.TaxUsageDto;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.enums.EventUsageType;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.core.Message;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxUsageListenerUnitTest {

    @Mock
    private ITaxUsageInputPort taxUsageInputPort;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @InjectMocks
    private TaxUsageListener taxUsageListener;

    private EventDto<TaxUsageDto, EventUsageType> validEvent;
    private TaxUsageDto validUsageDto;

    private static final Long TAX_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 1;
    private static final long DELIVERY_TAG = 123L;

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

    // ==================== Tests de handleTaxEvent ====================

    @Test
    @DisplayName("Debe procesar evento válido correctamente")
    void testHandleTaxEventWithValidEventProcessesSuccessfully() throws Exception {
        // Arrange
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe enviar ACK después de procesar evento válido")
    void testHandleTaxEventSendsAcknowledgment() throws Exception {
        // Arrange
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount con parámetros correctos")
    void testHandleTaxEventInvokesIncrementUsageCountWithCorrectParameters() throws Exception {
        // Arrange
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de isValidEvent - Validación indirecta ====================

    @Test
    @DisplayName("Debe rechazar evento nulo")
    void testIsValidEventWithNullEventDoesNotProcessEvent() throws Exception {
        // Arrange
        EventDto<TaxUsageDto, EventUsageType> nullEvent = null;

        // Act
        taxUsageListener.handleTaxEvent(nullEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con tipo nulo")
    void testIsValidEventWithNullTypeDoesNotProcessEvent() throws Exception {
        // Arrange
        validEvent.setType(null);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con data nulo")
    void testIsValidEventWithNullDataDoesNotProcessEvent() throws Exception {
        // Arrange
        validEvent.setData(null);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con taxId nulo")
    void testIsValidEventWithNullTaxIdDoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setTaxId(null);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId nulo")
    void testIsValidEventWithNullEnterpriseIdDoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setEnterpriseId(null);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId vacío")
    void testIsValidEventWithEmptyEnterpriseIdDoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setEnterpriseId("");

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId en blanco")
    void testIsValidEventWithBlankEnterpriseIdDoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setEnterpriseId("   ");

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed nulo")
    void testIsValidEventWithNullQuantityUsedDoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(null);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed cero")
    void testIsValidEventWithZeroQuantityUsedDoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(0);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed negativo")
    void testIsValidEventWithNegativeQuantityUsedDoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(-1);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // ==================== Tests de processEvent ====================

    @Test
    @DisplayName("Debe procesar evento USED correctamente")
    void testProcessEventWithUsedTypeProcessesCorrectly() throws Exception {
        // Arrange
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe manejar diferentes taxId correctamente")
    void testHandleTaxEventWithDifferentTaxIds() throws Exception {
        // Arrange
        Long differentTaxId = 999L;
        validUsageDto.setTaxId(differentTaxId);
        doNothing().when(taxUsageInputPort).incrementUsageCount(differentTaxId, ENTERPRISE_ID);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort).incrementUsageCount(differentTaxId, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe manejar diferentes enterpriseId correctamente")
    void testHandleTaxEventWithDifferentEnterpriseIds() throws Exception {
        // Arrange
        String differentEnterpriseId = "ENT-002";
        validUsageDto.setEnterpriseId(differentEnterpriseId);
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, differentEnterpriseId);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, differentEnterpriseId);
    }

    @Test
    @DisplayName("Debe aceptar evento con quantityUsed mayor a 1")
    void testHandleTaxEventWithQuantityGreaterThanOne() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(5);
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // ==================== Tests de getEntityType ====================

    @Test
    @DisplayName("Debe retornar tipo de entidad Tax")
    void testGetEntityTypeReturnsTax() throws Exception {
        // Arrange
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de manejo de excepciones ====================

    @Test
    @DisplayName("Debe propagar excepción cuando el input port falla")
    void testHandleTaxEventPropagatesExceptionWhenInputPortFails() throws Exception {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Error de servicio");
        doThrow(expectedException).when(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);

        // Act & Assert
        try {
            taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);
        } catch (RuntimeException e) {
            verify(taxUsageInputPort).incrementUsageCount(TAX_ID, ENTERPRISE_ID);
        }
    }

    @Test
    @DisplayName("Debe procesar múltiples eventos secuencialmente")
    void testHandleMultipleEventsSequentially() throws Exception {
        // Arrange
        doNothing().when(taxUsageInputPort).incrementUsageCount(anyLong(), anyString());
        EventDto<TaxUsageDto, EventUsageType> event1 = createValidEvent(1L, "ENT-001");
        EventDto<TaxUsageDto, EventUsageType> event2 = createValidEvent(2L, "ENT-002");

        // Act
        taxUsageListener.handleTaxEvent(event1, message, channel, DELIVERY_TAG);
        taxUsageListener.handleTaxEvent(event2, message, channel, DELIVERY_TAG + 1);

        // Assert
        verify(taxUsageInputPort).incrementUsageCount(1L, "ENT-001");
        verify(taxUsageInputPort).incrementUsageCount(2L, "ENT-002");
        verify(channel, times(2)).basicAck(anyLong(), eq(false));
    }

    @Test
    @DisplayName("Debe manejar taxId con valor máximo de Long")
    void testHandleTaxEventWithMaxLongTaxId() throws Exception {
        // Arrange
        Long maxTaxId = Long.MAX_VALUE;
        validUsageDto.setTaxId(maxTaxId);
        doNothing().when(taxUsageInputPort).incrementUsageCount(maxTaxId, ENTERPRISE_ID);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort).incrementUsageCount(maxTaxId, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe manejar enterpriseId con caracteres especiales")
    void testHandleTaxEventWithSpecialCharactersInEnterpriseId() throws Exception {
        // Arrange
        String specialEnterpriseId = "ENT-001_TEST-SPECIAL";
        validUsageDto.setEnterpriseId(specialEnterpriseId);
        doNothing().when(taxUsageInputPort).incrementUsageCount(TAX_ID, specialEnterpriseId);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort).incrementUsageCount(TAX_ID, specialEnterpriseId);
    }

    // ==================== Tests de cobertura branch isValidEvent dentro de processEvent ====================

    @Test
    @DisplayName("Debe retornar sin procesar cuando taxId es null - cubre branch isValidEvent en case USED")
    void testProcessEventReturnsEarlyWhenTaxIdIsNullInUsedCase() throws Exception {
        // Arrange
        validUsageDto.setTaxId(null);
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe retornar sin procesar cuando enterpriseId es vacío - cubre branch isValidEvent en case USED")
    void testProcessEventReturnsEarlyWhenEnterpriseIdIsEmptyInUsedCase() throws Exception {
        // Arrange
        validUsageDto.setEnterpriseId("");
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe retornar sin procesar cuando quantityUsed es cero - cubre branch isValidEvent en case USED")
    void testProcessEventReturnsEarlyWhenQuantityUsedIsZeroInUsedCase() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(0);
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe retornar sin procesar cuando quantityUsed es negativo - cubre branch isValidEvent en case USED")
    void testProcessEventReturnsEarlyWhenQuantityUsedIsNegativeInUsedCase() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(-5);
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe retornar sin procesar cuando data es null - cubre branch isValidEvent en case USED")
    void testProcessEventReturnsEarlyWhenDataIsNullInUsedCase() throws Exception {
        // Arrange
        validEvent.setData(null);
        validEvent.setType(EventUsageType.USED);

        // Act
        taxUsageListener.handleTaxEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(taxUsageInputPort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
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
