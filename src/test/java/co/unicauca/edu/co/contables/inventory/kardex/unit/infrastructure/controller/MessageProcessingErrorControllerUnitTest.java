package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.*;
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
import org.springframework.http.ResponseEntity;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.messageProcessingError.IMessageProcessingErrorCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.messageProcessingError.IMessageProcessingErrorQueryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller.MessageProcessingErrorController;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.MessageProcessingErrorDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper.IMessageProcessingErrorResponseMapper;

@ExtendWith(MockitoExtension.class)
public class MessageProcessingErrorControllerUnitTest {
    
    @Mock
    private IMessageProcessingErrorQueryPort messageProcessingErrorQueryPort;
    
    @Mock
    private IMessageProcessingErrorCommandPort messageProcessingErrorCommandPort;
    
    @Mock
    private IMessageProcessingErrorResponseMapper messageProcessingErrorResponseMapper;
    
    @InjectMocks
    private MessageProcessingErrorController messageProcessingErrorController;
    
    private MessageProcessingError error1;
    private MessageProcessingError error2;
    private MessageProcessingErrorDtoResponse errorResponse1;
    private MessageProcessingErrorDtoResponse errorResponse2;
    private Pageable pageable;
    
    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        
        // Setup error 1
        error1 = new MessageProcessingError();
        error1.setId(1L);
        error1.setEventType("PRODUCT_CREATED");
        error1.setErrorDescription("Failed to process product creation message");
        error1.setMessageData("{\"productId\": 100, \"name\": \"Product A\"}");
        error1.setErrorTimestamp(Instant.now());
        error1.setEntityType("Product");
        
        // Setup error 2
        error2 = new MessageProcessingError();
        error2.setId(2L);
        error2.setEventType("PRODUCT_UPDATED");
        error2.setErrorDescription("Failed to process product update message");
        error2.setMessageData("{\"productId\": 200, \"name\": \"Product B\"}");
        error2.setErrorTimestamp(Instant.now());
        error2.setEntityType("Product");
        
        // Setup response 1
        errorResponse1 = new MessageProcessingErrorDtoResponse();
        errorResponse1.setId(1L);
        errorResponse1.setEventType("PRODUCT_CREATED");
        errorResponse1.setErrorDescription("Failed to process product creation message");
        errorResponse1.setMessageData("{\"productId\": 100, \"name\": \"Product A\"}");
        errorResponse1.setErrorTimestamp(error1.getErrorTimestamp());
        errorResponse1.setEntityType("Product");
        
