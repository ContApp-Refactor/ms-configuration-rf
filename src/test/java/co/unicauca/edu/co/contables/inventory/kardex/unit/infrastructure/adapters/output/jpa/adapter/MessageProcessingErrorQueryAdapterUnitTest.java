package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter.MessageProcessingErrorQueryAdapter;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.IMessageProcessingErrorEntityMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IMessageProcessingErrorRepository;

/**
 * @brief Unit tests for MessageProcessingErrorQueryAdapter
 * 
 * Tests the query operations for message processing error records.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageProcessingErrorQueryAdapter Tests")
class MessageProcessingErrorQueryAdapterUnitTest {

    @Mock
    private IMessageProcessingErrorRepository messageProcessingErrorRepository;

    @Mock
    private IMessageProcessingErrorEntityMapper messageProcessingErrorMapper;

    @InjectMocks
    private MessageProcessingErrorQueryAdapter queryAdapter;

    private MessageProcessingErrorEntity errorEntity1;
    private MessageProcessingErrorEntity errorEntity2;
    private MessageProcessingError errorDomain1;
    private MessageProcessingError errorDomain2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        // Create first error entity
        errorEntity1 = new MessageProcessingErrorEntity();
        errorEntity1.setId(1L);
        errorEntity1.setEventType("PRODUCT_CREATED");
        errorEntity1.setErrorDescription("Failed to process message");
        errorEntity1.setMessageData("{\"productId\": 1}");
        errorEntity1.setErrorTimestamp(Instant.now());
        errorEntity1.setEntityType("Product");

        // Create second error entity
        errorEntity2 = new MessageProcessingErrorEntity();
        errorEntity2.setId(2L);
        errorEntity2.setEventType("INVOICE_UPDATED");
        errorEntity2.setErrorDescription("Connection timeout");
        errorEntity2.setMessageData("{\"invoiceId\": 123}");
        errorEntity2.setErrorTimestamp(Instant.now().plusSeconds(60));
        errorEntity2.setEntityType("Invoice");

        // Create domain objects
        errorDomain1 = new MessageProcessingError();
        errorDomain1.setId(1L);
        errorDomain1.setEventType("PRODUCT_CREATED");
        errorDomain1.setErrorDescription("Failed to process message");
        errorDomain1.setMessageData("{\"productId\": 1}");
        errorDomain1.setErrorTimestamp(errorEntity1.getErrorTimestamp());
        errorDomain1.setEntityType("Product");

        errorDomain2 = new MessageProcessingError();
        errorDomain2.setId(2L);
        errorDomain2.setEventType("INVOICE_UPDATED");
        errorDomain2.setErrorDescription("Connection timeout");
        errorDomain2.setMessageData("{\"invoiceId\": 123}");
        errorDomain2.setErrorTimestamp(errorEntity2.getErrorTimestamp());
        errorDomain2.setEntityType("Invoice");
    }

    @Test
    @DisplayName("Should find message processing error by ID successfully")
    void testFindById_Success() {
        // Arrange
        when(messageProcessingErrorRepository.findById(1L)).thenReturn(Optional.of(errorEntity1));
        when(messageProcessingErrorMapper.toDomain(errorEntity1)).thenReturn(errorDomain1);

        // Act
        Optional<MessageProcessingError> result = queryAdapter.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("PRODUCT_CREATED", result.get().getEventType());
        assertEquals("Product", result.get().getEntityType());

        verify(messageProcessingErrorRepository, times(1)).findById(1L);
        verify(messageProcessingErrorMapper, times(1)).toDomain(errorEntity1);
    }

    @Test
    @DisplayName("Should return empty optional when error not found by ID")
    void testFindById_NotFound() {
        // Arrange
        when(messageProcessingErrorRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<MessageProcessingError> result = queryAdapter.findById(999L);

        // Assert
        assertFalse(result.isPresent());

        verify(messageProcessingErrorRepository, times(1)).findById(999L);
        verify(messageProcessingErrorMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find last record successfully")
    void testFindLastRecord_Success() {
        // Arrange
        when(messageProcessingErrorRepository.findFirstByOrderByErrorTimestampDesc())
            .thenReturn(Optional.of(errorEntity2));
        when(messageProcessingErrorMapper.toDomain(errorEntity2)).thenReturn(errorDomain2);

        // Act
        Optional<MessageProcessingError> result = queryAdapter.findLastRecord();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getId());
        assertEquals("INVOICE_UPDATED", result.get().getEventType());

        verify(messageProcessingErrorRepository, times(1)).findFirstByOrderByErrorTimestampDesc();
        verify(messageProcessingErrorMapper, times(1)).toDomain(errorEntity2);
    }

    @Test
    @DisplayName("Should return empty optional when no last record exists")
    void testFindLastRecord_NotFound() {
        // Arrange
        when(messageProcessingErrorRepository.findFirstByOrderByErrorTimestampDesc())
            .thenReturn(Optional.empty());

        // Act
        Optional<MessageProcessingError> result = queryAdapter.findLastRecord();

        // Assert
        assertFalse(result.isPresent());

        verify(messageProcessingErrorRepository, times(1)).findFirstByOrderByErrorTimestampDesc();
        verify(messageProcessingErrorMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find all errors with pagination successfully")
    void testFindAll_Success() {
        // Arrange
        Page<MessageProcessingErrorEntity> entityPage = new PageImpl<>(
            Arrays.asList(errorEntity1, errorEntity2),
            pageable,
            2
        );

        when(messageProcessingErrorRepository.findAll(pageable)).thenReturn(entityPage);
        when(messageProcessingErrorMapper.toDomain(errorEntity1)).thenReturn(errorDomain1);
        when(messageProcessingErrorMapper.toDomain(errorEntity2)).thenReturn(errorDomain2);

        // Act
        Page<MessageProcessingError> result = queryAdapter.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals("PRODUCT_CREATED", result.getContent().get(0).getEventType());
        assertEquals("INVOICE_UPDATED", result.getContent().get(1).getEventType());

        verify(messageProcessingErrorRepository, times(1)).findAll(pageable);
        verify(messageProcessingErrorMapper, times(2)).toDomain(any(MessageProcessingErrorEntity.class));
    }

    @Test
    @DisplayName("Should return empty page when no errors exist")
    void testFindAll_EmptyResult() {
        // Arrange
        Page<MessageProcessingErrorEntity> emptyPage = new PageImpl<>(
            Collections.emptyList(),
            pageable,
            0
        );

        when(messageProcessingErrorRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<MessageProcessingError> result = queryAdapter.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(messageProcessingErrorRepository, times(1)).findAll(pageable);
        verify(messageProcessingErrorMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testFindAll_Pagination() {
        // Arrange
        Pageable secondPage = PageRequest.of(1, 5);
        Page<MessageProcessingErrorEntity> entityPage = new PageImpl<>(
            Arrays.asList(errorEntity1),
            secondPage,
            10
        );

        when(messageProcessingErrorRepository.findAll(secondPage)).thenReturn(entityPage);
        when(messageProcessingErrorMapper.toDomain(errorEntity1)).thenReturn(errorDomain1);

        // Act
        Page<MessageProcessingError> result = queryAdapter.findAll(secondPage);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber());
        assertEquals(5, result.getSize());
        assertEquals(10, result.getTotalElements());

        verify(messageProcessingErrorRepository, times(1)).findAll(secondPage);
    }

    @Test
    @DisplayName("Should map entity to domain correctly")
    void testMapperConversion() {
        // Arrange
        when(messageProcessingErrorRepository.findById(1L)).thenReturn(Optional.of(errorEntity1));
        when(messageProcessingErrorMapper.toDomain(errorEntity1)).thenReturn(errorDomain1);

        // Act
        Optional<MessageProcessingError> result = queryAdapter.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        MessageProcessingError error = result.get();
        assertEquals(errorEntity1.getId(), error.getId());
        assertEquals(errorEntity1.getEventType(), error.getEventType());
        assertEquals(errorEntity1.getErrorDescription(), error.getErrorDescription());
        assertEquals(errorEntity1.getMessageData(), error.getMessageData());
        assertEquals(errorEntity1.getEntityType(), error.getEntityType());

        verify(messageProcessingErrorMapper, times(1)).toDomain(errorEntity1);
    }

    @Test
    @DisplayName("Should handle multiple findById calls")
    void testFindById_MultipleCalls() {
        // Arrange
        when(messageProcessingErrorRepository.findById(1L)).thenReturn(Optional.of(errorEntity1));
        when(messageProcessingErrorRepository.findById(2L)).thenReturn(Optional.of(errorEntity2));
        when(messageProcessingErrorMapper.toDomain(errorEntity1)).thenReturn(errorDomain1);
        when(messageProcessingErrorMapper.toDomain(errorEntity2)).thenReturn(errorDomain2);

        // Act
        Optional<MessageProcessingError> result1 = queryAdapter.findById(1L);
        Optional<MessageProcessingError> result2 = queryAdapter.findById(2L);

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(1L, result1.get().getId());
        assertEquals(2L, result2.get().getId());

        verify(messageProcessingErrorRepository).findById(1L);
        verify(messageProcessingErrorRepository).findById(2L);
    }

    @Test
    @DisplayName("Should verify correct error details in findAll")
    void testFindAll_ErrorDetails() {
        // Arrange
        Page<MessageProcessingErrorEntity> entityPage = new PageImpl<>(
            Arrays.asList(errorEntity1),
            pageable,
            1
        );

        when(messageProcessingErrorRepository.findAll(pageable)).thenReturn(entityPage);
        when(messageProcessingErrorMapper.toDomain(errorEntity1)).thenReturn(errorDomain1);

        // Act
        Page<MessageProcessingError> result = queryAdapter.findAll(pageable);

        // Assert
        assertNotNull(result);
        MessageProcessingError error = result.getContent().get(0);
        assertEquals("PRODUCT_CREATED", error.getEventType());
        assertEquals("Failed to process message", error.getErrorDescription());
        assertEquals("{\"productId\": 1}", error.getMessageData());
        assertEquals("Product", error.getEntityType());
    }

    @Test
    @DisplayName("Should handle large page size")
    void testFindAll_LargePageSize() {
        // Arrange
        Pageable largePage = PageRequest.of(0, 100);
        Page<MessageProcessingErrorEntity> entityPage = new PageImpl<>(
            Arrays.asList(errorEntity1, errorEntity2),
            largePage,
            2
        );

        when(messageProcessingErrorRepository.findAll(largePage)).thenReturn(entityPage);
        when(messageProcessingErrorMapper.toDomain(any())).thenReturn(errorDomain1, errorDomain2);

        // Act
        Page<MessageProcessingError> result = queryAdapter.findAll(largePage);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getSize());
        assertEquals(2, result.getContent().size());

        verify(messageProcessingErrorRepository, times(1)).findAll(largePage);
    }

    @Test
    @DisplayName("Should return most recent error in findLastRecord")
    void testFindLastRecord_MostRecent() {
        // Arrange
        Instant now = Instant.now();
        errorEntity2.setErrorTimestamp(now);
        errorDomain2.setErrorTimestamp(now);

        when(messageProcessingErrorRepository.findFirstByOrderByErrorTimestampDesc())
            .thenReturn(Optional.of(errorEntity2));
        when(messageProcessingErrorMapper.toDomain(errorEntity2)).thenReturn(errorDomain2);

        // Act
        Optional<MessageProcessingError> result = queryAdapter.findLastRecord();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(now, result.get().getErrorTimestamp());

        verify(messageProcessingErrorRepository, times(1)).findFirstByOrderByErrorTimestampDesc();
    }
}
