package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.input.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IMessageProcessingErrorCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IMessageProcessingErrorQueryPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller.MessageProcessinErrorController;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.MessageProcessingErrorDtoResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.mapper.IMessageProcessingErrorResponseMapper;

/**
 * @brief Unit tests for MessageProcessinErrorController
 * 
 * Tests the REST controller for message processing error operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageProcessinErrorController Tests")
public class MessageProcessingErrorControllerUnitTest {
    
    @Mock
    private IMessageProcessingErrorQueryPort messageProcessingErrorQueryPort;
    
    @Mock
    private IMessageProcessingErrorCommandPort messageProcessingErrorCommandPort;
    
    @Mock
    private IMessageProcessingErrorResponseMapper messageProcessingErrorResponseMapper;
    
    @InjectMocks
    private MessageProcessinErrorController messageProcessinErrorController;
    
    private Long errorId;
    private MessageProcessingError messageProcessingError;
    private MessageProcessingErrorDtoResponse messageProcessingErrorDtoResponse;
    private Pageable pageable;
    private Page<MessageProcessingError> messageProcessingErrorPage;
    /* 
    @BeforeEach
    void setUp() {
        errorId = "1";
        
        // Setup message processing error
        messageProcessingError = new MessageProcessingError(
            errorId,
            "PURCHASE_EVENT",
            "Test error message",
            "{}",
            java.time.Instant.now(),
            "VALIDATION_ERROR"
        );
        
        // Setup DTO response
        messageProcessingErrorDtoResponse = new MessageProcessingErrorDtoResponse();
        messageProcessingErrorDtoResponse.setId(errorId);
        messageProcessingErrorDtoResponse.setErrorDescription("Test error message");
        messageProcessingErrorDtoResponse.setEntityType("VALIDATION_ERROR");
        
        // Setup pageable
        pageable = PageRequest.of(0, 10);
        
        // Setup page
        List<MessageProcessingError> errors = new ArrayList<>();
        errors.add(messageProcessingError);
        messageProcessingErrorPage = new PageImpl<>(errors, pageable, errors.size());
    }
    
    @Test
    @DisplayName("Should find message processing error by ID successfully")
    void testFindById_Success() {
        // Arrange
        when(messageProcessingErrorQueryPort.findById(errorId)).thenReturn(Optional.of(messageProcessingError));
        when(messageProcessingErrorResponseMapper.toDtoResponse(messageProcessingError))
            .thenReturn(messageProcessingErrorDtoResponse);
        
        // Act
        ResponseEntity<ResponseDTO<MessageProcessingErrorDtoResponse>> response = 
            messageProcessingErrorController.findById(errorId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<MessageProcessingErrorDtoResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Message processing error found successfully", body.getMessage());
        assertNotNull(body.getData());
        assertEquals(errorId, body.getData().getId());
        
        verify(messageProcessingErrorQueryPort, times(1)).findById(errorId);
        verify(messageProcessingErrorResponseMapper, times(1)).toDtoResponse(messageProcessingError);
    }
    
    @Test
    @DisplayName("Should find last record successfully")
    void testFindLastRecord_Success() {
        // Arrange
        when(messageProcessingErrorQueryPort.findLastRecord()).thenReturn(Optional.of(messageProcessingError));
        when(messageProcessingErrorResponseMapper.toDtoResponse(messageProcessingError))
            .thenReturn(messageProcessingErrorDtoResponse);
        
        // Act
        ResponseEntity<ResponseDTO<MessageProcessingErrorDtoResponse>> response = 
            messageProcessingErrorController.findLastRecord();
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<MessageProcessingErrorDtoResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Latest message processing error found successfully", body.getMessage());
        assertNotNull(body.getData());
        
        verify(messageProcessingErrorQueryPort, times(1)).findLastRecord();
        verify(messageProcessingErrorResponseMapper, times(1)).toDtoResponse(messageProcessingError);
    }
    
    @Test
    @DisplayName("Should find all paginated successfully")
    void testFindAllPaginated_Success() {
        // Arrange
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(messageProcessingErrorPage);
        when(messageProcessingErrorResponseMapper.toDtoResponse(any(MessageProcessingError.class)))
            .thenReturn(messageProcessingErrorDtoResponse);
        
        // Act
        ResponseEntity<ResponseDTO<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<Page<MessageProcessingErrorDtoResponse>> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Message processing errors retrieved successfully", body.getMessage());
        assertNotNull(body.getData());
        assertEquals(1, body.getData().getContent().size());
        assertEquals(1, body.getData().getTotalElements());
        
        verify(messageProcessingErrorQueryPort, times(1)).findAll(pageable);
    }
    
    @Test
    @DisplayName("Should delete by ID successfully")
    void testDeleteById_Success() {
        // Act
        ResponseEntity<ResponseDTO<Void>> response = 
            messageProcessingErrorController.deleteById(errorId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Message processing error deleted successfully", body.getMessage());
        assertNull(body.getData());
        
        verify(messageProcessingErrorCommandPort, times(1)).deleteById(errorId);
    }
    
    @Test
    @DisplayName("Should delete all successfully")
    void testDeleteAll_Success() {
        // Act
        ResponseEntity<ResponseDTO<Void>> response = 
            messageProcessingErrorController.deleteAll();
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("All message processing errors deleted successfully", body.getMessage());
        assertNull(body.getData());
        
        verify(messageProcessingErrorCommandPort, times(1)).deleteAll();
    }
    
    @Test
    @DisplayName("Should handle different error IDs for findById")
    void testFindById_DifferentIds() {
        // Arrange
        Long differentId = "999";
        MessageProcessingError differentError = new MessageProcessingError(
            differentId,
            "SALE_EVENT",
            "Different error",
            "{}",
            java.time.Instant.now(),
            "PROCESSING_ERROR"
        );
        
        when(messageProcessingErrorQueryPort.findById(differentId)).thenReturn(Optional.of(differentError));
        when(messageProcessingErrorResponseMapper.toDtoResponse(differentError))
            .thenReturn(messageProcessingErrorDtoResponse);
        
        // Act
        ResponseEntity<ResponseDTO<MessageProcessingErrorDtoResponse>> response = 
            messageProcessingErrorController.findById(differentId);
        
        // Assert
        assertNotNull(response);
        verify(messageProcessingErrorQueryPort, times(1)).findById(differentId);
    }
    
    @Test
    @DisplayName("Should handle empty page result")
    void testFindAllPaginated_EmptyPage() {
        // Arrange
        Page<MessageProcessingError> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(emptyPage);
        
        // Act
        ResponseEntity<ResponseDTO<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(pageable);
        
        // Assert
        assertNotNull(response);
        ResponseDTO<Page<MessageProcessingErrorDtoResponse>> body = response.getBody();
        assertNotNull(body);
        assertEquals(0, body.getData().getContent().size());
        assertEquals(0, body.getData().getTotalElements());
    }
    
    @Test
    @DisplayName("Should handle multiple errors in paginated result")
    void testFindAllPaginated_MultipleErrors() {
        // Arrange
        List<MessageProcessingError> errors = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            errors.add(new MessageProcessingError(
                (long) i,
                "EVENT_" + i,
                "Error " + i,
                "{}",
                java.time.Instant.now(),
                "ERROR_TYPE_" + i
            ));
        }
        Page<MessageProcessingError> multiPage = new PageImpl<>(errors, pageable, errors.size());
        
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(multiPage);
        when(messageProcessingErrorResponseMapper.toDtoResponse(any(MessageProcessingError.class)))
            .thenAnswer(invocation -> {
                MessageProcessingError error = invocation.getArgument(0);
                MessageProcessingErrorDtoResponse dto = new MessageProcessingErrorDtoResponse();
                dto.setId(error.getId());
                dto.setErrorDescription(error.getMessageData());
                return dto;
            });
        
        // Act
        ResponseEntity<ResponseDTO<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(5, response.getBody().getData().getContent().size());
        assertEquals(5, response.getBody().getData().getTotalElements());
    }
    
    @Test
    @DisplayName("Should handle different page sizes")
    void testFindAllPaginated_DifferentPageSize() {
        // Arrange
        Pageable largePageable = PageRequest.of(0, 50);
        Page<MessageProcessingError> largePage = new PageImpl<>(new ArrayList<>(), largePageable, 0);
        
        when(messageProcessingErrorQueryPort.findAll(largePageable)).thenReturn(largePage);
        
        // Act
        ResponseEntity<ResponseDTO<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(largePageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(50, response.getBody().getData().getSize());
    }
    
    @Test
    @DisplayName("Should handle different page numbers")
    void testFindAllPaginated_DifferentPageNumber() {
        // Arrange
        Pageable secondPage = PageRequest.of(1, 10);
        Page<MessageProcessingError> page2 = new PageImpl<>(new ArrayList<>(), secondPage, 0);
        
        when(messageProcessingErrorQueryPort.findAll(secondPage)).thenReturn(page2);
        
        // Act
        ResponseEntity<ResponseDTO<Page<MessageProcessingErrorDtoResponse>>> response = 
            messageProcessingErrorController.findAllPaginated(secondPage);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getBody().getData().getNumber());
    }
    
    @Test
    @DisplayName("Should delete different error IDs")
    void testDeleteById_DifferentIds() {
        // Arrange
        Long differentId = "999";
        
        // Act
        ResponseEntity<ResponseDTO<Void>> response = 
            messageProcessingErrorController.deleteById(differentId);
        
        // Assert
        assertNotNull(response);
        verify(messageProcessingErrorCommandPort, times(1)).deleteById(differentId);
    }
    
    @Test
    @DisplayName("Should verify mapper is called for each error in page")
    void testFindAllPaginated_MapperVerification() {
        // Arrange
        List<MessageProcessingError> errors = new ArrayList<>();
        errors.add(messageProcessingError);
        errors.add(messageProcessingError);
        errors.add(messageProcessingError);
        Page<MessageProcessingError> threePage = new PageImpl<>(errors, pageable, errors.size());
        
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(threePage);
        when(messageProcessingErrorResponseMapper.toDtoResponse(any(MessageProcessingError.class)))
            .thenReturn(messageProcessingErrorDtoResponse);
        
        // Act
        messageProcessingErrorController.findAllPaginated(pageable);
        
        // Assert
        verify(messageProcessingErrorResponseMapper, times(3)).toDtoResponse(any(MessageProcessingError.class));
    }
    
    @Test
    @DisplayName("Should find error by ID with different error types")
    void testFindById_DifferentErrorTypes() {
        // Arrange
        MessageProcessingError validationError = new MessageProcessingError(
            errorId,
            "VALIDATION_EVENT",
            "Validation failed",
            "{}",
            java.time.Instant.now(),
            "VALIDATION_ERROR"
        );
        
        when(messageProcessingErrorQueryPort.findById(errorId)).thenReturn(Optional.of(validationError));
        when(messageProcessingErrorResponseMapper.toDtoResponse(validationError))
            .thenReturn(messageProcessingErrorDtoResponse);
        
        // Act
        ResponseEntity<ResponseDTO<MessageProcessingErrorDtoResponse>> response = 
            messageProcessingErrorController.findById(errorId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody().getData());
    }
    
    @Test
    @DisplayName("Should verify correct messages for all operations")
    void testAllOperations_MessageVerification() {
        // Arrange
        when(messageProcessingErrorQueryPort.findById(errorId)).thenReturn(Optional.of(messageProcessingError));
        when(messageProcessingErrorQueryPort.findLastRecord()).thenReturn(Optional.of(messageProcessingError));
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(messageProcessingErrorPage);
        when(messageProcessingErrorResponseMapper.toDtoResponse(any())).thenReturn(messageProcessingErrorDtoResponse);
        
        // Act
        ResponseEntity<ResponseDTO<MessageProcessingErrorDtoResponse>> findByIdResponse = 
            messageProcessingErrorController.findById(errorId);
        ResponseEntity<ResponseDTO<MessageProcessingErrorDtoResponse>> findLastResponse = 
            messageProcessingErrorController.findLastRecord();
        ResponseEntity<ResponseDTO<Page<MessageProcessingErrorDtoResponse>>> findAllResponse = 
            messageProcessingErrorController.findAllPaginated(pageable);
        ResponseEntity<ResponseDTO<Void>> deleteByIdResponse = 
            messageProcessingErrorController.deleteById(errorId);
        ResponseEntity<ResponseDTO<Void>> deleteAllResponse = 
            messageProcessingErrorController.deleteAll();
        
        // Assert
        assertEquals("Message processing error found successfully", findByIdResponse.getBody().getMessage());
        assertEquals("Latest message processing error found successfully", findLastResponse.getBody().getMessage());
        assertEquals("Message processing errors retrieved successfully", findAllResponse.getBody().getMessage());
        assertEquals("Message processing error deleted successfully", deleteByIdResponse.getBody().getMessage());
        assertEquals("All message processing errors deleted successfully", deleteAllResponse.getBody().getMessage());
    }
    
    @Test
    @DisplayName("Should verify all operations return correct HTTP status")
    void testAllOperations_HttpStatusVerification() {
        // Arrange
        when(messageProcessingErrorQueryPort.findById(errorId)).thenReturn(Optional.of(messageProcessingError));
        when(messageProcessingErrorQueryPort.findLastRecord()).thenReturn(Optional.of(messageProcessingError));
        when(messageProcessingErrorQueryPort.findAll(pageable)).thenReturn(messageProcessingErrorPage);
        when(messageProcessingErrorResponseMapper.toDtoResponse(any())).thenReturn(messageProcessingErrorDtoResponse);
        
        // Act
        ResponseEntity<ResponseDTO<MessageProcessingErrorDtoResponse>> findByIdResponse = 
            messageProcessingErrorController.findById(errorId);
        ResponseEntity<ResponseDTO<MessageProcessingErrorDtoResponse>> findLastResponse = 
            messageProcessingErrorController.findLastRecord();
        ResponseEntity<ResponseDTO<Page<MessageProcessingErrorDtoResponse>>> findAllResponse = 
            messageProcessingErrorController.findAllPaginated(pageable);
        ResponseEntity<ResponseDTO<Void>> deleteByIdResponse = 
            messageProcessingErrorController.deleteById(errorId);
        ResponseEntity<ResponseDTO<Void>> deleteAllResponse = 
            messageProcessingErrorController.deleteAll();
        
        // Assert
        assertEquals(HttpStatus.OK, findByIdResponse.getStatusCode());
        assertEquals(HttpStatus.OK, findLastResponse.getStatusCode());
        assertEquals(HttpStatus.OK, findAllResponse.getStatusCode());
        assertEquals(HttpStatus.OK, deleteByIdResponse.getStatusCode());
        assertEquals(HttpStatus.OK, deleteAllResponse.getStatusCode());
        
        assertEquals(200, findByIdResponse.getBody().getStatus());
        assertEquals(200, findLastResponse.getBody().getStatus());
        assertEquals(200, findAllResponse.getBody().getStatus());
        assertEquals(200, deleteByIdResponse.getBody().getStatus());
        assertEquals(200, deleteAllResponse.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should handle findById and verify response structure")
    void testFindById_ResponseStructure() {
        // Arrange
        when(messageProcessingErrorQueryPort.findById(errorId)).thenReturn(Optional.of(messageProcessingError));
        when(messageProcessingErrorResponseMapper.toDtoResponse(messageProcessingError))
            .thenReturn(messageProcessingErrorDtoResponse);
        
        // Act
        ResponseEntity<ResponseDTO<MessageProcessingErrorDtoResponse>> response = 
            messageProcessingErrorController.findById(errorId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertNotNull(response.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should handle deleteById and verify void response")
    void testDeleteById_VoidResponse() {
        // Act
        ResponseEntity<ResponseDTO<Void>> response = 
            messageProcessingErrorController.deleteById(errorId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNull(response.getBody().getData());
        assertEquals(200, response.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should handle deleteAll and verify void response")
    void testDeleteAll_VoidResponse() {
        // Act
        ResponseEntity<ResponseDTO<Void>> response = 
            messageProcessingErrorController.deleteAll();
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNull(response.getBody().getData());
        assertEquals(200, response.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should verify query port is called correctly for findLastRecord")
    void testFindLastRecord_PortVerification() {
        // Arrange
        when(messageProcessingErrorQueryPort.findLastRecord()).thenReturn(Optional.of(messageProcessingError));
        when(messageProcessingErrorResponseMapper.toDtoResponse(any())).thenReturn(messageProcessingErrorDtoResponse);
        
        // Act
        messageProcessingErrorController.findLastRecord();
        
        // Assert
        verify(messageProcessingErrorQueryPort, times(1)).findLastRecord();
    }
    
    @Test
    @DisplayName("Should verify command port is called correctly for deleteAll")
    void testDeleteAll_PortVerification() {
        // Act
        messageProcessingErrorController.deleteAll();
        
        // Assert
        verify(messageProcessingErrorCommandPort, times(1)).deleteAll();
    }

    */
}