        // Setup response 2
        errorResponse2 = new MessageProcessingErrorDtoResponse();
        errorResponse2.setId(2L);
        errorResponse2.setEventType("PRODUCT_UPDATED");
        errorResponse2.setErrorDescription("Failed to process product update message");
        errorResponse2.setMessageData("{\"productId\": 200, \"name\": \"Product B\"}");
        errorResponse2.setErrorTimestamp(error2.getErrorTimestamp());
        errorResponse2.setEntityType("Product");
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should find message processing error by ID successfully")
    void testFindById() {
        // Arrange
        Long errorId = 1L;
        when(messageProcessingErrorQueryPort.findById(errorId)).thenReturn(Optional.of(error1));
        when(messageProcessingErrorResponseMapper.toDtoResponse(error1)).thenReturn(errorResponse1);
        
        // Act
        ResponseEntity<ResponseDto<MessageProcessingErrorDtoResponse>> response = 
            messageProcessingErrorController.findById(errorId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Message processing error found successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(1L, response.getBody().getData().getId());
        assertEquals("PRODUCT_CREATED", response.getBody().getData().getEventType());
        assertEquals("Product", response.getBody().getData().getEntityType());
        
        verify(messageProcessingErrorQueryPort).findById(errorId);
        verify(messageProcessingErrorResponseMapper).toDtoResponse(error1);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should find last message processing error successfully")
    void testFindLastRecord() {
        // Arrange
        when(messageProcessingErrorQueryPort.findLastRecord()).thenReturn(Optional.of(error2));
        when(messageProcessingErrorResponseMapper.toDtoResponse(error2)).thenReturn(errorResponse2);
        
        // Act
        ResponseEntity<ResponseDto<MessageProcessingErrorDtoResponse>> response = 
            messageProcessingErrorController.findLastRecord();
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Latest message processing error found successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(2L, response.getBody().getData().getId());
        assertEquals("PRODUCT_UPDATED", response.getBody().getData().getEventType());
        
        verify(messageProcessingErrorQueryPort).findLastRecord();
        verify(messageProcessingErrorResponseMapper).toDtoResponse(error2);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should find all paginated message processing errors successfully")
    void testFindAllPaginated() {
        // Arrange
        List<MessageProcessingError> errorList = Arrays.asList(error1, error2);
        Page<MessageProcessingError> errorPage = new PageImpl<>(errorList, pageable, errorList.size());
        
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(errorPage);
        when(messageProcessingErrorResponseMapper.toDtoResponse(error1)).thenReturn(errorResponse1);
        when(messageProcessingErrorResponseMapper.toDtoResponse(error2)).thenReturn(errorResponse2);
        
        // Act
        ResponseEntity<ResponseDto<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(pageable);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Message processing errors retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().getTotalElements());
        assertEquals(2, response.getBody().getData().getContent().size());
        
        verify(messageProcessingErrorQueryPort).findAll(pageable);
        verify(messageProcessingErrorResponseMapper, times(2)).toDtoResponse(any(MessageProcessingError.class));
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should return empty page when no errors found")
    void testFindAllPaginatedWithNoRecords() {
        // Arrange
        Page<MessageProcessingError> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(emptyPage);
        
        // Act
        ResponseEntity<ResponseDto<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(pageable);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals(0, response.getBody().getData().getTotalElements());
        assertTrue(response.getBody().getData().getContent().isEmpty());
        
        verify(messageProcessingErrorQueryPort).findAll(pageable);
        verify(messageProcessingErrorResponseMapper, never()).toDtoResponse(any());
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle pagination correctly")
    void testFindAllPaginatedWithCustomPageable() {
        // Arrange
        Pageable customPageable = PageRequest.of(1, 5);
        List<MessageProcessingError> errorList = Arrays.asList(error1);
        Page<MessageProcessingError> errorPage = new PageImpl<>(errorList, customPageable, 10);
        
        when(messageProcessingErrorQueryPort.findAll(customPageable)).thenReturn(errorPage);
        when(messageProcessingErrorResponseMapper.toDtoResponse(error1)).thenReturn(errorResponse1);
        
        // Act
        ResponseEntity<ResponseDto<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(customPageable);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(10, response.getBody().getData().getTotalElements());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals(1, response.getBody().getData().getNumber());
        
        verify(messageProcessingErrorQueryPort).findAll(customPageable);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should delete message processing error by ID successfully")
    void testDeleteById() {
        // Arrange
        Long errorId = 1L;
        doNothing().when(messageProcessingErrorCommandPort).deleteById(errorId);
        
        // Act
        ResponseEntity<ResponseDto<Void>> response = 
            messageProcessingErrorController.deleteById(errorId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Message processing error deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        
        verify(messageProcessingErrorCommandPort).deleteById(errorId);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should delete all message processing errors successfully")
    void testDeleteAll() {
        // Arrange
        doNothing().when(messageProcessingErrorCommandPort).deleteAll();
        
        // Act
        ResponseEntity<ResponseDto<Void>> response = 
            messageProcessingErrorController.deleteAll();
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("All message processing errors deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        
        verify(messageProcessingErrorCommandPort).deleteAll();
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify findById response structure")
    void testFindByIdResponseStructure() {
        // Arrange
        Long errorId = 1L;
        when(messageProcessingErrorQueryPort.findById(errorId)).thenReturn(Optional.of(error1));
        when(messageProcessingErrorResponseMapper.toDtoResponse(error1)).thenReturn(errorResponse1);
        
        // Act
        ResponseEntity<ResponseDto<MessageProcessingErrorDtoResponse>> response = 
            messageProcessingErrorController.findById(errorId);
        
        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getStatus() > 0);
        assertTrue(response.getBody().getData() instanceof MessageProcessingErrorDtoResponse);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify findLastRecord response structure")
    void testFindLastRecordResponseStructure() {
        // Arrange
        when(messageProcessingErrorQueryPort.findLastRecord()).thenReturn(Optional.of(error1));
        when(messageProcessingErrorResponseMapper.toDtoResponse(error1)).thenReturn(errorResponse1);
        
        // Act
        ResponseEntity<ResponseDto<MessageProcessingErrorDtoResponse>> response = 
            messageProcessingErrorController.findLastRecord();
        
        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getStatus() > 0);
        assertTrue(response.getBody().getData() instanceof MessageProcessingErrorDtoResponse);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify findAllPaginated response structure")
    void testFindAllPaginatedResponseStructure() {
        // Arrange
        Page<MessageProcessingError> errorPage = new PageImpl<>(Arrays.asList(error1), pageable, 1);
        
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(errorPage);
        when(messageProcessingErrorResponseMapper.toDtoResponse(error1)).thenReturn(errorResponse1);
        
        // Act
        ResponseEntity<ResponseDto<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(pageable);
        
        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getStatus() > 0);
        assertTrue(response.getBody().getData() instanceof Page);
    }
    
    @Test
    @DisplayName("Should map error entities to response DTOs correctly")
    void testFindAllPaginatedMapping() {
        // Arrange
        List<MessageProcessingError> errorList = Arrays.asList(error1, error2);
        Page<MessageProcessingError> errorPage = new PageImpl<>(errorList, pageable, errorList.size());
        
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(errorPage);
        when(messageProcessingErrorResponseMapper.toDtoResponse(error1)).thenReturn(errorResponse1);
        when(messageProcessingErrorResponseMapper.toDtoResponse(error2)).thenReturn(errorResponse2);
        
        // Act
        ResponseEntity<ResponseDto<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(pageable);
        
        // Assert
        @SuppressWarnings("null")
        List<MessageProcessingErrorDtoResponse> content = response.getBody().getData().getContent();
        assertEquals(2, content.size());
        assertEquals(1L, content.get(0).getId());
        assertEquals(2L, content.get(1).getId());
        assertEquals("PRODUCT_CREATED", content.get(0).getEventType());
        assertEquals("PRODUCT_UPDATED", content.get(1).getEventType());
    }
    
    @Test
    @DisplayName("Should verify mapper interactions for findById")
    void testFindByIdMapperInteractions() {
        // Arrange
        Long errorId = 1L;
        when(messageProcessingErrorQueryPort.findById(errorId)).thenReturn(Optional.of(error1));
        when(messageProcessingErrorResponseMapper.toDtoResponse(error1)).thenReturn(errorResponse1);
        
        // Act
        messageProcessingErrorController.findById(errorId);
        
        // Assert
        verify(messageProcessingErrorResponseMapper, times(1)).toDtoResponse(error1);
        verifyNoMoreInteractions(messageProcessingErrorResponseMapper);
    }
    
    @Test
    @DisplayName("Should verify mapper interactions for findLastRecord")
    void testFindLastRecordMapperInteractions() {
        // Arrange
        when(messageProcessingErrorQueryPort.findLastRecord()).thenReturn(Optional.of(error2));
        when(messageProcessingErrorResponseMapper.toDtoResponse(error2)).thenReturn(errorResponse2);
        
        // Act
        messageProcessingErrorController.findLastRecord();
        
        // Assert
        verify(messageProcessingErrorResponseMapper, times(1)).toDtoResponse(error2);
        verifyNoMoreInteractions(messageProcessingErrorResponseMapper);
    }
    
    @Test
    @DisplayName("Should verify deleteById is called only once")
    void testDeleteByIdVerifyOnce() {
        // Arrange
        Long errorId = 1L;
        doNothing().when(messageProcessingErrorCommandPort).deleteById(errorId);
        
        // Act
        messageProcessingErrorController.deleteById(errorId);
        
        // Assert
        verify(messageProcessingErrorCommandPort, times(1)).deleteById(errorId);
        verifyNoMoreInteractions(messageProcessingErrorCommandPort);
    }
    
    @Test
    @DisplayName("Should verify deleteAll is called only once")
    void testDeleteAllVerifyOnce() {
        // Arrange
        doNothing().when(messageProcessingErrorCommandPort).deleteAll();
        
        // Act
        messageProcessingErrorController.deleteAll();
        
        // Assert
        verify(messageProcessingErrorCommandPort, times(1)).deleteAll();
        verifyNoMoreInteractions(messageProcessingErrorCommandPort);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle error with all fields populated")
    void testFindByIdWithAllFields() {
        // Arrange
        Long errorId = 1L;
        error1.setMessageData("{\"productId\": 100, \"name\": \"Product A\", \"price\": 99.99}");
        errorResponse1.setMessageData(error1.getMessageData());
        
        when(messageProcessingErrorQueryPort.findById(errorId)).thenReturn(Optional.of(error1));
        when(messageProcessingErrorResponseMapper.toDtoResponse(error1)).thenReturn(errorResponse1);
        
        // Act
        ResponseEntity<ResponseDto<MessageProcessingErrorDtoResponse>> response = 
            messageProcessingErrorController.findById(errorId);
        
        // Assert
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getData().getMessageData());
        assertNotNull(response.getBody().getData().getErrorDescription());
        assertNotNull(response.getBody().getData().getEventType());
        assertNotNull(response.getBody().getData().getEntityType());
        
        verify(messageProcessingErrorQueryPort).findById(errorId);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle different event types")
    void testFindAllPaginatedWithDifferentEventTypes() {
        // Arrange
        MessageProcessingError error3 = new MessageProcessingError();
        error3.setId(3L);
        error3.setEventType("PRODUCT_DELETED");
        error3.setErrorDescription("Failed to process deletion");
        error3.setEntityType("Product");
        
        MessageProcessingErrorDtoResponse errorResponse3 = new MessageProcessingErrorDtoResponse();
        errorResponse3.setId(3L);
        errorResponse3.setEventType("PRODUCT_DELETED");
        
        List<MessageProcessingError> errorList = Arrays.asList(error1, error2, error3);
        Page<MessageProcessingError> errorPage = new PageImpl<>(errorList, pageable, errorList.size());
        
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(errorPage);
        when(messageProcessingErrorResponseMapper.toDtoResponse(error1)).thenReturn(errorResponse1);
        when(messageProcessingErrorResponseMapper.toDtoResponse(error2)).thenReturn(errorResponse2);
        when(messageProcessingErrorResponseMapper.toDtoResponse(error3)).thenReturn(errorResponse3);
        
        // Act
        ResponseEntity<ResponseDto<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(pageable);
        
        // Assert
        assertEquals(3, response.getBody().getData().getContent().size());
        verify(messageProcessingErrorQueryPort).findAll(pageable);
        verify(messageProcessingErrorResponseMapper, times(3)).toDtoResponse(any(MessageProcessingError.class));
    }
}
