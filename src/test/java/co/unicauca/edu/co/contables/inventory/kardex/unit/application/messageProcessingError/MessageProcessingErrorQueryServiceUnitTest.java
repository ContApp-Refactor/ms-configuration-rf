package co.unicauca.edu.co.contables.inventory.kardex.unit.application.messageProcessingError;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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

import co.unicauca.edu.co.contables.inventory.kardex.application.service.messageProcessingError.query.MessageProcessingErrorQueryService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.messageProcessingError.IMessageProcessingErrorQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.config.i18n.MessageKeys;

@ExtendWith(MockitoExtension.class)
public class MessageProcessingErrorQueryServiceUnitTest {
    
    @Mock
    private IMessageProcessingErrorQueryRepositoryPort messageProcessingErrorQueryRepositoryPort;
    
    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;
    
    @Mock
    private IMessageServicePort messageService;
    
    @InjectMocks
    private MessageProcessingErrorQueryService messageProcessingErrorQueryService;
    
    private MessageProcessingError messageProcessingError1;
    private MessageProcessingError messageProcessingError2;
    private MessageProcessingError messageProcessingError3;
    private Pageable pageable;
    private List<MessageProcessingError> errorList;
    
    @BeforeEach
    void setUp() {
        // Setup pageable
        pageable = PageRequest.of(0, 10);
        
        // Setup error 1
        messageProcessingError1 = new MessageProcessingError();
        messageProcessingError1.setId(1L);
        messageProcessingError1.setEventType("PURCHASE_EVENT");
        messageProcessingError1.setErrorDescription("Connection timeout");
        messageProcessingError1.setMessageData("Failed message content 1");
        messageProcessingError1.setErrorTimestamp(Instant.now().minusSeconds(5 * 24 * 60 * 60));
        messageProcessingError1.setEntityType("KARDEX");
        
        // Setup error 2
        messageProcessingError2 = new MessageProcessingError();
        messageProcessingError2.setId(2L);
        messageProcessingError2.setEventType("SALE_EVENT");
        messageProcessingError2.setErrorDescription("Invalid message format");
        messageProcessingError2.setMessageData("Failed message content 2");
        messageProcessingError2.setErrorTimestamp(Instant.now().minusSeconds(3 * 24 * 60 * 60));
        messageProcessingError2.setEntityType("KARDEX");
        
        // Setup error 3 (most recent)
        messageProcessingError3 = new MessageProcessingError();
        messageProcessingError3.setId(3L);
        messageProcessingError3.setEventType("UPDATE_EVENT");
        messageProcessingError3.setErrorDescription("Database connection failed");
        messageProcessingError3.setMessageData("Failed message content 3");
        messageProcessingError3.setErrorTimestamp(Instant.now().minusSeconds(2 * 60 * 60));
        messageProcessingError3.setEntityType("STOCK");
        
        errorList = Arrays.asList(messageProcessingError1, messageProcessingError2, messageProcessingError3);
    }
    
    @Test
    @DisplayName("Should find message processing error by id successfully")
    void testFindByIdSuccess() {
        // Arrange
        Long errorId = 1L;
        when(messageProcessingErrorQueryRepositoryPort.findById(errorId))
            .thenReturn(Optional.of(messageProcessingError1));
        
        // Act
        Optional<MessageProcessingError> result = messageProcessingErrorQueryService.findById(errorId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(errorId, result.get().getId());
        assertEquals("Connection timeout", result.get().getErrorDescription());
        verify(messageProcessingErrorQueryRepositoryPort).findById(errorId);
        verify(formatterResultOutputPort, never()).returnEntityDoesNotExistErrorResponse(anyInt(), anyString());
    }
    
    @Test
    @DisplayName("Should throw error when message processing error not found by id")
    void testFindByIdNotFound() {
        // Arrange
        Long errorId = 999L;
        String errorMessage = "Message processing error with id: " + errorId;
        when(messageProcessingErrorQueryRepositoryPort.findById(errorId))
            .thenReturn(Optional.empty());
        when(messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, errorMessage))
            .thenReturn("Error not found: " + errorMessage);
        doThrow(new RuntimeException("Error not found"))
            .when(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(404, 
                "Error not found: " + errorMessage);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageProcessingErrorQueryService.findById(errorId);
        });
        
        verify(messageProcessingErrorQueryRepositoryPort).findById(errorId);
        verify(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(eq(404), anyString());
    }
    
    @Test
    @DisplayName("Should find the most recent message processing error")
    void testFindLastRecordSuccess() {
        // Arrange
        when(messageProcessingErrorQueryRepositoryPort.findLastRecord())
            .thenReturn(Optional.of(messageProcessingError3));
        
        // Act
        Optional<MessageProcessingError> result = messageProcessingErrorQueryService.findLastRecord();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(3L, result.get().getId());
        assertEquals("Database connection failed", result.get().getErrorDescription());
        verify(messageProcessingErrorQueryRepositoryPort).findLastRecord();
        verify(formatterResultOutputPort, never()).returnEntityDoesNotExistErrorResponse(anyInt(), anyString());
    }
    
