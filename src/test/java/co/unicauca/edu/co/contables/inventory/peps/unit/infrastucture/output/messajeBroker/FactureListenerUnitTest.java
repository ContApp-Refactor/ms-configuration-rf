package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.messajeBroker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.FactureListener;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.enums.EventFactureType;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.mapper.IKardexRabbitMQMapper;
import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
public class FactureListenerUnitTest {
    
    @Mock
    private IKardexCommandPort kardexCommandPort;

    @Mock
    private IKardexRabbitMQMapper kardexRabbitMQMapper;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @InjectMocks
    private FactureListener factureListener;

    private EventDto<KardexRabbitDto, EventFactureType> event;
    private KardexRabbitDto kardexRabbitDto;
    private Kardex kardex;
    private Product product;
    private long deliveryTag;

    @BeforeEach
    void setUp() {
        deliveryTag = 1L;
        
        // Initialize Product
        product = new Product();
        product.setId(1L);
        product.setProductId(100L);
        product.setName("Test Product");
        
        // Initialize KardexRabbitDto
        kardexRabbitDto = new KardexRabbitDto();
        kardexRabbitDto.setProductId(100L);
        kardexRabbitDto.setFactCode("1001");
        kardexRabbitDto.setQuantity(10L);
        kardexRabbitDto.setUnitPrice(BigDecimal.valueOf(100.00));
        kardexRabbitDto.setDetails("Test purchase");

        // Initialize Kardex domain object
        kardex = Kardex.builder()
            .idKardex(1L)
            .factCode("1001")
            .quantity(10)
            .unitPrice(BigDecimal.valueOf(100.00))
            .details("Test purchase")
            .date(ZonedDateTime.now())
            .product(product)
            .build();

        // Initialize event
        event = new EventDto<>();
        event.setType(EventFactureType.PURCHASE);
        event.setData(kardexRabbitDto);
    }

