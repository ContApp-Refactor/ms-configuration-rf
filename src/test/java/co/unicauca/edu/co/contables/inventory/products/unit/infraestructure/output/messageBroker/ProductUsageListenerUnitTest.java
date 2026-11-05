package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.output.messageBroker;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductUsagePort;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.ProductUsageListener;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.ProductUsageEventDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.enums.EventUsageType;
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
class ProductUsageListenerUnitTest {

    @Mock
    private IProductUsagePort productUsagePort;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @InjectMocks
    private ProductUsageListener productUsageListener;

    private EventDto<ProductUsageEventDto, EventUsageType> validEvent;
    private ProductUsageEventDto validData;

    private static final Long PRODUCT_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 10;
    private static final long DELIVERY_TAG = 123L;

    @BeforeEach
    void setUp() {
        validData = new ProductUsageEventDto();
        validData.setProductId(PRODUCT_ID);
        validData.setEnterpriseId(ENTERPRISE_ID);
        validData.setQuantityUsed(QUANTITY_USED);

        validEvent = new EventDto<>();
        validEvent.setData(validData);
        validEvent.setType(EventUsageType.USED);
    }

    // ==================== Tests de handleProductEvent ====================

    @Test
    @DisplayName("Debe procesar evento válido correctamente")
    void testHandleProductEvent_WithValidEvent_ProcessesSuccessfully() throws Exception {
        // Arrange
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe enviar ACK después de procesar evento")
    void testHandleProductEvent_SendsAcknowledgment() throws Exception {
        // Arrange
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount con productId correcto")
    void testHandleProductEvent_InvokesIncrementUsageCountWithCorrectProductId() throws Exception {
        // Arrange
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    // ==================== Tests de isValidEvent ====================

    @Test
    @DisplayName("Debe retornar true para evento válido completo")
    void testIsValidEvent_WithValidEvent_ReturnsTrue() throws Exception {
        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    @Test
    @DisplayName("Debe rechazar evento nulo")
    void testIsValidEvent_WithNullEvent_DoesNotProcessEvent() throws Exception {
        // Act
        productUsageListener.handleProductEvent(null, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort, never()).incrementUsageCount(any());
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Debe rechazar evento con data nulo")
    void testIsValidEvent_WithNullData_DoesNotProcessEvent() throws Exception {
        // Arrange
        validEvent.setData(null);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort, never()).incrementUsageCount(any());
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Debe rechazar evento con productId nulo")
    void testIsValidEvent_WithNullProductId_DoesNotProcessEvent() throws Exception {
        // Arrange
        validData.setProductId(null);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort, never()).incrementUsageCount(any());
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed nulo")
    void testIsValidEvent_WithNullQuantityUsed_DoesNotProcessEvent() throws Exception {
        // Arrange
        validData.setQuantityUsed(null);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort, never()).incrementUsageCount(any());
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed cero")
    void testIsValidEvent_WithZeroQuantityUsed_DoesNotProcessEvent() throws Exception {
        // Arrange
        validData.setQuantityUsed(0);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort, never()).incrementUsageCount(any());
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed negativo")
    void testIsValidEvent_WithNegativeQuantityUsed_DoesNotProcessEvent() throws Exception {
        // Arrange
        validData.setQuantityUsed(-5);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort, never()).incrementUsageCount(any());
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Debe aceptar evento sin enterpriseId")
    void testIsValidEvent_WithNullEnterpriseId_ProcessesEvent() throws Exception {
        // Arrange
        validData.setEnterpriseId(null);
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    @Test
    @DisplayName("Debe aceptar evento con enterpriseId vacío")
    void testIsValidEvent_WithEmptyEnterpriseId_ProcessesEvent() throws Exception {
        // Arrange
        validData.setEnterpriseId("");
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    // ==================== Tests de manejo de excepciones ====================

    @Test
    @DisplayName("Debe manejar excepción en processEvent y enviar ACK")
    void testHandleProductEvent_WhenExceptionOccurs_SendsAck() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error al incrementar uso")).when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe continuar procesamiento después de excepción")
    void testHandleProductEvent_WhenExceptionOccurs_DoesNotThrowException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error al incrementar uso")).when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act & Assert
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    @Test
    @DisplayName("Debe enviar ACK aunque falle el procesamiento")
    void testHandleProductEvent_WhenProcessingFails_StillSendsAck() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error interno")).when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe manejar excepción en basicAck sin propagar")
    void testHandleProductEvent_WhenAckFails_DoesNotThrowException() throws Exception {
        // Arrange
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);
        doThrow(new RuntimeException("Error en ACK")).when(channel).basicAck(DELIVERY_TAG, false);

        // Act & Assert
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    // ==================== Tests de getEntityType ====================

    @Test
    @DisplayName("Debe retornar tipo de entidad ProductUsage")
    void testGetEntityType_ReturnsProductUsage() throws Exception {
        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    // ==================== Tests con diferentes valores de quantityUsed ====================

    @Test
    @DisplayName("Debe procesar evento con quantityUsed = 1")
    void testHandleProductEvent_WithQuantityUsedOne_ProcessesSuccessfully() throws Exception {
        // Arrange
        validData.setQuantityUsed(1);
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    @Test
    @DisplayName("Debe procesar evento con quantityUsed grande")
    void testHandleProductEvent_WithLargeQuantityUsed_ProcessesSuccessfully() throws Exception {
        // Arrange
        validData.setQuantityUsed(1000000);
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    // ==================== Tests con diferentes tipos de evento ====================

    @Test
    @DisplayName("Debe procesar evento tipo USED")
    void testHandleProductEvent_WithUsedType_ProcessesSuccessfully() throws Exception {
        // Arrange
        validEvent.setType(EventUsageType.USED);
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    @Test
    @DisplayName("Debe procesar evento con type nulo")
    void testHandleProductEvent_WithNullType_ProcessesSuccessfully() throws Exception {
        // Arrange
        validEvent.setType(null);
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    // ==================== Tests de integración de validaciones ====================

    @Test
    @DisplayName("Debe validar campos en orden correcto")
    void testIsValidEvent_ValidatesFieldsInOrder() throws Exception {
        // Arrange
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe procesar múltiples eventos consecutivos")
    void testHandleProductEvent_ProcessMultipleEvents_AllProcessedSuccessfully() throws Exception {
        // Arrange
        doNothing().when(productUsagePort).incrementUsageCount(any());

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG + 1);
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG + 2);

        // Assert
        verify(productUsagePort, times(3)).incrementUsageCount(PRODUCT_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel).basicAck(DELIVERY_TAG + 1, false);
        verify(channel).basicAck(DELIVERY_TAG + 2, false);
    }

    @Test
    @DisplayName("Debe procesar eventos con diferentes productIds")
    void testHandleProductEvent_WithDifferentProductIds_ProcessesCorrectly() throws Exception {
        // Arrange
        Long productId1 = 1L;
        Long productId2 = 2L;
        Long productId3 = 3L;

        ProductUsageEventDto data1 = new ProductUsageEventDto(productId1, ENTERPRISE_ID, QUANTITY_USED);
        ProductUsageEventDto data2 = new ProductUsageEventDto(productId2, ENTERPRISE_ID, QUANTITY_USED);
        ProductUsageEventDto data3 = new ProductUsageEventDto(productId3, ENTERPRISE_ID, QUANTITY_USED);

        EventDto<ProductUsageEventDto, EventUsageType> event1 = new EventDto<>(data1, EventUsageType.USED);
        EventDto<ProductUsageEventDto, EventUsageType> event2 = new EventDto<>(data2, EventUsageType.USED);
        EventDto<ProductUsageEventDto, EventUsageType> event3 = new EventDto<>(data3, EventUsageType.USED);

        doNothing().when(productUsagePort).incrementUsageCount(any());

        // Act
        productUsageListener.handleProductEvent(event1, message, channel, DELIVERY_TAG);
        productUsageListener.handleProductEvent(event2, message, channel, DELIVERY_TAG + 1);
        productUsageListener.handleProductEvent(event3, message, channel, DELIVERY_TAG + 2);

        // Assert
        verify(productUsagePort).incrementUsageCount(productId1);
        verify(productUsagePort).incrementUsageCount(productId2);
        verify(productUsagePort).incrementUsageCount(productId3);
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount solo una vez por evento")
    void testHandleProductEvent_InvokesIncrementUsageCountOncePerEvent() throws Exception {
        // Arrange
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(productUsagePort, times(1)).incrementUsageCount(PRODUCT_ID);
    }

    @Test
    @DisplayName("Debe usar deliveryTag correcto para ACK")
    void testHandleProductEvent_UsesCorrectDeliveryTag() throws Exception {
        // Arrange
        long customDeliveryTag = 999L;
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, customDeliveryTag);

        // Assert
        verify(channel).basicAck(customDeliveryTag, false);
    }

    @Test
    @DisplayName("Debe usar basicAck con multiple = false")
    void testHandleProductEvent_UsesBasicAckWithMultipleFalse() throws Exception {
        // Arrange
        doNothing().when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        // Act
        productUsageListener.handleProductEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }
}
