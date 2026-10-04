package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.remoteSync.adapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;

import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.KardexBatchDTORequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized.GenericErrorException;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter.KardexExternalClientAdapter;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.config.IKardexExternalClient;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto.KardexExternalResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.mapper.IKardexExternalClientMapper;

/**
 * @brief Unit tests for KardexExternalClientAdapter
 * 
 * Tests the adapter for external kardex service communication.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KardexExternalClientAdapter Tests")
public class KardexExternalClientAdapterUnitTest {
    
    @Mock
    private IKardexExternalClientMapper kardexExternalClientMapper;
    
    @Mock
    private IKardexExternalClient kardexExternalClient;
    
    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;
    
    @InjectMocks
    private KardexExternalClientAdapter kardexExternalClientAdapter;
    
    private String enterpriseId;
    private KardexExternalResponseDTO mockResponse;
    private List<KardexBatchDTORequest> mockDTOList;
    private List<Kardex> mockKardexList;
    private Kardex mockKardex;
    private KardexBatchDTORequest mockDTO;
    
    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        
        // Setup mock DTO
        mockDTO = new KardexBatchDTORequest();
        mockDTO.setDetails("Test movement");
        mockDTO.setQuantity(10);
        mockDTO.setUnitPrice(new BigDecimal("100.00"));
        mockDTO.setFactCode("1");
        mockDTO.setProductId(1L);
        mockDTO.setType(MovementType.PURCHASE);
        
        mockDTOList = new ArrayList<>();
        mockDTOList.add(mockDTO);
        
        // Setup mock response
        mockResponse = new KardexExternalResponseDTO();
        mockResponse.setData(mockDTOList);
        mockResponse.setStatus(200);
        mockResponse.setMessage("Success");
        
        // Setup mock Kardex
        mockKardex = Kardex.builder()
                .idKardex(1L)
                .details("Test movement")
                .build();
        
        mockKardexList = new ArrayList<>();
        mockKardexList.add(mockKardex);
    }
    
    @Test
    @DisplayName("Should successfully retrieve kardex records when service returns data")
    void testFindKardexByEnterpriseId_Success() {
        // Arrange
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenReturn(mockResponse);
        when(kardexExternalClientMapper.toDomain(any(KardexBatchDTORequest.class))).thenReturn(mockKardex);
        
        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockKardex, result.get(0));
        verify(kardexExternalClient, times(1)).findKardexByEnterpriseId(enterpriseId);
        verify(kardexExternalClientMapper, times(1)).toDomain(mockDTO);
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }
    
    @Test
    @DisplayName("Should return empty list when response is null")
    void testFindKardexByEnterpriseId_NullResponse() {
        // Arrange
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenReturn(null);
        doThrow(new GenericErrorException(500, "No data received from external kardex service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());
        
        // Act & Assert
        GenericErrorException exception = assertThrows(GenericErrorException.class, 
            () -> kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId)
        );
        
        assertEquals("No data received from external kardex service", exception.getMessage());
        verify(kardexExternalClient, times(1)).findKardexByEnterpriseId(enterpriseId);
        verify(formatterResultOutputPort).returnErrorGenericResponse(500, "No data received from external kardex service");
    }
    
    @Test
    @DisplayName("Should return empty list when response data is null")
    void testFindKardexByEnterpriseId_NullResponseData() {
        // Arrange
        mockResponse.setData(null);
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenReturn(mockResponse);
        doThrow(new GenericErrorException(500, "No data received from external kardex service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());
        
        // Act & Assert
        GenericErrorException exception = assertThrows(GenericErrorException.class, 
            () -> kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId)
        );
        
        assertEquals("No data received from external kardex service", exception.getMessage());
        verify(kardexExternalClient, times(1)).findKardexByEnterpriseId(enterpriseId);
        verify(formatterResultOutputPort).returnErrorGenericResponse(500, "No data received from external kardex service");
    }
    
    @Test
    @DisplayName("Should handle multiple kardex records")
    void testFindKardexByEnterpriseId_MultipleRecords() {
        // Arrange
        KardexBatchDTORequest secondDTO = new KardexBatchDTORequest();
        secondDTO.setDetails("Second movement");
        secondDTO.setQuantity(20);
        secondDTO.setUnitPrice(new BigDecimal("200.00"));
        secondDTO.setFactCode("2");
        secondDTO.setProductId(2L);
        secondDTO.setType(MovementType.SALE);
        
        Kardex secondKardex = Kardex.builder()
                .idKardex(2L)
                .details("Second movement")
                .build();
        
        mockDTOList.add(secondDTO);
        
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenReturn(mockResponse);
        when(kardexExternalClientMapper.toDomain(mockDTO)).thenReturn(mockKardex);
        when(kardexExternalClientMapper.toDomain(secondDTO)).thenReturn(secondKardex);
        
        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockKardex, result.get(0));
        assertEquals(secondKardex, result.get(1));
        verify(kardexExternalClientMapper, times(2)).toDomain(any(KardexBatchDTORequest.class));
    }
    
    @Test
    @DisplayName("Should return empty list when response data is empty")
    void testFindKardexByEnterpriseId_EmptyResponseData() {
        // Arrange
        mockResponse.setData(new ArrayList<>());
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenReturn(mockResponse);
        
        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(kardexExternalClient, times(1)).findKardexByEnterpriseId(enterpriseId);
        verify(kardexExternalClientMapper, never()).toDomain(any(KardexBatchDTORequest.class));
    }
    
    @Test
    @DisplayName("Should handle ServiceUnavailable exception (503)")
    void testFindKardexByEnterpriseId_ServiceUnavailable() {
        // Arrange
        RestClientResponseException exception = 
            HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", null, null, null);
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenThrow(exception);
        doThrow(new GenericErrorException(503, "Kardex external service is unavailable"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());
        
        // Act & Assert
        GenericErrorException thrownException = assertThrows(GenericErrorException.class, 
            () -> kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId)
        );
        
        assertEquals("Kardex external service is unavailable", thrownException.getMessage());
        verify(formatterResultOutputPort).returnErrorGenericResponse(503, "Kardex external service is unavailable");
    }
    
    @Test
    @DisplayName("Should handle NotFound exception (404)")
    void testFindKardexByEnterpriseId_NotFound() {
        // Arrange
        RestClientResponseException exception = 
            HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenThrow(exception);
        doThrow(new GenericErrorException(404, "No kardex records found for the given enterprise ID"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());
        
        // Act & Assert
        GenericErrorException thrownException = assertThrows(GenericErrorException.class, 
            () -> kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId)
        );
        
        assertEquals("No kardex records found for the given enterprise ID", thrownException.getMessage());
        verify(formatterResultOutputPort).returnErrorGenericResponse(404, "No kardex records found for the given enterprise ID");
    }
    
    @Test
    @DisplayName("Should handle generic RestClientResponseException")
    void testFindKardexByEnterpriseId_WebClientException() {
        // Arrange
        RestClientResponseException exception = 
            HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, null, null);
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenThrow(exception);
        doThrow(new GenericErrorException(500, "Error communicating with kardex external service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());
        
        // Act & Assert
        GenericErrorException thrownException = assertThrows(GenericErrorException.class, 
            () -> kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId)
        );
        
        assertEquals("Error communicating with kardex external service", thrownException.getMessage());
        verify(formatterResultOutputPort).returnErrorGenericResponse(500, "Error communicating with kardex external service");
    }
    
    @Test
    @DisplayName("Should handle unexpected exceptions")
    void testFindKardexByEnterpriseId_UnexpectedException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error");
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenThrow(exception);
        doThrow(new GenericErrorException(500, "Unexpected error communicating with kardex external service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());
        
        // Act & Assert
        GenericErrorException thrownException = assertThrows(GenericErrorException.class, 
            () -> kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId)
        );
        
        assertEquals("Unexpected error communicating with kardex external service", thrownException.getMessage());
        verify(formatterResultOutputPort).returnErrorGenericResponse(500, "Unexpected error communicating with kardex external service");
    }
    
    @Test
    @DisplayName("Should handle BadRequest exception (400)")
    void testFindKardexByEnterpriseId_BadRequest() {
        // Arrange
        RestClientResponseException exception = 
            HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null);
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenThrow(exception);
        doThrow(new GenericErrorException(500, "Error communicating with kardex external service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());
        
        // Act & Assert
        GenericErrorException thrownException = assertThrows(GenericErrorException.class, 
            () -> kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId)
        );
        
        assertEquals("Error communicating with kardex external service", thrownException.getMessage());
        verify(formatterResultOutputPort).returnErrorGenericResponse(500, "Error communicating with kardex external service");
    }
    
    @Test
    @DisplayName("Should handle different enterprise IDs")
    void testFindKardexByEnterpriseId_DifferentEnterpriseIds() {
        // Arrange
        String differentEnterpriseId = "ENT-999";
        when(kardexExternalClient.findKardexByEnterpriseId(differentEnterpriseId)).thenReturn(mockResponse);
        when(kardexExternalClientMapper.toDomain(any(KardexBatchDTORequest.class))).thenReturn(mockKardex);
        
        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(differentEnterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(kardexExternalClient, times(1)).findKardexByEnterpriseId(differentEnterpriseId);
    }
    
    @Test
    @DisplayName("Should map all DTOs to domain objects correctly")
    void testFindKardexByEnterpriseId_MappingVerification() {
        // Arrange
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenReturn(mockResponse);
        when(kardexExternalClientMapper.toDomain(mockDTO)).thenReturn(mockKardex);
        
        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(kardexExternalClientMapper, times(1)).toDomain(mockDTO);
        assertEquals(mockKardex, result.get(0));
    }
    
    @Test
    @DisplayName("Should handle empty enterprise ID")
    void testFindKardexByEnterpriseId_EmptyEnterpriseId() {
        // Arrange
        String emptyId = "";
        when(kardexExternalClient.findKardexByEnterpriseId(emptyId)).thenReturn(mockResponse);
        when(kardexExternalClientMapper.toDomain(any(KardexBatchDTORequest.class))).thenReturn(mockKardex);
        
        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(emptyId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(kardexExternalClient, times(1)).findKardexByEnterpriseId(emptyId);
    }
    
    @Test
    @DisplayName("Should handle null enterprise ID gracefully")
    void testFindKardexByEnterpriseId_NullEnterpriseId() {
        // Arrange
        when(kardexExternalClient.findKardexByEnterpriseId(null)).thenReturn(mockResponse);
        when(kardexExternalClientMapper.toDomain(any(KardexBatchDTORequest.class))).thenReturn(mockKardex);
        
        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(kardexExternalClient, times(1)).findKardexByEnterpriseId(null);
    }
    
    @Test
    @DisplayName("Should handle large number of kardex records")
    void testFindKardexByEnterpriseId_LargeDataSet() {
        // Arrange
        List<KardexBatchDTORequest> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            KardexBatchDTORequest dto = new KardexBatchDTORequest();
            dto.setDetails("Movement " + i);
            dto.setQuantity(i);
            dto.setUnitPrice(new BigDecimal(i * 10));
            dto.setFactCode(String.valueOf(i));
            dto.setProductId((long) i);
            dto.setType(i % 2 == 0 ? MovementType.PURCHASE : MovementType.SALE);
            largeList.add(dto);
        }
        
        mockResponse.setData(largeList);
        when(kardexExternalClient.findKardexByEnterpriseId(enterpriseId)).thenReturn(mockResponse);
        when(kardexExternalClientMapper.toDomain(any(KardexBatchDTORequest.class))).thenReturn(mockKardex);
        
        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(100, result.size());
        verify(kardexExternalClientMapper, times(100)).toDomain(any(KardexBatchDTORequest.class));
    }
}