    @Test
    @DisplayName("Should throw error when no message processing errors exist")
    void testFindLastRecordNotFound() {
        // Arrange
        String errorMessage = "No message processing errors found";
        when(messageProcessingErrorQueryRepositoryPort.findLastRecord())
            .thenReturn(Optional.empty());
        when(messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, errorMessage))
            .thenReturn("Error not found: " + errorMessage);
        doThrow(new RuntimeException("Error not found"))
            .when(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(404, 
                "Error not found: " + errorMessage);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageProcessingErrorQueryService.findLastRecord();
        });
        
        verify(messageProcessingErrorQueryRepositoryPort).findLastRecord();
        verify(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(eq(404), anyString());
    }
    
    @Test
    @DisplayName("Should find all message processing errors with pagination")
    void testFindAllSuccess() {
        // Arrange
        Page<MessageProcessingError> expectedPage = new PageImpl<>(errorList, pageable, errorList.size());
        when(messageProcessingErrorQueryRepositoryPort.findAll(pageable))
            .thenReturn(expectedPage);
        
        // Act
        Page<MessageProcessingError> result = messageProcessingErrorQueryService.findAll(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(errorList.size(), result.getContent().size());
        assertEquals("Connection timeout", result.getContent().get(0).getErrorDescription());
        assertEquals("Invalid message format", result.getContent().get(1).getErrorDescription());
        assertEquals("Database connection failed", result.getContent().get(2).getErrorDescription());
        verify(messageProcessingErrorQueryRepositoryPort).findAll(pageable);
    }
    
    @Test
    @DisplayName("Should return empty page when no errors exist")
    void testFindAllWithNoErrors() {
        // Arrange
        Page<MessageProcessingError> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(messageProcessingErrorQueryRepositoryPort.findAll(pageable))
            .thenReturn(emptyPage);
        
        // Act
        Page<MessageProcessingError> result = messageProcessingErrorQueryService.findAll(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(messageProcessingErrorQueryRepositoryPort).findAll(pageable);
    }
    
    @Test
    @DisplayName("Should handle pagination correctly with different page sizes")
    void testFindAllWithCustomPageable() {
        // Arrange
        Pageable customPageable = PageRequest.of(1, 2);
        List<MessageProcessingError> secondPageList = Arrays.asList(messageProcessingError3);
        Page<MessageProcessingError> secondPage = new PageImpl<>(secondPageList, customPageable, errorList.size());
        when(messageProcessingErrorQueryRepositoryPort.findAll(customPageable))
            .thenReturn(secondPage);
        
        // Act
        Page<MessageProcessingError> result = messageProcessingErrorQueryService.findAll(customPageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements()); // Total records
        assertEquals(1, result.getContent().size()); // Records in current page
        assertEquals(1, result.getNumber()); // Current page number
        verify(messageProcessingErrorQueryRepositoryPort).findAll(customPageable);
    }
    
    @Test
    @DisplayName("Should find different errors by different ids")
    void testFindByIdMultipleErrors() {
        // Arrange
        when(messageProcessingErrorQueryRepositoryPort.findById(1L))
            .thenReturn(Optional.of(messageProcessingError1));
        when(messageProcessingErrorQueryRepositoryPort.findById(2L))
            .thenReturn(Optional.of(messageProcessingError2));
        
        // Act
        Optional<MessageProcessingError> result1 = messageProcessingErrorQueryService.findById(1L);
        Optional<MessageProcessingError> result2 = messageProcessingErrorQueryService.findById(2L);
        
        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals("Connection timeout", result1.get().getErrorDescription());
        assertEquals("Invalid message format", result2.get().getErrorDescription());
        verify(messageProcessingErrorQueryRepositoryPort).findById(1L);
        verify(messageProcessingErrorQueryRepositoryPort).findById(2L);
    }
    
    @Test
    @DisplayName("Should handle null id gracefully")
    void testFindByIdWithNullId() {
        // Arrange
        Long nullId = null;
        when(messageProcessingErrorQueryRepositoryPort.findById(nullId))
            .thenReturn(Optional.empty());
        when(messageService.getMessage(eq(MessageKeys.ERROR_NOT_FOUND), anyString()))
            .thenReturn("Error not found");
        doThrow(new RuntimeException("Error not found"))
            .when(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(eq(404), anyString());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageProcessingErrorQueryService.findById(nullId);
        });
        
        verify(messageProcessingErrorQueryRepositoryPort).findById(nullId);
    }
    
    @Test
    @DisplayName("Should return consistent results when querying multiple times")
    void testConsistentQueryResults() {
        // Arrange
        when(messageProcessingErrorQueryRepositoryPort.findById(1L))
            .thenReturn(Optional.of(messageProcessingError1));
        
        // Act
        Optional<MessageProcessingError> result1 = messageProcessingErrorQueryService.findById(1L);
        Optional<MessageProcessingError> result2 = messageProcessingErrorQueryService.findById(1L);
        
        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(result1.get().getId(), result2.get().getId());
        assertEquals(result1.get().getErrorDescription(), result2.get().getErrorDescription());
        verify(messageProcessingErrorQueryRepositoryPort, times(2)).findById(1L);
    }
    
    @Test
    @DisplayName("Should handle large page numbers")
    void testFindAllWithLargePageNumber() {
        // Arrange
        Pageable largePageable = PageRequest.of(100, 10);
        Page<MessageProcessingError> emptyPage = new PageImpl<>(Arrays.asList(), largePageable, 0);
        when(messageProcessingErrorQueryRepositoryPort.findAll(largePageable))
            .thenReturn(emptyPage);
        
        // Act
        Page<MessageProcessingError> result = messageProcessingErrorQueryService.findAll(largePageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(messageProcessingErrorQueryRepositoryPort).findAll(largePageable);
    }
}
