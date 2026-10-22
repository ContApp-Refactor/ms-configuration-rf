package co.unicauca.edu.co.contables.inventory.kardex.unit.application.kardex;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.command.StockIntegrationService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.external.IStockClientPort;

@ExtendWith(MockitoExtension.class)
public class StockIntegrationServiceUnitTest {
    
    @Mock
    private IStockClientPort stockClient;
    
    @InjectMocks
    private StockIntegrationService stockIntegrationService;
    
    private Kardex kardex;
    private Stock stock;
    
    @BeforeEach
    void setUp() {
        kardex = new Kardex();
        kardex.setProductId(1L);
        kardex.setQuantity(10);
        kardex.setBalanceUnitPrice(new BigDecimal("25.50"));
        
        stock = Stock.builder()
            .productId(1L)
            .quantity(10)
            .price(new BigDecimal("25.50"))
            .build();
    }
    
    @Test
    @DisplayName("Should create stock object from kardex successfully")
    void testCreateStock() {
        // Act
        Stock result = stockIntegrationService.createStock(kardex);
        
        // Assert
        assertNotNull(result);
        assertEquals(kardex.getProductId(), result.getProductId());
        assertEquals(kardex.getQuantity(), result.getQuantity());
        assertEquals(kardex.getBalanceUnitPrice(), result.getPrice());
    }
    
    
    @Test
    @DisplayName("Should handle exception when buy stock API fails")
    void testCallApiStockServiceForBuyWithException() {
        // Arrange
        RuntimeException exception = new RuntimeException("API Error");
        doThrow(exception).when(stockClient).buyStock(stock);
        
        // Act - El método captura la excepción y solo la loguea, no la re-lanza
        assertDoesNotThrow(() -> {
            stockIntegrationService.callApiStockService(stock, true);
        });
        
        // Assert
        verify(stockClient).buyStock(stock);
    }
    
    @Test
    @DisplayName("Should handle exception when sell stock API fails")
    void testCallApiStockServiceForSellWithException() {
        // Arrange
        RuntimeException exception = new RuntimeException("API Error");
        doThrow(exception).when(stockClient).sellStock(stock);
        
        // Act - El método captura la excepción y solo la loguea, no la re-lanza
        assertDoesNotThrow(() -> {
            stockIntegrationService.callApiStockService(stock, false);
        });
        
        // Assert
        verify(stockClient).sellStock(stock);
    }
    
    @Test
    @DisplayName("Should create stock with correct values when kardex has different values")
    void testCreateStockWithDifferentValues() {
        // Arrange
        kardex.setProductId(999L);
        kardex.setQuantity(50);
        kardex.setBalanceUnitPrice(new BigDecimal("100.75"));
        
        // Act
        Stock result = stockIntegrationService.createStock(kardex);
        
        // Assert
        assertEquals(999L, result.getProductId());
        assertEquals(50, result.getQuantity());
        assertEquals(new BigDecimal("100.75"), result.getPrice());
    }
    
    @Test
    @DisplayName("Should create stock with zero values when kardex has zero values")
    void testCreateStockWithZeroValues() {
        // Arrange
        kardex.setProductId(0L);
        kardex.setQuantity(0);
        kardex.setBalanceUnitPrice(BigDecimal.ZERO);
        
        // Act
        Stock result = stockIntegrationService.createStock(kardex);
        
        // Assert
        assertEquals(0L, result.getProductId());
        assertEquals(0, result.getQuantity());
        assertEquals(BigDecimal.ZERO, result.getPrice());
    }
}