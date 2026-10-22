package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.input.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller.KardexPEPSCommandController;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.AdjustmentEntryDTORequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.AdjustmentExitDTORequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.AdjustmentEntryDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.AdjustmentExitDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.mapper.IKardexRestMapper;

/**
 * @brief Unit tests for KardexPEPSCommandController
 * 
 * Tests the REST controller for kardex PEPS command operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KardexPEPSCommandController Tests")
public class KardexPEPSCommandControllerUnitTest {
    
    @Mock
    private IKardexCommandPort kardexCommandPort;
    
    @Mock
    private IKardexRestMapper kardexRestMapper;
    
    @InjectMocks
    private KardexPEPSCommandController kardexPEPSCommandController;
    
    private AdjustmentEntryDTORequest entryRequest;
    private AdjustmentExitDTORequest exitRequest;
    private Kardex mockKardex;
    private AdjustmentEntryDTOResponse entryResponse;
    private AdjustmentExitDTOResponse exitResponse;
    
    @BeforeEach
    void setUp() {
        // Setup entry request
        entryRequest = new AdjustmentEntryDTORequest();
        entryRequest.setDetails("Purchase adjustment");
        entryRequest.setQuantity(10);
        entryRequest.setUnitPrice(new BigDecimal("100.00"));
        entryRequest.setProductId(1L);
        entryRequest.setDate(ZonedDateTime.now());
        
        // Setup exit request
        exitRequest = new AdjustmentExitDTORequest();
        exitRequest.setDetails("Sale adjustment");
        exitRequest.setQuantity(5);
        exitRequest.setProductId(1L);
        exitRequest.setDate(ZonedDateTime.now());
        
        // Setup mock Kardex
        mockKardex = Kardex.builder()
                .idKardex(1L)
                .details("Test kardex")
                .build();
        
        // Setup entry response
        entryResponse = new AdjustmentEntryDTOResponse();
        entryResponse.setDetails("Purchase adjustment");
        entryResponse.setQuantity(10);
        
        // Setup exit response
        exitResponse = new AdjustmentExitDTOResponse();
        exitResponse.setDetails("Sale adjustment");
        exitResponse.setQuantity(5);
    }
    
    @Test
    @DisplayName("Should register purchase adjustment successfully")
    void testAdjustmentEntry_Success() {
        // Arrange
        when(kardexRestMapper.toDomain(entryRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentEntry(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponse(mockKardex)).thenReturn(entryResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentEntry(entryRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<AdjustmentEntryDTOResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("kardex purchase registered sucesfully", body.getMessage());
        
        AdjustmentEntryDTOResponse data = body.getData();
        assertNotNull(data);
        assertEquals("Purchase adjustment", data.getDetails());
        assertEquals(10, data.getQuantity());
        
        verify(kardexRestMapper, times(1)).toDomain(entryRequest);
        verify(kardexCommandPort, times(1)).registerAdjustmentEntry(mockKardex);
        verify(kardexRestMapper, times(1)).toDTOResponse(mockKardex);
    }
    
    @Test
    @DisplayName("Should register sale adjustment successfully")
    void testAdjustmentExit_Success() {
        // Arrange
        when(kardexRestMapper.toDomain(exitRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentExit(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponseSale(mockKardex)).thenReturn(exitResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentExitDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentExit(exitRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<AdjustmentExitDTOResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("kardex sale registered sucesfullly", body.getMessage());
        
        AdjustmentExitDTOResponse data = body.getData();
        assertNotNull(data);
        assertEquals("Sale adjustment", data.getDetails());
        assertEquals(5, data.getQuantity());
        
        verify(kardexRestMapper, times(1)).toDomain(exitRequest);
        verify(kardexCommandPort, times(1)).registerAdjustmentExit(mockKardex);
        verify(kardexRestMapper, times(1)).toDTOResponseSale(mockKardex);
    }
    
    @Test
    @DisplayName("Should delete all kardex records successfully")
    void testDeleteAllKardex_Success() {
        // Arrange
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        ResponseEntity<ResponseDTO<Void>> response = kardexPEPSCommandController.deleteAllKardex();
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("All kardex records deleted successfully", body.getMessage());
        assertNull(body.getData());
        
        verify(kardexCommandPort, times(1)).deleteAll();
    }
    
    @Test
    @DisplayName("Should handle purchase adjustment with minimum values")
    void testAdjustmentEntry_MinimumValues() {
        // Arrange
        AdjustmentEntryDTORequest minRequest = new AdjustmentEntryDTORequest();
        minRequest.setDetails("Min purchase");
        minRequest.setQuantity(1);
        minRequest.setUnitPrice(new BigDecimal("0.01"));
        minRequest.setProductId(1L);
        
        when(kardexRestMapper.toDomain(minRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentEntry(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponse(mockKardex)).thenReturn(entryResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentEntry(minRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexCommandPort, times(1)).registerAdjustmentEntry(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle purchase adjustment with large values")
    void testAdjustmentEntry_LargeValues() {
        // Arrange
        AdjustmentEntryDTORequest largeRequest = new AdjustmentEntryDTORequest();
        largeRequest.setDetails("Large purchase");
        largeRequest.setQuantity(10000);
        largeRequest.setUnitPrice(new BigDecimal("99999.99"));
        largeRequest.setProductId(999999L);
        
        when(kardexRestMapper.toDomain(largeRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentEntry(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponse(mockKardex)).thenReturn(entryResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentEntry(largeRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexCommandPort, times(1)).registerAdjustmentEntry(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle sale adjustment with minimum values")
    void testAdjustmentExit_MinimumValues() {
        // Arrange
        AdjustmentExitDTORequest minRequest = new AdjustmentExitDTORequest();
        minRequest.setDetails("Min sale");
        minRequest.setQuantity(1);
        minRequest.setProductId(1L);
        
        when(kardexRestMapper.toDomain(minRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentExit(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponseSale(mockKardex)).thenReturn(exitResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentExitDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentExit(minRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexCommandPort, times(1)).registerAdjustmentExit(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle sale adjustment with large values")
    void testAdjustmentExit_LargeValues() {
        // Arrange
        AdjustmentExitDTORequest largeRequest = new AdjustmentExitDTORequest();
        largeRequest.setDetails("Large sale");
        largeRequest.setQuantity(10000);
        largeRequest.setProductId(999999L);
        
        when(kardexRestMapper.toDomain(largeRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentExit(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponseSale(mockKardex)).thenReturn(exitResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentExitDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentExit(largeRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexCommandPort, times(1)).registerAdjustmentExit(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle purchase adjustment with current date")
    void testAdjustmentEntry_WithCurrentDate() {
        // Arrange
        ZonedDateTime currentDate = ZonedDateTime.now();
        entryRequest.setDate(currentDate);
        
        when(kardexRestMapper.toDomain(entryRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentEntry(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponse(mockKardex)).thenReturn(entryResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentEntry(entryRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexCommandPort, times(1)).registerAdjustmentEntry(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle sale adjustment with current date")
    void testAdjustmentExit_WithCurrentDate() {
        // Arrange
        ZonedDateTime currentDate = ZonedDateTime.now();
        exitRequest.setDate(currentDate);
        
        when(kardexRestMapper.toDomain(exitRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentExit(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponseSale(mockKardex)).thenReturn(exitResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentExitDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentExit(exitRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexCommandPort, times(1)).registerAdjustmentExit(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle different product IDs for purchase")
    void testAdjustmentEntry_DifferentProductIds() {
        // Arrange
        entryRequest.setProductId(100L);
        
        when(kardexRestMapper.toDomain(entryRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentEntry(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponse(mockKardex)).thenReturn(entryResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentEntry(entryRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexCommandPort, times(1)).registerAdjustmentEntry(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle different product IDs for sale")
    void testAdjustmentExit_DifferentProductIds() {
        // Arrange
        exitRequest.setProductId(200L);
        
        when(kardexRestMapper.toDomain(exitRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentExit(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponseSale(mockKardex)).thenReturn(exitResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentExitDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentExit(exitRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexCommandPort, times(1)).registerAdjustmentExit(mockKardex);
    }
    
    @Test
    @DisplayName("Should verify mapper is called with correct request for entry")
    void testAdjustmentEntry_MapperVerification() {
        // Arrange
        when(kardexRestMapper.toDomain(entryRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentEntry(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponse(mockKardex)).thenReturn(entryResponse);
        
        // Act
        kardexPEPSCommandController.AdjustmentEntry(entryRequest);
        
        // Assert
        verify(kardexRestMapper, times(1)).toDomain(entryRequest);
        verify(kardexRestMapper, times(1)).toDTOResponse(mockKardex);
    }
    
    @Test
    @DisplayName("Should verify mapper is called with correct request for exit")
    void testAdjustmentExit_MapperVerification() {
        // Arrange
        when(kardexRestMapper.toDomain(exitRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentExit(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponseSale(mockKardex)).thenReturn(exitResponse);
        
        // Act
        kardexPEPSCommandController.AdjustmentExit(exitRequest);
        
        // Assert
        verify(kardexRestMapper, times(1)).toDomain(exitRequest);
        verify(kardexRestMapper, times(1)).toDTOResponseSale(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle multiple consecutive purchase adjustments")
    void testAdjustmentEntry_MultipleConsecutive() {
        // Arrange
        when(kardexRestMapper.toDomain(any(AdjustmentEntryDTORequest.class))).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentEntry(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponse(mockKardex)).thenReturn(entryResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> response1 = 
            kardexPEPSCommandController.AdjustmentEntry(entryRequest);
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> response2 = 
            kardexPEPSCommandController.AdjustmentEntry(entryRequest);
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> response3 = 
            kardexPEPSCommandController.AdjustmentEntry(entryRequest);
        
        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        verify(kardexCommandPort, times(3)).registerAdjustmentEntry(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle multiple consecutive sale adjustments")
    void testAdjustmentExit_MultipleConsecutive() {
        // Arrange
        when(kardexRestMapper.toDomain(any(AdjustmentExitDTORequest.class))).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentExit(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponseSale(mockKardex)).thenReturn(exitResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentExitDTOResponse>> response1 = 
            kardexPEPSCommandController.AdjustmentExit(exitRequest);
        ResponseEntity<ResponseDTO<AdjustmentExitDTOResponse>> response2 = 
            kardexPEPSCommandController.AdjustmentExit(exitRequest);
        ResponseEntity<ResponseDTO<AdjustmentExitDTOResponse>> response3 = 
            kardexPEPSCommandController.AdjustmentExit(exitRequest);
        
        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        verify(kardexCommandPort, times(3)).registerAdjustmentExit(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle multiple consecutive delete operations")
    void testDeleteAllKardex_MultipleConsecutive() {
        // Arrange
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        ResponseEntity<ResponseDTO<Void>> response1 = kardexPEPSCommandController.deleteAllKardex();
        ResponseEntity<ResponseDTO<Void>> response2 = kardexPEPSCommandController.deleteAllKardex();
        ResponseEntity<ResponseDTO<Void>> response3 = kardexPEPSCommandController.deleteAllKardex();
        
        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        verify(kardexCommandPort, times(3)).deleteAll();
    }
    
    @Test
    @DisplayName("Should return correct HTTP status for all operations")
    void testAllOperations_HttpStatusVerification() {
        // Arrange
        when(kardexRestMapper.toDomain(any(AdjustmentEntryDTORequest.class))).thenReturn(mockKardex);
        when(kardexRestMapper.toDomain(any(AdjustmentExitDTORequest.class))).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentEntry(any())).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentExit(any())).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponse(any())).thenReturn(entryResponse);
        when(kardexRestMapper.toDTOResponseSale(any())).thenReturn(exitResponse);
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> entryResp = 
            kardexPEPSCommandController.AdjustmentEntry(entryRequest);
        ResponseEntity<ResponseDTO<AdjustmentExitDTOResponse>> exitResp = 
            kardexPEPSCommandController.AdjustmentExit(exitRequest);
        ResponseEntity<ResponseDTO<Void>> deleteResp = 
            kardexPEPSCommandController.deleteAllKardex();
        
        // Assert
        assertEquals(HttpStatus.OK, entryResp.getStatusCode());
        assertEquals(HttpStatus.OK, exitResp.getStatusCode());
        assertEquals(HttpStatus.OK, deleteResp.getStatusCode());
    }
    
    @Test
    @DisplayName("Should handle purchase with decimal unit price")
    void testAdjustmentEntry_DecimalUnitPrice() {
        // Arrange
        entryRequest.setUnitPrice(new BigDecimal("123.456"));
        
        when(kardexRestMapper.toDomain(entryRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentEntry(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponse(mockKardex)).thenReturn(entryResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentEntry(entryRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kardexCommandPort, times(1)).registerAdjustmentEntry(mockKardex);
    }
    
    @Test
    @DisplayName("Should handle purchase with long details text")
    void testAdjustmentEntry_LongDetails() {
        // Arrange
        String longDetails = "This is a very long details text that describes the purchase adjustment " +
                           "with extensive information about the transaction and its purpose";
        entryRequest.setDetails(longDetails);
        
        when(kardexRestMapper.toDomain(entryRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentEntry(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponse(mockKardex)).thenReturn(entryResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentEntryDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentEntry(entryRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    @DisplayName("Should handle sale with long details text")
    void testAdjustmentExit_LongDetails() {
        // Arrange
        String longDetails = "This is a very long details text that describes the sale adjustment " +
                           "with extensive information about the transaction and its purpose";
        exitRequest.setDetails(longDetails);
        
        when(kardexRestMapper.toDomain(exitRequest)).thenReturn(mockKardex);
        when(kardexCommandPort.registerAdjustmentExit(mockKardex)).thenReturn(mockKardex);
        when(kardexRestMapper.toDTOResponseSale(mockKardex)).thenReturn(exitResponse);
        
        // Act
        ResponseEntity<ResponseDTO<AdjustmentExitDTOResponse>> response = 
            kardexPEPSCommandController.AdjustmentExit(exitRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