    // ==================== PURCHASE Events ====================
    @Test
    @DisplayName("Should process PURCHASE event successfully")
    void testHandlePurchaseEvent_Success() throws IOException {
        // Arrange
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexRabbitMQMapper, times(1)).toDomain(kardexRabbitDto);
        verify(kardexCommandPort, times(1)).registerPurchase(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    // ==================== SALE Events ====================
    @Test
    @DisplayName("Should process SALE event successfully")
    void testHandleSaleEvent_Success() throws IOException {
        // Arrange
        event.setType(EventFactureType.SALE);
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerSale(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexRabbitMQMapper, times(1)).toDomain(kardexRabbitDto);
        verify(kardexCommandPort, times(1)).registerSale(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    // ==================== RETURNONSALE Events ====================
    @Test
    @DisplayName("Should process RETURNONSALE event successfully")
    void testHandleReturnOnSaleEvent_Success() throws IOException {
        // Arrange
        event.setType(EventFactureType.RETURNONSALE);
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerSaleReturn(any(Kardex.class))).thenReturn(List.of(kardex));

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexRabbitMQMapper, times(1)).toDomain(kardexRabbitDto);
        verify(kardexCommandPort, times(1)).registerSaleReturn(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    // ==================== RETURNONPURCHASE Events ====================
    @Test
    @DisplayName("Should process RETURNONPURCHASE event successfully")
    void testHandleReturnOnPurchaseEvent_Success() throws IOException {
        // Arrange
        event.setType(EventFactureType.RETURNONPURCHASE);
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchaseReturn(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexRabbitMQMapper, times(1)).toDomain(kardexRabbitDto);
        verify(kardexCommandPort, times(1)).registerPurchaseReturn(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    // ==================== NONCOMMERCIALENTRY Events ====================
    @Test
    @DisplayName("Should process NONCOMMERCIALENTRY event successfully")
    void testHandleNonCommercialEntryEvent_Success() throws IOException {
        // Arrange
        event.setType(EventFactureType.NONCOMMERCIALENTRY);
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerNonCommercialEntry(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexRabbitMQMapper, times(1)).toDomain(kardexRabbitDto);
        verify(kardexCommandPort, times(1)).registerNonCommercialEntry(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    // ==================== NONCOMMERCIALEXIT Events ====================
    @Test
    @DisplayName("Should process NONCOMMERCIALEXIT event successfully")
    void testHandleNonCommercialExitEvent_Success() throws IOException {
        // Arrange
        event.setType(EventFactureType.NONCOMMERCIALEXIT);
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerNonCommercialExit(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexRabbitMQMapper, times(1)).toDomain(kardexRabbitDto);
        verify(kardexCommandPort, times(1)).registerNonCommercialExit(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    // ==================== Error Handling Tests ====================
    @Test
    @DisplayName("Should handle processing exception gracefully")
    void testHandleProcessingException() throws IOException {
        // Arrange
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> 
            factureListener.handleFactureEvent(event, message, channel, deliveryTag)
        );
        
        verify(kardexCommandPort, times(1)).registerPurchase(kardex);
    }

    @Test
    @DisplayName("Should handle mapper exception gracefully")
    void testHandleMapperException() throws IOException {
        // Arrange
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class)))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> 
            factureListener.handleFactureEvent(event, message, channel, deliveryTag)
        );
        
        verify(kardexCommandPort, never()).registerPurchase(any());
    }

    @Test
    @DisplayName("Should handle acknowledgment failure gracefully")
    void testHandleAcknowledgmentFailure() throws IOException {
        // Arrange
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);
        doThrow(new IOException("Channel closed")).when(channel).basicAck(deliveryTag, false);

        // Act & Assert
        assertDoesNotThrow(() -> 
            factureListener.handleFactureEvent(event, message, channel, deliveryTag)
        );
        
        verify(kardexCommandPort, times(1)).registerPurchase(kardex);
    }

    // ==================== Integration Tests ====================
    @Test
    @DisplayName("Should process event with all optional fields populated")
    void testProcessEventWithAllFieldsPopulated() throws IOException {
        // Arrange
        kardexRabbitDto.setDetails("Complete purchase details");
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
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
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
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
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
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
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);

        // Act
        factureListener.handleFactureEvent(event, message, channel, deliveryTag);

        // Assert
        verify(kardexCommandPort, times(1)).registerPurchase(kardex);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should process all event types correctly")
    void testProcessAllEventTypes() throws IOException {
        // Arrange
        when(kardexRabbitMQMapper.toDomain(any())).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any())).thenReturn(kardex);
        when(kardexCommandPort.registerSale(any())).thenReturn(kardex);
        when(kardexCommandPort.registerSaleReturn(any())).thenReturn(List.of(kardex));
        when(kardexCommandPort.registerPurchaseReturn(any())).thenReturn(kardex);
        when(kardexCommandPort.registerNonCommercialEntry(any())).thenReturn(kardex);
        when(kardexCommandPort.registerNonCommercialExit(any())).thenReturn(kardex);

        // Act & Assert - Test all event types
        event.setType(EventFactureType.PURCHASE);
        assertDoesNotThrow(() -> factureListener.handleFactureEvent(event, message, channel, 1L));

        event.setType(EventFactureType.SALE);
        assertDoesNotThrow(() -> factureListener.handleFactureEvent(event, message, channel, 2L));

        event.setType(EventFactureType.RETURNONSALE);
        assertDoesNotThrow(() -> factureListener.handleFactureEvent(event, message, channel, 3L));

        event.setType(EventFactureType.RETURNONPURCHASE);
        assertDoesNotThrow(() -> factureListener.handleFactureEvent(event, message, channel, 4L));

        event.setType(EventFactureType.NONCOMMERCIALENTRY);
        assertDoesNotThrow(() -> factureListener.handleFactureEvent(event, message, channel, 5L));

        event.setType(EventFactureType.NONCOMMERCIALEXIT);
        assertDoesNotThrow(() -> factureListener.handleFactureEvent(event, message, channel, 6L));

        // Verify all were called
        verify(kardexCommandPort).registerPurchase(any());
        verify(kardexCommandPort).registerSale(any());
        verify(kardexCommandPort).registerSaleReturn(any());
        verify(kardexCommandPort).registerPurchaseReturn(any());
        verify(kardexCommandPort).registerNonCommercialEntry(any());
        verify(kardexCommandPort).registerNonCommercialExit(any());
    }
}
