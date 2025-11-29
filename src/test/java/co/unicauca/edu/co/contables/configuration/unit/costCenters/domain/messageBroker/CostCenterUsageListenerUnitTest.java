package co.unicauca.edu.co.contables.configuration.unit.costCenters.domain.messageBroker;

import co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.CostCenterUsageListener;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.dto.CostCenterUsageDto;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.enums.EventUsageType;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.IUsageCostCenter;
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
import org.springframework.amqp.core.MessageProperties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CostCenterUsageListenerUnitTest {

    @Mock
    private IUsageCostCenter usageCostCenter;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @InjectMocks
    private CostCenterUsageListener listener;

    private static final Long COST_CENTER_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 5;
    private static final long DELIVERY_TAG = 1L;

    private EventDto<CostCenterUsageDto, EventUsageType> validEvent;
    private CostCenterUsageDto validData;

    @BeforeEach
    void setUp() {
        validData = new CostCenterUsageDto(COST_CENTER_ID, ENTERPRISE_ID, QUANTITY_USED);
        validEvent = new EventDto<>(validData, EventUsageType.USED);

        MessageProperties messageProperties = new MessageProperties();
        when(message.getMessageProperties()).thenReturn(messageProperties);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe procesar evento USED correctamente")
    void testHandleCostCenterEventSuccess() throws Exception {
        // Arrange
        doNothing().when(usageCostCenter).incrementUsageCount(COST_CENTER_ID);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleCostCenterEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(usageCostCenter).incrementUsageCount(COST_CENTER_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe hacer ack cuando el evento es inválido")
    void testHandleCostCenterEventWithInvalidEvent() throws Exception {
        // Arrange
        EventDto<CostCenterUsageDto, EventUsageType> invalidEvent = new EventDto<>(null, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleCostCenterEvent(invalidEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe hacer ack cuando el evento es null")
    void testHandleCostCenterEventWithNullEvent() throws Exception {
        // Arrange
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleCostCenterEvent(null, message, channel, DELIVERY_TAG);

        // Assert
        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe hacer ack cuando el tipo de evento es null")
    void testHandleCostCenterEventWithNullEventType() throws Exception {
        // Arrange
        EventDto<CostCenterUsageDto, EventUsageType> eventWithNullType = new EventDto<>(validData, null);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleCostCenterEvent(eventWithNullType, message, channel, DELIVERY_TAG);

        // Assert
        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe hacer ack cuando costCenterId es null")
    void testHandleCostCenterEventWithNullCostCenterId() throws Exception {
        // Arrange
        CostCenterUsageDto invalidData = new CostCenterUsageDto(null, ENTERPRISE_ID, QUANTITY_USED);
        EventDto<CostCenterUsageDto, EventUsageType> eventWithNullId = new EventDto<>(invalidData, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleCostCenterEvent(eventWithNullId, message, channel, DELIVERY_TAG);

        // Assert
        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe hacer ack cuando quantityUsed es null")
    void testHandleCostCenterEventWithNullQuantityUsed() throws Exception {
        // Arrange
        CostCenterUsageDto invalidData = new CostCenterUsageDto(COST_CENTER_ID, ENTERPRISE_ID, null);
        EventDto<CostCenterUsageDto, EventUsageType> eventWithNullQuantity = new EventDto<>(invalidData, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleCostCenterEvent(eventWithNullQuantity, message, channel, DELIVERY_TAG);

        // Assert
        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe hacer ack cuando quantityUsed es cero")
    void testHandleCostCenterEventWithZeroQuantityUsed() throws Exception {
        // Arrange
        CostCenterUsageDto invalidData = new CostCenterUsageDto(COST_CENTER_ID, ENTERPRISE_ID, 0);
        EventDto<CostCenterUsageDto, EventUsageType> eventWithZeroQuantity = new EventDto<>(invalidData, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleCostCenterEvent(eventWithZeroQuantity, message, channel, DELIVERY_TAG);

        // Assert
        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe hacer ack cuando quantityUsed es negativo")
    void testHandleCostCenterEventWithNegativeQuantityUsed() throws Exception {
        // Arrange
        CostCenterUsageDto invalidData = new CostCenterUsageDto(COST_CENTER_ID, ENTERPRISE_ID, -1);
        EventDto<CostCenterUsageDto, EventUsageType> eventWithNegativeQuantity = new EventDto<>(invalidData, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleCostCenterEvent(eventWithNegativeQuantity, message, channel, DELIVERY_TAG);

        // Assert
        verify(usageCostCenter, never()).incrementUsageCount(anyLong());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe manejar excepción en incrementUsageCount")
    void testHandleCostCenterEventHandlesServiceException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error en servicio")).when(usageCostCenter).incrementUsageCount(COST_CENTER_ID);

        // Act & Assert
        assertDoesNotThrow(() -> listener.handleCostCenterEvent(validEvent, message, channel, DELIVERY_TAG));

        verify(usageCostCenter).incrementUsageCount(COST_CENTER_ID);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe manejar excepción en basicAck")
    void testHandleCostCenterEventHandlesAckException() throws Exception {
        // Arrange
        doNothing().when(usageCostCenter).incrementUsageCount(COST_CENTER_ID);
        doThrow(new RuntimeException("Error en ack")).when(channel).basicAck(DELIVERY_TAG, false);

        // Act & Assert
        assertDoesNotThrow(() -> listener.handleCostCenterEvent(validEvent, message, channel, DELIVERY_TAG));

        verify(usageCostCenter).incrementUsageCount(COST_CENTER_ID);
    }

    @Test
    @DisplayName("handleCostCenterEvent - Debe procesar evento con quantityUsed positivo")
    void testHandleCostCenterEventWithPositiveQuantityUsed() throws Exception {
        // Arrange
        CostCenterUsageDto dataWithHighQuantity = new CostCenterUsageDto(COST_CENTER_ID, ENTERPRISE_ID, 100);
        EventDto<CostCenterUsageDto, EventUsageType> eventWithHighQuantity = new EventDto<>(dataWithHighQuantity, EventUsageType.USED);
        doNothing().when(usageCostCenter).incrementUsageCount(COST_CENTER_ID);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleCostCenterEvent(eventWithHighQuantity, message, channel, DELIVERY_TAG);

        // Assert
        verify(usageCostCenter).incrementUsageCount(COST_CENTER_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }
}
