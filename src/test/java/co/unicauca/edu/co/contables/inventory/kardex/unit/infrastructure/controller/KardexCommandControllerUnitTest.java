package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller.KardexCommandController;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexAdjustmentEntryDtoRequest;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexAdjustmentExitDtoRequest;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper.IKardexResponseMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper.IKardexRestMapper;

@ExtendWith(MockitoExtension.class)
public class KardexCommandControllerUnitTest {
    
    @Mock
    private IKardexCommandPort kardexCommandPort;
    
    @Mock
    private IKardexRestMapper kardexRestMapper;
    
    @Mock
    private IKardexResponseMapper kardexResponseMapper;
    
    @InjectMocks
    private KardexCommandController kardexCommandController;
    
    private KardexAdjustmentEntryDtoRequest purchaseRequest;
    private KardexAdjustmentExitDtoRequest saleRequest;
    private Kardex adjustmentEntry;
    private Kardex adjustmentExit;
    private KardexDtoResponse purchaseResponse;
    private KardexDtoResponse saleResponse;
    
    @BeforeEach
    void setUp() {
        // Setup purchase request
        purchaseRequest = new KardexAdjustmentEntryDtoRequest();
        purchaseRequest.setProductId(100L);
        purchaseRequest.setQuantity(50L);
        purchaseRequest.setUnitPrice(new BigDecimal("100.00"));
        purchaseRequest.setDetails("Purchase of office supplies");
        
        // Setup sale request
        saleRequest = new KardexAdjustmentExitDtoRequest();
        saleRequest.setProductId(100L);
        saleRequest.setQuantity(20L);
        saleRequest.setDetails("Sale to client ABC");
        
        // Setup purchase kardex domain
        adjustmentEntry = new Kardex();
        adjustmentEntry.setId(1L);
        adjustmentEntry.setProductId(100L);
        adjustmentEntry.setQuantity(50);
        adjustmentEntry.setUnitPrice(new BigDecimal("100.00"));
        adjustmentEntry.setType(MovementType.PURCHASE);
        adjustmentEntry.setDate(ZonedDateTime.now());
        adjustmentEntry.setDetails("Purchase of office supplies");
        
        // Setup sale kardex domain
        adjustmentExit = new Kardex();
        adjustmentExit.setId(2L);
        adjustmentExit.setProductId(100L);
        adjustmentExit.setQuantity(20);
        adjustmentExit.setUnitPrice(new BigDecimal("100.00"));
        adjustmentExit.setType(MovementType.SALE);
        adjustmentExit.setDate(ZonedDateTime.now());
        adjustmentExit.setDetails("Sale to client ABC");
        
        // Setup purchase response
        purchaseResponse = new KardexDtoResponse();
        purchaseResponse.setId(1L);
        purchaseResponse.setQuantity(50L);
        purchaseResponse.setType(MovementType.PURCHASE);
        
        // Setup sale response
        saleResponse = new KardexDtoResponse();
        saleResponse.setId(2L);
        saleResponse.setQuantity(20L);
        saleResponse.setType(MovementType.SALE);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should register purchase successfully")
    void testadjustmentEntry() {
        // Arrange
        when(kardexRestMapper.toDomain(purchaseRequest)).thenReturn(adjustmentEntry);
        when(kardexCommandPort.registerAdjustmentEntry(adjustmentEntry)).thenReturn(adjustmentEntry);
        when(kardexResponseMapper.toDtoResponse(adjustmentEntry)).thenReturn(purchaseResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.adjustmentEntry(purchaseRequest);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Kardex adjustment entry registered successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(1L, response.getBody().getData().getId());
        assertEquals(50L, response.getBody().getData().getQuantity());
        assertEquals(MovementType.PURCHASE, response.getBody().getData().getType());
        
        verify(kardexRestMapper).toDomain(purchaseRequest);
        verify(kardexCommandPort).registerAdjustmentEntry(adjustmentEntry);
        verify(kardexResponseMapper).toDtoResponse(adjustmentEntry);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should register sale successfully")
    void testadjustmentExit() {
        // Arrange
        when(kardexRestMapper.toDomain(saleRequest)).thenReturn(adjustmentExit);
        when(kardexCommandPort.registerAdjustmentExit(adjustmentExit)).thenReturn(adjustmentExit);
        when(kardexResponseMapper.toDtoResponse(adjustmentExit)).thenReturn(saleResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.adjustmentExit(saleRequest);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Kardex adjustment exit registered successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(2L, response.getBody().getData().getId());
        assertEquals(20L, response.getBody().getData().getQuantity());
        assertEquals(MovementType.SALE, response.getBody().getData().getType());
        
        verify(kardexRestMapper).toDomain(saleRequest);
        verify(kardexCommandPort).registerAdjustmentExit(adjustmentExit);
        verify(kardexResponseMapper).toDtoResponse(adjustmentExit);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should delete all kardex records successfully")
    void testDeleteAllKardex() {
        // Arrange
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        ResponseEntity<ResponseDto<Void>> response = kardexCommandController.deleteAllKardex();
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("All kardex records deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        
        verify(kardexCommandPort).deleteAll();
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle purchase with minimum valid values")
    void testadjustmentEntryWithMinimumValues() {
        // Arrange
        KardexAdjustmentEntryDtoRequest minRequest = new KardexAdjustmentEntryDtoRequest();
        minRequest.setProductId(1L);
        minRequest.setQuantity(1L);
        minRequest.setUnitPrice(new BigDecimal("0.01"));
        
        Kardex minKardex = new Kardex();
        minKardex.setId(3L);
        minKardex.setProductId(1L);
        minKardex.setQuantity(1);
        minKardex.setUnitPrice(new BigDecimal("0.01"));
        minKardex.setType(MovementType.PURCHASE);
        
        KardexDtoResponse minResponse = new KardexDtoResponse();
        minResponse.setId(3L);
        minResponse.setQuantity(1L);
        
        when(kardexRestMapper.toDomain(minRequest)).thenReturn(minKardex);
        when(kardexCommandPort.registerAdjustmentEntry(minKardex)).thenReturn(minKardex);
        when(kardexResponseMapper.toDtoResponse(minKardex)).thenReturn(minResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.adjustmentEntry(minRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        assertEquals(1L, response.getBody().getData().getQuantity());
        
        verify(kardexCommandPort).registerAdjustmentEntry(minKardex);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle sale with minimum valid values")
    void testadjustmentExitWithMinimumValues() {
        // Arrange
        KardexAdjustmentExitDtoRequest minRequest = new KardexAdjustmentExitDtoRequest();
        minRequest.setProductId(1L);
        minRequest.setQuantity(1L);
        
        Kardex minKardex = new Kardex();
        minKardex.setId(4L);
        minKardex.setProductId(1L);
        minKardex.setQuantity(1);
        minKardex.setType(MovementType.SALE);
        
        KardexDtoResponse minResponse = new KardexDtoResponse();
        minResponse.setId(4L);
        minResponse.setQuantity(1L);
        
        when(kardexRestMapper.toDomain(minRequest)).thenReturn(minKardex);
        when(kardexCommandPort.registerAdjustmentExit(minKardex)).thenReturn(minKardex);
        when(kardexResponseMapper.toDtoResponse(minKardex)).thenReturn(minResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.adjustmentExit(minRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        assertEquals(1L, response.getBody().getData().getQuantity());
        
        verify(kardexCommandPort).registerAdjustmentExit(minKardex);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle purchase with large quantities")
    void testadjustmentEntryWithLargeQuantity() {
        // Arrange
        KardexAdjustmentEntryDtoRequest largeRequest = new KardexAdjustmentEntryDtoRequest();
        largeRequest.setProductId(100L);
        largeRequest.setQuantity(10000L);
        largeRequest.setUnitPrice(new BigDecimal("999.99"));
        
        Kardex largeKardex = new Kardex();
        largeKardex.setId(5L);
        largeKardex.setQuantity(10000);
        largeKardex.setUnitPrice(new BigDecimal("999.99"));
        
        KardexDtoResponse largeResponse = new KardexDtoResponse();
        largeResponse.setId(5L);
        largeResponse.setQuantity(10000L);
        
        when(kardexRestMapper.toDomain(largeRequest)).thenReturn(largeKardex);
        when(kardexCommandPort.registerAdjustmentEntry(largeKardex)).thenReturn(largeKardex);
        when(kardexResponseMapper.toDtoResponse(largeKardex)).thenReturn(largeResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.adjustmentEntry(largeRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(10000L, response.getBody().getData().getQuantity());
        
        verify(kardexCommandPort).registerAdjustmentEntry(largeKardex);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify purchase response structure")
    void testadjustmentEntryResponseStructure() {
        // Arrange
        when(kardexRestMapper.toDomain(purchaseRequest)).thenReturn(adjustmentEntry);
        when(kardexCommandPort.registerAdjustmentEntry(adjustmentEntry)).thenReturn(adjustmentEntry);
        when(kardexResponseMapper.toDtoResponse(adjustmentEntry)).thenReturn(purchaseResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.adjustmentEntry(purchaseRequest);
        
        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getStatus() > 0);
        assertTrue(response.getBody().getData() instanceof KardexDtoResponse);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify sale response structure")
    void testadjustmentExitResponseStructure() {
        // Arrange
        when(kardexRestMapper.toDomain(saleRequest)).thenReturn(adjustmentExit);
        when(kardexCommandPort.registerAdjustmentExit(adjustmentExit)).thenReturn(adjustmentExit);
        when(kardexResponseMapper.toDtoResponse(adjustmentExit)).thenReturn(saleResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.adjustmentExit(saleRequest);
        
        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getStatus() > 0);
        assertTrue(response.getBody().getData() instanceof KardexDtoResponse);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle purchase with details")
    void testadjustmentEntryWithDetails() {
        // Arrange
        purchaseRequest.setDetails("Urgent purchase for project X");
        
        when(kardexRestMapper.toDomain(purchaseRequest)).thenReturn(adjustmentEntry);
        when(kardexCommandPort.registerAdjustmentEntry(adjustmentEntry)).thenReturn(adjustmentEntry);
        when(kardexResponseMapper.toDtoResponse(adjustmentEntry)).thenReturn(purchaseResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.adjustmentEntry(purchaseRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        
        verify(kardexRestMapper).toDomain(purchaseRequest);
        verify(kardexCommandPort).registerAdjustmentEntry(adjustmentEntry);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle sale with details")
    void testadjustmentExitWithDetails() {
        // Arrange
        saleRequest.setDetails("Sale with 10% discount");
        
        when(kardexRestMapper.toDomain(saleRequest)).thenReturn(adjustmentExit);
        when(kardexCommandPort.registerAdjustmentExit(adjustmentExit)).thenReturn(adjustmentExit);
        when(kardexResponseMapper.toDtoResponse(adjustmentExit)).thenReturn(saleResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.adjustmentExit(saleRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        
        verify(kardexRestMapper).toDomain(saleRequest);
        verify(kardexCommandPort).registerAdjustmentExit(adjustmentExit);
    }
    
    @Test
    @DisplayName("Should verify mapper interactions for purchase")
    void testadjustmentEntryMapperInteractions() {
        // Arrange
        when(kardexRestMapper.toDomain(purchaseRequest)).thenReturn(adjustmentEntry);
        when(kardexCommandPort.registerAdjustmentEntry(adjustmentEntry)).thenReturn(adjustmentEntry);
        when(kardexResponseMapper.toDtoResponse(adjustmentEntry)).thenReturn(purchaseResponse);
        
        // Act
        kardexCommandController.adjustmentEntry(purchaseRequest);
        
        // Assert
        verify(kardexRestMapper, times(1)).toDomain(purchaseRequest);
        verify(kardexResponseMapper, times(1)).toDtoResponse(adjustmentEntry);
        verifyNoMoreInteractions(kardexRestMapper, kardexResponseMapper);
    }
    
    @Test
    @DisplayName("Should verify mapper interactions for sale")
    void testadjustmentExitMapperInteractions() {
        // Arrange
        when(kardexRestMapper.toDomain(saleRequest)).thenReturn(adjustmentExit);
        when(kardexCommandPort.registerAdjustmentExit(adjustmentExit)).thenReturn(adjustmentExit);
        when(kardexResponseMapper.toDtoResponse(adjustmentExit)).thenReturn(saleResponse);
        
        // Act
        kardexCommandController.adjustmentExit(saleRequest);
        
        // Assert
        verify(kardexRestMapper, times(1)).toDomain(saleRequest);
        verify(kardexResponseMapper, times(1)).toDtoResponse(adjustmentExit);
        verifyNoMoreInteractions(kardexRestMapper, kardexResponseMapper);
    }
    
    @Test
    @DisplayName("Should verify deleteAll is called only once")
    void testDeleteAllKardexVerifyOnce() {
        // Arrange
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        kardexCommandController.deleteAllKardex();
        
        // Assert
        verify(kardexCommandPort, times(1)).deleteAll();
        verifyNoMoreInteractions(kardexCommandPort);
    }
}
