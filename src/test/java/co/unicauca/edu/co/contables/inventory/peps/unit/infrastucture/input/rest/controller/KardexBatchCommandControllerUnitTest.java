package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.input.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexBatchCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller.KardexBatchCommandController;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexBatchErrorDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexBatchProcessingResultDTO;

/**
 * @brief Unit tests for KardexBatchCommandController
 * 
 * Tests the REST controller for batch kardex operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KardexBatchCommandController Tests")
public class KardexBatchCommandControllerUnitTest {
    
    @Mock
    private IKardexBatchCommandPort kardexBatchCommandPort;
    
    @InjectMocks
    private KardexBatchCommandController kardexBatchCommandController;
    
    private String enterpriseId;
    private KardexBatchProcessingResultDTO successResult;
    private KardexBatchProcessingResultDTO partialSuccessResult;
    private KardexBatchProcessingResultDTO failureResult;
    
    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        
        // Success result - all records processed
        successResult = KardexBatchProcessingResultDTO.builder()
                .totalRecords(10)
                .processedRecords(10)
                .failedRecords(0)
                .success(true)
                .errors(new ArrayList<>())
                .build();
        
        // Partial success result - some records failed
        List<KardexBatchErrorDTO> errors = new ArrayList<>();
        errors.add(new KardexBatchErrorDTO());
        
        partialSuccessResult = KardexBatchProcessingResultDTO.builder()
                .totalRecords(10)
                .processedRecords(8)
                .failedRecords(2)
                .success(false)
                .errors(errors)
                .build();
        
        // Failure result - all records failed
        List<KardexBatchErrorDTO> manyErrors = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            manyErrors.add(new KardexBatchErrorDTO());
        }
        
        failureResult = KardexBatchProcessingResultDTO.builder()
                .totalRecords(5)
                .processedRecords(0)
                .failedRecords(5)
                .success(false)
                .errors(manyErrors)
                .build();
    }
    
    @Test
    @DisplayName("Should process batch successfully when all records are processed")
    void testProcessBatchForEnterprise_AllRecordsProcessed() {
        // Arrange
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(successResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<KardexBatchProcessingResultDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Batch processing completed. Processed: 10/10 records", body.getMessage());
        
        KardexBatchProcessingResultDTO data = body.getData();
        assertNotNull(data);
        assertEquals(10, data.getTotalRecords());
        assertEquals(10, data.getProcessedRecords());
        assertEquals(0, data.getFailedRecords());
        assertTrue(data.isSuccess());
        
        verify(kardexBatchCommandPort, times(1)).processBatchFromExternalService(enterpriseId);
    }
    
    @Test
    @DisplayName("Should process batch with partial success when some records fail")
    void testProcessBatchForEnterprise_PartialSuccess() {
        // Arrange
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(partialSuccessResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<KardexBatchProcessingResultDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Batch processing completed. Processed: 8/10 records", body.getMessage());
        
        KardexBatchProcessingResultDTO data = body.getData();
        assertNotNull(data);
        assertEquals(10, data.getTotalRecords());
        assertEquals(8, data.getProcessedRecords());
        assertEquals(2, data.getFailedRecords());
        
        verify(kardexBatchCommandPort, times(1)).processBatchFromExternalService(enterpriseId);
    }
    
    @Test
    @DisplayName("Should return result when all records fail")
    void testProcessBatchForEnterprise_AllRecordsFailed() {
        // Arrange
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(failureResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<KardexBatchProcessingResultDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Batch processing completed. Processed: 0/5 records", body.getMessage());
        
        KardexBatchProcessingResultDTO data = body.getData();
        assertNotNull(data);
        assertEquals(5, data.getTotalRecords());
        assertEquals(0, data.getProcessedRecords());
        assertEquals(5, data.getFailedRecords());
        
        verify(kardexBatchCommandPort, times(1)).processBatchFromExternalService(enterpriseId);
    }
    
    @Test
    @DisplayName("Should process batch for different enterprise IDs")
    void testProcessBatchForEnterprise_DifferentEnterpriseIds() {
        // Arrange
        String enterpriseId1 = "ENT-001";
        String enterpriseId2 = "ENT-999";
        
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId1)).thenReturn(successResult);
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId2)).thenReturn(partialSuccessResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response1 = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId1);
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response2 = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId2);
        
        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(10, response1.getBody().getData().getProcessedRecords());
        assertEquals(8, response2.getBody().getData().getProcessedRecords());
        
        verify(kardexBatchCommandPort, times(1)).processBatchFromExternalService(enterpriseId1);
        verify(kardexBatchCommandPort, times(1)).processBatchFromExternalService(enterpriseId2);
    }
    
    @Test
    @DisplayName("Should handle empty batch (zero records)")
    void testProcessBatchForEnterprise_EmptyBatch() {
        // Arrange
        KardexBatchProcessingResultDTO emptyResult = KardexBatchProcessingResultDTO.builder()
                .totalRecords(0)
                .processedRecords(0)
                .failedRecords(0)
                .success(true)
                .errors(new ArrayList<>())
                .build();
        
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(emptyResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<KardexBatchProcessingResultDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals("Batch processing completed. Processed: 0/0 records", body.getMessage());
        assertEquals(0, body.getData().getTotalRecords());
    }
    
    @Test
    @DisplayName("Should handle large batch (many records)")
    void testProcessBatchForEnterprise_LargeBatch() {
        // Arrange
        KardexBatchProcessingResultDTO largeResult = KardexBatchProcessingResultDTO.builder()
                .totalRecords(1000)
                .processedRecords(995)
                .failedRecords(5)
                .success(false)
                .errors(new ArrayList<>())
                .build();
        
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(largeResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<KardexBatchProcessingResultDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals("Batch processing completed. Processed: 995/1000 records", body.getMessage());
        assertEquals(1000, body.getData().getTotalRecords());
        assertEquals(995, body.getData().getProcessedRecords());
    }
    
    @Test
    @DisplayName("Should format message correctly with single record")
    void testProcessBatchForEnterprise_SingleRecord() {
        // Arrange
        KardexBatchProcessingResultDTO singleResult = KardexBatchProcessingResultDTO.builder()
                .totalRecords(1)
                .processedRecords(1)
                .failedRecords(0)
                .success(true)
                .errors(new ArrayList<>())
                .build();
        
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(singleResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        ResponseDTO<KardexBatchProcessingResultDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals("Batch processing completed. Processed: 1/1 records", body.getMessage());
    }
    
    @Test
    @DisplayName("Should verify response structure")
    void testProcessBatchForEnterprise_ResponseStructure() {
        // Arrange
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(successResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertEquals(Integer.valueOf(200), response.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should call service exactly once per request")
    void testProcessBatchForEnterprise_ServiceCallVerification() {
        // Arrange
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(successResult);
        
        // Act
        kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        verify(kardexBatchCommandPort, times(1)).processBatchFromExternalService(enterpriseId);
    }
    
    @Test
    @DisplayName("Should handle enterprise ID with special characters")
    void testProcessBatchForEnterprise_SpecialCharacters() {
        // Arrange
        String specialId = "ENT-001-ABC_123";
        when(kardexBatchCommandPort.processBatchFromExternalService(specialId)).thenReturn(successResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(specialId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexBatchCommandPort, times(1)).processBatchFromExternalService(specialId);
    }
    
    @Test
    @DisplayName("Should handle consecutive requests")
    void testProcessBatchForEnterprise_ConsecutiveRequests() {
        // Arrange
        when(kardexBatchCommandPort.processBatchFromExternalService(anyString())).thenReturn(successResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response1 = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response2 = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response3 = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        verify(kardexBatchCommandPort, times(3)).processBatchFromExternalService(enterpriseId);
    }
    
    @Test
    @DisplayName("Should return HTTP 200 even when processing fails")
    void testProcessBatchForEnterprise_ReturnsOkOnFailure() {
        // Arrange
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(failureResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should include all error details in response")
    void testProcessBatchForEnterprise_ErrorDetailsIncluded() {
        // Arrange
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(partialSuccessResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response.getBody().getData().getErrors());
        assertEquals(1, response.getBody().getData().getErrors().size());
    }
    
    @Test
    @DisplayName("Should maintain error list when all records fail")
    void testProcessBatchForEnterprise_AllErrorsPreserved() {
        // Arrange
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(failureResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response.getBody().getData().getErrors());
        assertEquals(5, response.getBody().getData().getErrors().size());
    }
    
    @Test
    @DisplayName("Should handle numeric enterprise IDs")
    void testProcessBatchForEnterprise_NumericEnterpriseId() {
        // Arrange
        String numericId = "12345";
        when(kardexBatchCommandPort.processBatchFromExternalService(numericId)).thenReturn(successResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(numericId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexBatchCommandPort, times(1)).processBatchFromExternalService(numericId);
    }
    
    @Test
    @DisplayName("Should handle UUID format enterprise IDs")
    void testProcessBatchForEnterprise_UuidEnterpriseId() {
        // Arrange
        String uuidId = "550e8400-e29b-41d4-a716-446655440000";
        when(kardexBatchCommandPort.processBatchFromExternalService(uuidId)).thenReturn(successResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(uuidId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexBatchCommandPort, times(1)).processBatchFromExternalService(uuidId);
    }
    
    @Test
    @DisplayName("Should verify message format is correct")
    void testProcessBatchForEnterprise_MessageFormat() {
        // Arrange
        KardexBatchProcessingResultDTO customResult = KardexBatchProcessingResultDTO.builder()
                .totalRecords(100)
                .processedRecords(75)
                .failedRecords(25)
                .success(false)
                .errors(new ArrayList<>())
                .build();
        
        when(kardexBatchCommandPort.processBatchFromExternalService(enterpriseId)).thenReturn(customResult);
        
        // Act
        ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> response = 
            kardexBatchCommandController.processBatchForEnterprise(enterpriseId);
        
        // Assert
        String expectedMessage = "Batch processing completed. Processed: 75/100 records";
        assertEquals(expectedMessage, response.getBody().getMessage());
    }
}

