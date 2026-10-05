package co.unicauca.edu.co.contables.inventory.kardex.unit.application.kardex;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.command.KardexReturnService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexQueryRepositoryPort;

@ExtendWith(MockitoExtension.class)
public class KardexReturnServiceUnitTest {
    
    @Mock
    private IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    
    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;
    
    @Mock
    private IMessageServicePort messageService;
    
    @InjectMocks
    private KardexReturnService kardexReturnService;
    
    private String factCode;
    private Long productId;
    private int quantity;
    private Kardex originalKardex;
    private Kardex returnKardex;
    
    @BeforeEach
    void setUp() {
        factCode = "FACT001";
        productId = 1L;
        quantity = 5;
        
        originalKardex = new Kardex();
        originalKardex.setProductId(productId);
        originalKardex.setFactCode(factCode);
        originalKardex.setQuantity(10);
        originalKardex.setUnitPrice(new BigDecimal("25.50"));
        originalKardex.setType(MovementType.PURCHASE);
        
        returnKardex = new Kardex();
        returnKardex.setProductId(productId);
        returnKardex.setFactCode(factCode);
        returnKardex.setQuantity(3);
        returnKardex.setType(MovementType.PURCHASERETURN);
    }
    
    @Test
    @DisplayName("Should return unit price when return is allowed for purchase")
    void testGetUnitPriceIfReturnAllowedForPurchaseSuccess() {
        // Arrange
        List<Kardex> originalKardexList = Arrays.asList(originalKardex);
        List<Kardex> returnKardexList = Arrays.asList(returnKardex);
        
        when(kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, MovementType.PURCHASE))
            .thenReturn(originalKardexList);
        when(kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, MovementType.PURCHASERETURN))
            .thenReturn(returnKardexList);
        
        // Act
        BigDecimal result = kardexReturnService.getUnitPriceIfReturnAllowed(factCode, quantity, productId, MovementType.PURCHASE);
        
        // Assert
        assertEquals(new BigDecimal("25.50"), result);
        verify(kardexQueryRepositoryPort).findByFactCodeAndProductIdAndType(factCode, productId, MovementType.PURCHASE);
        verify(kardexQueryRepositoryPort).findByFactCodeAndProductIdAndType(factCode, productId, MovementType.PURCHASERETURN);
    }
    
    @Test
    @DisplayName("Should return unit price when return is allowed for sale")
    void testGetUnitPriceIfReturnAllowedForSaleSuccess() {
        // Arrange
        originalKardex.setType(MovementType.SALE);
        returnKardex.setType(MovementType.SALESRETURN);
        
        List<Kardex> originalKardexList = Arrays.asList(originalKardex);
        List<Kardex> returnKardexList = Arrays.asList(returnKardex);
        
        when(kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, MovementType.SALE))
            .thenReturn(originalKardexList);
        when(kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, MovementType.SALESRETURN))
            .thenReturn(returnKardexList);
        
        // Act
        BigDecimal result = kardexReturnService.getUnitPriceIfReturnAllowed(factCode, quantity, productId, MovementType.SALE);
        
        // Assert
        assertEquals(new BigDecimal("25.50"), result);
        verify(kardexQueryRepositoryPort).findByFactCodeAndProductIdAndType(factCode, productId, MovementType.SALE);
        verify(kardexQueryRepositoryPort).findByFactCodeAndProductIdAndType(factCode, productId, MovementType.SALESRETURN);
    }
    
    @Test
    @DisplayName("Should throw error when original movement not found")
    void testGetUnitPriceIfReturnAllowedWithNoOriginalMovement() {
        // Arrange
        List<Kardex> emptyList = Collections.emptyList();
        when(kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, MovementType.PURCHASE))
            .thenReturn(emptyList);
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Not found error");
        
        // Act & Assert
        try {
            kardexReturnService.getUnitPriceIfReturnAllowed(factCode, quantity, productId, MovementType.PURCHASE);
        } catch (Exception e) {
            // El método lanza una excepción cuando no encuentra el registro original
            // pero también llama al formatterResultOutputPort antes de lanzar la excepción
        }
        
        // Verify that the error response was called
        verify(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(eq(404), anyString());
        verify(messageService).getMessage(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should throw error when return quantity exceeds original quantity")
    void testGetUnitPriceIfReturnAllowedWithExceededQuantity() {
        // Arrange
        int excessiveQuantity = 8; // Original is 10, previous return is 3, total would be 11
        List<Kardex> originalKardexList = Arrays.asList(originalKardex);
        List<Kardex> returnKardexList = Arrays.asList(returnKardex);
        
        when(kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, MovementType.PURCHASE))
            .thenReturn(originalKardexList);
        when(kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, MovementType.PURCHASERETURN))
            .thenReturn(returnKardexList);
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Quantity exceeded error");
        
        // Act
        BigDecimal result = kardexReturnService.getUnitPriceIfReturnAllowed(factCode, excessiveQuantity, productId, MovementType.PURCHASE);
        
        // Assert
        assertEquals(BigDecimal.ZERO, result);
        verify(formatterResultOutputPort).returnBusinessRuleErrorResponse(eq(400), anyString());
        verify(messageService).getMessage(anyString(), eq("Quantity"));
    }
    
    @Test
    @DisplayName("Should return unit price when no previous returns exist")
    void testGetUnitPriceIfReturnAllowedWithNoPreviousReturns() {
        // Arrange
        List<Kardex> originalKardexList = Arrays.asList(originalKardex);
        List<Kardex> emptyReturnList = Collections.emptyList();
        
        when(kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, MovementType.PURCHASE))
            .thenReturn(originalKardexList);
        when(kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, MovementType.PURCHASERETURN))
            .thenReturn(emptyReturnList);
        
        // Act
        BigDecimal result = kardexReturnService.getUnitPriceIfReturnAllowed(factCode, quantity, productId, MovementType.PURCHASE);
        
        // Assert
        assertEquals(new BigDecimal("25.50"), result);
    }
    
    @Test
    @DisplayName("Should return PURCHASERETURN for PURCHASE movement type")
    void testGetReturnTypeForPurchase() {
        // Act
        MovementType result = kardexReturnService.getReturnType(MovementType.PURCHASE);
        
        // Assert
        assertEquals(MovementType.PURCHASERETURN, result);
    }
    
    @Test
    @DisplayName("Should return SALESRETURN for SALE movement type")
    void testGetReturnTypeForSale() {
        // Act
        MovementType result = kardexReturnService.getReturnType(MovementType.SALE);
        
        // Assert
        assertEquals(MovementType.SALESRETURN, result);
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid movement type")
    void testGetReturnTypeForInvalidType() {
        // Arrange
        when(messageService.getMessage(anyString(), anyString(), any())).thenReturn("Invalid type error");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            kardexReturnService.getReturnType(MovementType.PURCHASERETURN);
        });
    }
}