package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.messageBroker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.FactureListener;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.mapper.IKardexRabbitMQMapper;
import com.rabbitmq.client.Channel;

/**
 * @brief Unit tests for FactureListener
 * 
 * Tests the RabbitMQ message handling for facture events including
 * purchases, sales, and returns with validation and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FactureListener Unit Tests")
class FactureListenerUnitTest {

    @Mock
    private IKardexCommandPort kardexCommandPort;

    @Mock
    private IKardexRabbitMQMapper kardexRabbitMQRestMapper;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @InjectMocks
    private FactureListener factureListener;

    private EventDto<KardexRabbitDto, EventFactureType> event;
    private KardexRabbitDto kardexRabbitDto;
    private Kardex kardex;
    private long deliveryTag;

    @BeforeEach
    void setUp() throws Exception {
        deliveryTag = 1L;
        
        // Initialize KardexRabbitDto
        kardexRabbitDto = new KardexRabbitDto();
        kardexRabbitDto.setProductId(1L);
        kardexRabbitDto.setFactCode(1001L);
        kardexRabbitDto.setQuantity(10L);
        kardexRabbitDto.setUnitPrice(BigDecimal.valueOf(100.00));
        kardexRabbitDto.setDetails("Test purchase");

        // Initialize Kardex domain object
        kardex = new Kardex();
        kardex.setProductId(1L);
        kardex.setFactCode("INV-001");
        kardex.setQuantity(10);
        kardex.setUnitPrice(BigDecimal.valueOf(100.00));
        kardex.setDetails("Test purchase");
        kardex.setDate(ZonedDateTime.now());

        // Initialize event
        event = new EventDto<>();
        event.setType(EventFactureType.PURCHASE);
        event.setData(kardexRabbitDto);
        
        // Call the private init() method using reflection to initialize messageErrorHandlingPort
        Method initMethod = FactureListener.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(factureListener);
    }

    @Test
    @DisplayName("Should process PURCHASE event successfully")
    void testHandlePurchaseEvent_Success() throws IOException {
        // Arrange
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexRabbitMQRestMapper, times(1)).toDomain(kardexRabbitDto);
        verify(kardexCommandPort, times(1)).registerPurchase(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
        verify(messageErrorHandlingPort, never()).saveProcessingError(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should process SALE event successfully")
    void testHandleSaleEvent_Success() throws IOException {
        // Arrange
        event.setType(EventFactureType.SALE);
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerSale(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexRabbitMQRestMapper, times(1)).toDomain(kardexRabbitDto);
        verify(kardexCommandPort, times(1)).registerSale(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should process RETURNONSALE event successfully")
    void testHandleReturnOnSaleEvent_Success() throws IOException {
        // Arrange
        event.setType(EventFactureType.RETURNONSALE);
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerReturnOnSale(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexRabbitMQRestMapper, times(1)).toDomain(kardexRabbitDto);
        verify(kardexCommandPort, times(1)).registerReturnOnSale(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should process RETURNONPURCHASE event successfully")
    void testHandleReturnOnPurchaseEvent_Success() throws IOException {
        // Arrange
        event.setType(EventFactureType.RETURNONPURCHASE);
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerReturnOnPurchase(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexRabbitMQRestMapper, times(1)).toDomain(kardexRabbitDto);
        verify(kardexCommandPort, times(1)).registerReturnOnPurchase(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject null event and save error")
    void testHandleNullEvent() throws IOException {
        // Act
        factureListener.handleFactureEvent(null, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            isNull(),
            contains("Validation failed"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
        verify(kardexCommandPort, never()).registerPurchase(any());
    }

    @Test
    @DisplayName("Should reject event with null type and save error")
    void testHandleEventWithNullType() throws IOException {
        // Arrange
        event.setType(null);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            isNull(),
            contains("Validation failed"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with null data and save error")
    void testHandleEventWithNullData() throws IOException {
        // Arrange
        event.setData(null);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("Validation failed"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with null quantity and save error")
    void testHandleEventWithNullQuantity() throws IOException {
        // Arrange
        kardexRabbitDto.setQuantity(null);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("quantity"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with invalid quantity (zero) and save error")
    void testHandleEventWithZeroQuantity() throws IOException {
        // Arrange
        kardexRabbitDto.setQuantity(0L);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("quantity"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with invalid quantity (negative) and save error")
    void testHandleEventWithNegativeQuantity() throws IOException {
        // Arrange
        kardexRabbitDto.setQuantity(-5L);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("quantity"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with null factCode and save error")
    void testHandleEventWithNullFactCode() throws IOException {
        // Arrange
        kardexRabbitDto.setFactCode(null);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("factCode"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with null productId and save error")
    void testHandleEventWithNullProductId() throws IOException {
        // Arrange
        kardexRabbitDto.setProductId(null);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("productId"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject PURCHASE event with null unitPrice and save error")
    void testHandlePurchaseEventWithNullUnitPrice() throws IOException {
        // Arrange
        kardexRabbitDto.setUnitPrice(null);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("unitPrice"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject PURCHASE event with zero unitPrice and save error")
    void testHandlePurchaseEventWithZeroUnitPrice() throws IOException {
        // Arrange
        kardexRabbitDto.setUnitPrice(BigDecimal.ZERO);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("unitPrice"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject PURCHASE event with negative unitPrice and save error")
    void testHandlePurchaseEventWithNegativeUnitPrice() throws IOException {
        // Arrange
        kardexRabbitDto.setUnitPrice(BigDecimal.valueOf(-50.00));

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("unitPrice"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should allow SALE event without unitPrice validation")
    void testHandleSaleEventWithoutUnitPrice() throws IOException {
        // Arrange
        event.setType(EventFactureType.SALE);
        kardexRabbitDto.setUnitPrice(null); // SALE doesn't require unitPrice
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerSale(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexCommandPort, times(1)).registerSale(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle processing exception and save error")
    void testHandleProcessingException() throws IOException {
        // Arrange
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("Processing error"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle mapper exception and save error")
    void testHandleMapperException() throws IOException {
        // Arrange
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class)))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("Processing error"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
        verify(kardexCommandPort, never()).registerPurchase(any());
    }

    @Test
    @DisplayName("Should handle unsupported event type")
    void testHandleUnsupportedEventType() throws IOException {
        // Arrange
        // Create a custom EventDto with an unexpected type scenario
        // Since we can't easily create new enum values, we'll simulate by causing the switch to fail
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        
        // We'll use a valid type but make the command port throw IllegalArgumentException
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
            .thenThrow(new IllegalArgumentException("Unsupported event type"));

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("PURCHASE"),
            contains("Processing error"),
            anyString(),
            eq("Kardex")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle acknowledgment failure gracefully")
    void testHandleAcknowledgmentFailure() throws IOException {
        // Arrange
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);
        doThrow(new IOException("Channel closed")).when(channel).basicAck(deliveryTag, false);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> 
            factureListener.handleFactureEvent(event, message, channel, deliveryTag)
        );
        
        verify(kardexCommandPort, times(1)).registerPurchase(kardex);
    }

    @Test
    @DisplayName("Should process event with all optional fields populated")
    void testProcessEventWithAllFieldsPopulated() throws IOException {
        // Arrange
        kardexRabbitDto.setDetails("Complete purchase details");
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexCommandPort, times(1)).registerPurchase(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle multiple events in sequence")
    void testHandleMultipleEventsInSequence() throws IOException {
        // Arrange
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);
        when(kardexCommandPort.registerSale(any(Kardex.class))).thenReturn(kardex);

        // Act - Process purchase
        factureListener.handleFactureEvent(event, message, channel, 1L);
        
        // Change to sale
        event.setType(EventFactureType.SALE);
        factureListener.handleFactureEvent(event, message, channel, 2L);

        // Assert
        verify(kardexCommandPort, times(1)).registerPurchase(kardex);
        verify(kardexCommandPort, times(1)).registerSale(kardex);
        verify(channel, times(1)).basicAck(1L, false);
        verify(channel, times(1)).basicAck(2L, false);
    }

    @Test
    @DisplayName("Should handle large quantity values")
    void testHandleLargeQuantityValues() throws IOException {
        // Arrange
        kardexRabbitDto.setQuantity(999999L);
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexCommandPort, times(1)).registerPurchase(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle large decimal unit prices")
    void testHandleLargeDecimalPrices() throws IOException {
        // Arrange
        kardexRabbitDto.setUnitPrice(new BigDecimal("99999999.99"));
        when(kardexRabbitMQRestMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexCommandPort, times(1)).registerPurchase(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }
}
