package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.messageBroker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.product.IProductCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.ProductListener;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.enums.EventProductType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.mapper.ProductBrokerMapper;
import com.rabbitmq.client.Channel;

/**
 * @brief Unit tests for ProductListener
 * 
 * Tests the RabbitMQ message handling for product events including
 * creation, updates, and deletions with validation and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductListener Unit Tests")
class ProductListenerUnitTest {

    @Mock
    private IProductCommandRepositoryPort productCommandPort;

    @Mock
    private ProductBrokerMapper productBrokerMapper;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Mock
    private IEventRecoveryActionPort<EventDto<ProductAsyncDto, EventProductType>> productRecoveryActionPort;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @InjectMocks
    private ProductListener productListener;

    private EventDto<ProductAsyncDto, EventProductType> event;
    private ProductAsyncDto productAsyncDto;
    private Product product;
    private long deliveryTag;

    @BeforeEach
    void setUp() throws Exception {
        deliveryTag = 1L;
        
        // Initialize ProductAsyncDto
        productAsyncDto = new ProductAsyncDto();
        productAsyncDto.setProductId(1L);
        productAsyncDto.setName("Test Product");
        productAsyncDto.setReference("REF-001");
        productAsyncDto.setEnterpriseId("ENT-123");
        productAsyncDto.setPresentation("Box of 10");

        // Initialize Product domain object
        product = new Product();
        product.setProductId(1L);
        product.setName("Test Product");
        product.setReference("REF-001");
        product.setEnterpriseId("ENT-123");
        product.setPresentation("Box of 10");

        // Initialize event
        event = new EventDto<>();
        event.setType(EventProductType.CREATED);
        event.setData(productAsyncDto);
        
        // Call the private init() method using reflection to initialize messageErrorHandlingPort
        Method initMethod = ProductListener.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(productListener);
    }

    @Test
    @DisplayName("Should process CREATED event successfully")
    void testHandleCreatedEvent_Success() throws IOException {
        // Arrange
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class))).thenReturn("Product saved successfully");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productBrokerMapper, times(1)).toDomain(productAsyncDto);
        verify(productCommandPort, times(1)).save(product);
        verify(channel, times(1)).basicAck(deliveryTag, false);
        verify(messageErrorHandlingPort, never()).saveProcessingError(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should process UPDATED event successfully")
    void testHandleUpdatedEvent_Success() throws IOException {
        // Arrange
        event.setType(EventProductType.UPDATED);
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.update(any(Product.class))).thenReturn("Product updated successfully");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productBrokerMapper, times(1)).toDomain(productAsyncDto);
        verify(productCommandPort, times(1)).update(product);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should process DELETED event successfully")
    void testHandleDeletedEvent_Success() throws IOException {
        // Arrange
        event.setType(EventProductType.DELETED);
        when(productCommandPort.delete(anyLong())).thenReturn("Product deleted successfully");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productCommandPort, times(1)).delete(productAsyncDto.getProductId());
        verify(channel, times(1)).basicAck(deliveryTag, false);
        verify(productBrokerMapper, never()).toDomain(any()); // DELETE doesn't need mapping
    }

    @Test
    @DisplayName("Should reject null event and save error")
    void testHandleNullEvent() throws IOException {
        // Act
        productListener.handleProductEvent(null, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            isNull(),
            contains("Validation failed"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
        verify(productCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("Should reject event with null type and save error")
    void testHandleEventWithNullType() throws IOException {
        // Arrange
        event.setType(null);

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            isNull(),
            contains("Validation failed"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with null data and save error")
    void testHandleEventWithNullData() throws IOException {
        // Arrange
        event.setData(null);

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Validation failed"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with null productId and save error")
    void testHandleEventWithNullProductId() throws IOException {
        // Arrange
        productAsyncDto.setProductId(null);

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("productId"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with null name and save error")
    void testHandleEventWithNullName() throws IOException {
        // Arrange
        productAsyncDto.setName(null);

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Name is null or empty"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with empty name and save error")
    void testHandleEventWithEmptyName() throws IOException {
        // Arrange
        productAsyncDto.setName("   ");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Name is null or empty"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with null reference and save error")
    void testHandleEventWithNullReference() throws IOException {
        // Arrange
        productAsyncDto.setReference(null);

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Reference is null or empty"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with empty reference and save error")
    void testHandleEventWithEmptyReference() throws IOException {
        // Arrange
        productAsyncDto.setReference("");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Reference is null or empty"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with null enterpriseId and save error")
    void testHandleEventWithNullEnterpriseId() throws IOException {
        // Arrange
        productAsyncDto.setEnterpriseId(null);

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("EnterpriseId is null or empty"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should reject event with empty enterpriseId and save error")
    void testHandleEventWithEmptyEnterpriseId() throws IOException {
        // Arrange
        productAsyncDto.setEnterpriseId("  ");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("EnterpriseId is null or empty"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should allow event with null presentation (optional field)")
    void testHandleEventWithNullPresentation() throws IOException {
        // Arrange
        productAsyncDto.setPresentation(null);
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class))).thenReturn("Product saved");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productCommandPort, times(1)).save(product);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle processing exception and save error")
    void testHandleProcessingException() throws IOException {
        // Arrange
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class)))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Processing error"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle processing exception with recovery action")
    void testHandleProcessingExceptionWithRecovery() throws IOException {
        // Arrange
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class)))
            .thenThrow(new RuntimeException("Duplicate product"));
        when(productRecoveryActionPort.canHandle(any())).thenReturn(true);
        when(productRecoveryActionPort.executeRecoveryAction(any())).thenReturn(true);

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productRecoveryActionPort, times(1)).canHandle(event);
        verify(productRecoveryActionPort, times(1)).executeRecoveryAction(event);
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Recovery action executed"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle processing exception when recovery action fails")
    void testHandleProcessingExceptionWithFailedRecovery() throws IOException {
        // Arrange
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class)))
            .thenThrow(new RuntimeException("Database error"));
        when(productRecoveryActionPort.canHandle(any())).thenReturn(true);
        when(productRecoveryActionPort.executeRecoveryAction(any())).thenReturn(false);

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productRecoveryActionPort, times(1)).canHandle(event);
        verify(productRecoveryActionPort, times(1)).executeRecoveryAction(event);
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Processing error"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle recovery action exception")
    void testHandleRecoveryActionException() throws IOException {
        // Arrange
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class)))
            .thenThrow(new RuntimeException("Database error"));
        when(productRecoveryActionPort.canHandle(any())).thenReturn(true);
        when(productRecoveryActionPort.executeRecoveryAction(any()))
            .thenThrow(new RuntimeException("Recovery failed"));

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productRecoveryActionPort, times(1)).executeRecoveryAction(event);
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Processing error"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle mapper exception and save error")
    void testHandleMapperException() throws IOException {
        // Arrange
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class)))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Processing error"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
        verify(productCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("Should handle unsupported event type")
    void testHandleUnsupportedEventType() throws IOException {
        // Arrange
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class)))
            .thenThrow(new IllegalArgumentException("Unsupported event type"));

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(messageErrorHandlingPort, times(1)).saveProcessingError(
            eq("CREATED"),
            contains("Processing error"),
            anyString(),
            eq("Product")
        );
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle acknowledgment failure gracefully")
    void testHandleAcknowledgmentFailure() throws IOException {
        // Arrange
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class))).thenReturn("Product saved");
        doThrow(new IOException("Channel closed")).when(channel).basicAck(deliveryTag, false);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> 
            productListener.handleProductEvent(event, message, channel, deliveryTag)
        );
        
        verify(productCommandPort, times(1)).save(product);
    }

    @Test
    @DisplayName("Should handle multiple events in sequence")
    void testHandleMultipleEventsInSequence() throws IOException {
        // Arrange
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class))).thenReturn("Saved");
        when(productCommandPort.update(any(Product.class))).thenReturn("Updated");

        // Act - Process created
        productListener.handleProductEvent(event, message, channel, 1L);
        
        // Change to updated
        event.setType(EventProductType.UPDATED);
        productListener.handleProductEvent(event, message, channel, 2L);

        // Assert
        verify(productCommandPort, times(1)).save(product);
        verify(productCommandPort, times(1)).update(product);
        verify(channel, times(1)).basicAck(1L, false);
        verify(channel, times(1)).basicAck(2L, false);
    }

    @Test
    @DisplayName("Should process all event types successfully")
    void testProcessAllEventTypes() throws IOException {
        // Arrange
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class))).thenReturn("Saved");
        when(productCommandPort.update(any(Product.class))).thenReturn("Updated");
        when(productCommandPort.delete(anyLong())).thenReturn("Deleted");

        // Act & Assert - CREATED
        event.setType(EventProductType.CREATED);
        productListener.handleProductEvent(event, message, channel, 1L);
        verify(productCommandPort, times(1)).save(product);

        // Act & Assert - UPDATED
        event.setType(EventProductType.UPDATED);
        productListener.handleProductEvent(event, message, channel, 2L);
        verify(productCommandPort, times(1)).update(product);

        // Act & Assert - DELETED
        event.setType(EventProductType.DELETED);
        productListener.handleProductEvent(event, message, channel, 3L);
        verify(productCommandPort, times(1)).delete(productAsyncDto.getProductId());

        verify(channel, times(1)).basicAck(1L, false);
        verify(channel, times(1)).basicAck(2L, false);
        verify(channel, times(1)).basicAck(3L, false);
    }

    @Test
    @DisplayName("Should handle product with long name")
    void testHandleProductWithLongName() throws IOException {
        // Arrange
        String longName = "A".repeat(255);
        productAsyncDto.setName(longName);
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class))).thenReturn("Saved");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productCommandPort, times(1)).save(product);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should handle product with special characters in fields")
    void testHandleProductWithSpecialCharacters() throws IOException {
        // Arrange
        productAsyncDto.setName("Product & Co. (Test) #123");
        productAsyncDto.setReference("REF-001/A");
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class))).thenReturn("Saved");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productCommandPort, times(1)).save(product);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should validate all required fields for CREATED event")
    void testValidateAllRequiredFieldsForCreated() throws IOException {
        // Test that all required fields must be present
        ProductAsyncDto completeDto = new ProductAsyncDto();
        completeDto.setProductId(1L);
        completeDto.setName("Product");
        completeDto.setReference("REF");
        completeDto.setEnterpriseId("ENT");
        event.setData(completeDto);
        
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(any(Product.class))).thenReturn("Saved");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productCommandPort, times(1)).save(product);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should validate all required fields for UPDATED event")
    void testValidateAllRequiredFieldsForUpdated() throws IOException {
        // Test that all required fields must be present
        event.setType(EventProductType.UPDATED);
        
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.update(any(Product.class))).thenReturn("Updated");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productCommandPort, times(1)).update(product);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }

    @Test
    @DisplayName("Should validate all required fields for DELETED event")
    void testValidateAllRequiredFieldsForDeleted() throws IOException {
        // Test that required fields for delete must be present
        event.setType(EventProductType.DELETED);

        when(productCommandPort.delete(anyLong())).thenReturn("Deleted");

        // Act
        productListener.handleProductEvent(event, message, channel, deliveryTag);

        // Assert
        verify(productCommandPort, times(1)).delete(1L);
        verify(channel, times(1)).basicAck(deliveryTag, false);
    }
}
