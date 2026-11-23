package co.unicauca.edu.co.contables.inventory.peps.unit.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import co.unicauca.edu.co.contables.inventory.peps.application.service.command.StockIntegrationService;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IStockClientPort;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StockIntegrationServiceUnitTest {
    @Mock
    private IStockClientPort stockClient;

    @InjectMocks
    private StockIntegrationService stockIntegrationService;

    private Product mockProduct;
    private Kardex mockKardex;
    private Stock mockStock;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        mockProduct = new Product();
        mockProduct.setProductId(1L);
        mockProduct.setName("BlackBerry");
        mockProduct.setState(true);

        mockKardex = Kardex.createPurchase(
            "1001",
            "Purchase",
            100,
            new BigDecimal("10.50"),
            mockProduct
        );

        mockStock = Stock.builder()
            .productId(1L)
            .quantity(100)
            .price(new BigDecimal("10.50"))
            .build();
    }


    @Test
    @DisplayName("Should create stock from kardex successfully")
    void testCreateStock_ValidKardex_ReturnsStock() {
        // Act
        Stock result = stockIntegrationService.createStock(mockKardex);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(100, result.getQuantity());
        assertEquals(0, new BigDecimal("10.50").compareTo(result.getPrice()));
    }
    @Test
    @DisplayName("Should create stock with correct product ID from kardex")
    void testCreateStock_ValidKardex_CopiesProductId() {
        // Arrange
        Product productWithDifferentId = new Product();
        productWithDifferentId.setProductId(999L);
        productWithDifferentId.setState(true); // ✅ AGREGAR ESTA LÍNEA
        
        Kardex kardexWithDifferentProduct = Kardex.createPurchase(
            "2001",
            "Purchase",
            50,
            new BigDecimal("25.00"),
            productWithDifferentId
        );

        // Act
        Stock result = stockIntegrationService.createStock(kardexWithDifferentProduct);

        // Assert
        assertEquals(999L, result.getProductId());
        assertEquals(50, result.getQuantity());
        assertEquals(0, new BigDecimal("25.00").compareTo(result.getPrice()));
    }


    @Test
    @DisplayName("Should create stock with quantity from kardex")
    void testCreateStock_DifferentQuantities_CopiesCorrectly() {
        // Arrange
        Kardex kardexWithDifferentQuantity = Kardex.createPurchase(
            "3001",
            "Purchase",
            500,
            new BigDecimal("15.75"),
            mockProduct
        );

        // Act
        Stock result = stockIntegrationService.createStock(kardexWithDifferentQuantity);

        // Assert
        assertEquals(500, result.getQuantity());
    }

    @Test
    @DisplayName("Should create stock with unit price from kardex")
    void testCreateStock_DifferentPrices_CopiesCorrectly() {
        // Arrange
        Kardex kardexWithDifferentPrice = Kardex.createPurchase(
            "4001",
            "Purchase",
            100,
            new BigDecimal("99.99"),
            mockProduct
        );

        // Act
        Stock result = stockIntegrationService.createStock(kardexWithDifferentPrice);

        // Assert
        assertEquals(0, new BigDecimal("99.99").compareTo(result.getPrice()));
    }

    @Test
    @DisplayName("Should create stock from sale kardex")
    void testCreateStock_SaleKardex_CreatesStock() {
        // Arrange
        Kardex saleKardex = Kardex.createSale(
            "5001",
            "Sale",
            30,
            new BigDecimal("12.00"),
            mockProduct
        );

        // Act
        Stock result = stockIntegrationService.createStock(saleKardex);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(30, result.getQuantity());
        assertEquals(0, new BigDecimal("12.00").compareTo(result.getPrice()));
    }

    // ==================== callApiStockService() - Buy Operations ====================
    @Test
    @DisplayName("Should call buyStock when isBuy is true")
    void testCallApiStockService_IsBuyTrue_CallsBuyStock() {
        // Arrange
        doNothing().when(stockClient).buyStock(any(Stock.class));

        // Act
        stockIntegrationService.callApiStockService(mockStock, true);

        // Assert
        verify(stockClient).buyStock(mockStock);
        verify(stockClient, never()).sellStock(any(Stock.class));
    }

    @Test
    @DisplayName("Should pass correct stock to buyStock")
    void testCallApiStockService_BuyOperation_PassesCorrectStock() {
        // Arrange
        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        doNothing().when(stockClient).buyStock(any(Stock.class));

        // Act
        stockIntegrationService.callApiStockService(mockStock, true);

        // Assert
        verify(stockClient).buyStock(stockCaptor.capture());
        
        Stock capturedStock = stockCaptor.getValue();
        assertEquals(mockStock.getProductId(), capturedStock.getProductId());
        assertEquals(mockStock.getQuantity(), capturedStock.getQuantity());
        assertEquals(0, mockStock.getPrice().compareTo(capturedStock.getPrice()));
    }

    @Test
    @DisplayName("Should log success message for buy operation")
    void testCallApiStockService_BuySuccess_LogsSuccessMessage() {
        // Arrange
        doNothing().when(stockClient).buyStock(any(Stock.class));

        // Act & Assert
        assertDoesNotThrow(() -> 
            stockIntegrationService.callApiStockService(mockStock, true)
        );
        
        verify(stockClient).buyStock(mockStock);
    }

    // ==================== callApiStockService() - Sell Operations ====================
    @Test
    @DisplayName("Should call sellStock when isBuy is false")
    void testCallApiStockService_IsBuyFalse_CallsSellStock() {
        // Arrange
        doNothing().when(stockClient).sellStock(any(Stock.class));

        // Act
        stockIntegrationService.callApiStockService(mockStock, false);

        // Assert
        verify(stockClient).sellStock(mockStock);
        verify(stockClient, never()).buyStock(any(Stock.class));
    }

    @Test
    @DisplayName("Should pass correct stock to sellStock")
    void testCallApiStockService_SellOperation_PassesCorrectStock() {
        // Arrange
        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        doNothing().when(stockClient).sellStock(any(Stock.class));

        // Act
        stockIntegrationService.callApiStockService(mockStock, false);

        // Assert
        verify(stockClient).sellStock(stockCaptor.capture());
        
        Stock capturedStock = stockCaptor.getValue();
        assertEquals(mockStock.getProductId(), capturedStock.getProductId());
        assertEquals(mockStock.getQuantity(), capturedStock.getQuantity());
        assertEquals(0, mockStock.getPrice().compareTo(capturedStock.getPrice()));
    }

    @Test
    @DisplayName("Should log success message for sell operation")
    void testCallApiStockService_SellSuccess_LogsSuccessMessage() {
        // Arrange
        doNothing().when(stockClient).sellStock(any(Stock.class));

        // Act & Assert
        assertDoesNotThrow(() -> 
            stockIntegrationService.callApiStockService(mockStock, false)
        );
        
        verify(stockClient).sellStock(mockStock);
    }

    // ==================== Error Handling - Buy ====================
    @Test
    @DisplayName("Should handle exception during buy operation")
    void testCallApiStockService_BuyThrowsException_LogsError() {
        // Arrange
        RuntimeException exception = new RuntimeException("Connection timeout");
        doThrow(exception).when(stockClient).buyStock(any(Stock.class));

        // Act & Assert - Should not throw exception, just log it
        assertDoesNotThrow(() -> 
            stockIntegrationService.callApiStockService(mockStock, true)
        );
        
        verify(stockClient).buyStock(mockStock);
    }

    @Test
    @DisplayName("Should not throw exception when buy operation fails")
    void testCallApiStockService_BuyFails_DoesNotThrowException() {
        // Arrange
        doThrow(new RuntimeException("API Error")).when(stockClient).buyStock(any(Stock.class));

        // Act & Assert
        assertDoesNotThrow(() -> 
            stockIntegrationService.callApiStockService(mockStock, true)
        );
    }

    // ==================== Error Handling - Sell ====================
    @Test
    @DisplayName("Should handle exception during sell operation")
    void testCallApiStockService_SellThrowsException_LogsError() {
        // Arrange
        RuntimeException exception = new RuntimeException("Insufficient stock");
        doThrow(exception).when(stockClient).sellStock(any(Stock.class));

        // Act & Assert - Should not throw exception, just log it
        assertDoesNotThrow(() -> 
            stockIntegrationService.callApiStockService(mockStock, false)
        );
        
        verify(stockClient).sellStock(mockStock);
    }

    @Test
    @DisplayName("Should not throw exception when sell operation fails")
    void testCallApiStockService_SellFails_DoesNotThrowException() {
        // Arrange
        doThrow(new RuntimeException("API Error")).when(stockClient).sellStock(any(Stock.class));

        // Act & Assert
        assertDoesNotThrow(() -> 
            stockIntegrationService.callApiStockService(mockStock, false)
        );
    }

    @Test
    @DisplayName("Should handle network timeout exception")
    void testCallApiStockService_NetworkTimeout_LogsError() {
        // Arrange
        RuntimeException timeoutException = new RuntimeException("Request timeout after 5000ms");
        doThrow(timeoutException).when(stockClient).buyStock(any(Stock.class));

        // Act & Assert - Should not throw exception, just log it
        assertDoesNotThrow(() -> 
            stockIntegrationService.callApiStockService(mockStock, true)
        );
        
        verify(stockClient).buyStock(mockStock);
    }

    // ==================== Integration Tests ====================
    @Test
    @DisplayName("Should create stock and call buy API in sequence")
    void testCreateStockAndBuy_ValidKardex_ExecutesSuccessfully() {
        // Arrange
        doNothing().when(stockClient).buyStock(any(Stock.class));

        // Act
        Stock stock = stockIntegrationService.createStock(mockKardex);
        stockIntegrationService.callApiStockService(stock, true);

        // Assert
        assertNotNull(stock);
        verify(stockClient).buyStock(stock);
    }

    @Test
    @DisplayName("Should create stock and call sell API in sequence")
    void testCreateStockAndSell_ValidKardex_ExecutesSuccessfully() {
        // Arrange
        doNothing().when(stockClient).sellStock(any(Stock.class));

        // Act
        Stock stock = stockIntegrationService.createStock(mockKardex);
        stockIntegrationService.callApiStockService(stock, false);

        // Assert
        assertNotNull(stock);
        verify(stockClient).sellStock(stock);
    }
    
    @Test
    @DisplayName("Should handle multiple buy operations")
    void testCallApiStockService_MultipleBuys_CallsClientMultipleTimes() {
        // Arrange
        Stock stock1 = Stock.builder().productId(1L).quantity(10).price(new BigDecimal("5.00")).build();
        Stock stock2 = Stock.builder().productId(2L).quantity(20).price(new BigDecimal("10.00")).build();
        Stock stock3 = Stock.builder().productId(3L).quantity(30).price(new BigDecimal("15.00")).build();
        
        doNothing().when(stockClient).buyStock(any(Stock.class));

        // Act
        stockIntegrationService.callApiStockService(stock1, true);
        stockIntegrationService.callApiStockService(stock2, true);
        stockIntegrationService.callApiStockService(stock3, true);

        // Assert
        verify(stockClient, times(3)).buyStock(any(Stock.class));
        verify(stockClient).buyStock(stock1);
        verify(stockClient).buyStock(stock2);
        verify(stockClient).buyStock(stock3);
    }

    @Test
    @DisplayName("Should handle mixed buy and sell operations")
    void testCallApiStockService_MixedOperations_CallsCorrectMethods() {
        // Arrange
        doNothing().when(stockClient).buyStock(any(Stock.class));
        doNothing().when(stockClient).sellStock(any(Stock.class));

        // Act
        stockIntegrationService.callApiStockService(mockStock, true);  // Buy
        stockIntegrationService.callApiStockService(mockStock, false); // Sell
        stockIntegrationService.callApiStockService(mockStock, true);  // Buy

        // Assert
        verify(stockClient, times(2)).buyStock(mockStock);
        verify(stockClient, times(1)).sellStock(mockStock);
    }

    // ==================== Edge Cases ====================
    @Test
    @DisplayName("Should handle stock with zero quantity")
    void testCreateStock_ZeroQuantity_CreatesStock() {
        // Arrange
        Kardex zeroQuantityKardex = Kardex.builder()
            .product(mockProduct)
            .quantity(0)
            .unitPrice(new BigDecimal("10.00"))
            .build();

        // Act
        Stock result = stockIntegrationService.createStock(zeroQuantityKardex);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getQuantity());
    }

    @Test
    @DisplayName("Should handle stock with large quantity")
    void testCreateStock_LargeQuantity_CreatesStock() {
        // Arrange
        Kardex largeQuantityKardex = Kardex.createPurchase(
            "6001",
            "Large Purchase",
            1_000_000,
            new BigDecimal("0.01"),
            mockProduct
        );

        // Act
        Stock result = stockIntegrationService.createStock(largeQuantityKardex);

        // Assert
        assertEquals(1_000_000, result.getQuantity());
    }

    @Test
    @DisplayName("Should handle stock with very small price")
    void testCreateStock_VerySmallPrice_CreatesStock() {
        // Arrange
        Kardex smallPriceKardex = Kardex.createPurchase(
            "7001",
            "Small Price Purchase",
            100,
            new BigDecimal("0.0001"),
            mockProduct
        );

        // Act
        Stock result = stockIntegrationService.createStock(smallPriceKardex);

        // Assert
        assertEquals(0, new BigDecimal("0.0001").compareTo(result.getPrice()));
    }


}
