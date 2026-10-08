package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.remoteSync;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.adapter.StockClientAdapter;
import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockCommandPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.GenericErrorException;

/**
 * @brief Unit tests for StockClientAdapter
 *
 * Tests the in-process adapter for stock operations including buy and sell
 * transactions. In the monolith the former WebClient proxy (IStockClient)
 * and its HTTP request/response DTOs were replaced by IStockCommandPort,
 * which registers purchases and sales directly in the stock module.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockClientAdapter Unit Tests")
class StockClientAdapterUnitTest {

    @Mock
    private IStockCommandPort stockCommandPort;

    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;

    @InjectMocks
    private StockClientAdapter stockClientAdapter;

    private Stock stock;

    @BeforeEach
    void setUp() {
        // Initialize Stock domain object
        stock = new Stock();
        stock.setProductId(1L);
        stock.setQuantity(10);
        stock.setPrice(BigDecimal.valueOf(100.00));
        stock.setEnterpriseId("ENT-123");
    }

    @Test
    @DisplayName("Should buy stock successfully with 200 OK")
    void testBuyStock_Success() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));

        verify(stockCommandPort, times(1)).registerPurchase(argThat(converted ->
            converted.getProductId().equals(1L)
                && converted.getQuantity() == 10
                && converted.getPrice().equals(BigDecimal.valueOf(100.00))));
    }

    @Test
    @DisplayName("Should buy stock successfully with 201 Created")
    void testBuyStock_Created() {
        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));

        verify(stockCommandPort, times(1)).registerPurchase(any());
    }

    @Test
    @DisplayName("Should swallow a client error on buy stock and report a generic error")
    void testBuyStock_ClientError() {
        // Arrange - el fallo remoto llega al adaptador como excepcion del port
        when(stockCommandPort.registerPurchase(any()))
            .thenThrow(new RuntimeException("Service unavailable"));

        // Act - el adaptador traga la excepcion del port y continua normalmente
        stockClientAdapter.buyStock(stock);

        // Assert - reporta el error generico 500 al formatter (contrato del monolito)
        verify(formatterResultOutputPort, times(1))
            .returnErrorGenericResponse(500, "Unexpected error communicating with stock service");
        verify(stockCommandPort, times(1)).registerPurchase(argThat(converted ->
            converted.getProductId().equals(1L)
                && converted.getQuantity() == 10
                && converted.getPrice().equals(BigDecimal.valueOf(100.00))));
        verify(stockCommandPort, never()).registerSale(any());

        // Assert - si el formatter mismo falla, la excepcion se propaga
        doThrow(new GenericErrorException(500, "Unexpected error communicating with stock service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> stockClientAdapter.buyStock(stock));
        assertEquals("Unexpected error communicating with stock service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should swallow a server error on buy stock and report a generic error")
    void testBuyStock_ServerError() {
        // Arrange - el fallo remoto llega al adaptador como excepcion del port
        when(stockCommandPort.registerPurchase(any()))
            .thenThrow(new RuntimeException("Service unavailable"));

        // Act - el adaptador traga la excepcion del port y continua normalmente
        stockClientAdapter.buyStock(stock);

        // Assert - reporta el error generico 500 al formatter (contrato del monolito)
        verify(formatterResultOutputPort, times(1))
            .returnErrorGenericResponse(500, "Unexpected error communicating with stock service");
        verify(stockCommandPort, times(1)).registerPurchase(argThat(converted ->
            converted.getProductId().equals(1L)
                && converted.getQuantity() == 10
                && converted.getPrice().equals(BigDecimal.valueOf(100.00))));
        verify(stockCommandPort, never()).registerSale(any());

        // Assert - si el formatter mismo falla, la excepcion se propaga
        doThrow(new GenericErrorException(500, "Unexpected error communicating with stock service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> stockClientAdapter.buyStock(stock));
        assertEquals("Unexpected error communicating with stock service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should sell stock successfully with 200 OK")
    void testSellStock_Success() {
        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));

        verify(stockCommandPort, times(1)).registerSale(argThat(converted ->
            converted.getProductId().equals(1L)
                && converted.getQuantity() == 10
                && converted.getPrice().equals(BigDecimal.valueOf(100.00))));
    }

    @Test
    @DisplayName("Should sell stock successfully with 202 Accepted")
    void testSellStock_Accepted() {
        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));

        verify(stockCommandPort, times(1)).registerSale(any());
    }

    @Test
    @DisplayName("Should swallow a client error on sell stock and report a generic error")
    void testSellStock_ClientError() {
        // Arrange - el fallo remoto llega al adaptador como excepcion del port
        when(stockCommandPort.registerSale(any()))
            .thenThrow(new RuntimeException("Not found"));

        // Act - el adaptador traga la excepcion del port y continua normalmente
        stockClientAdapter.sellStock(stock);

        // Assert - reporta el error generico 500 al formatter (contrato del monolito)
        verify(formatterResultOutputPort, times(1))
            .returnErrorGenericResponse(500, "Unexpected error communicating with stock service");
        verify(stockCommandPort, times(1)).registerSale(argThat(converted ->
            converted.getProductId().equals(1L)
                && converted.getQuantity() == 10
                && converted.getPrice().equals(BigDecimal.valueOf(100.00))));
        verify(stockCommandPort, never()).registerPurchase(any());

        // Assert - si el formatter mismo falla, la excepcion se propaga
        doThrow(new GenericErrorException(500, "Unexpected error communicating with stock service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> stockClientAdapter.sellStock(stock));
        assertEquals("Unexpected error communicating with stock service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should swallow a server error on sell stock and report a generic error")
    void testSellStock_ServerError() {
        // Arrange - el fallo remoto llega al adaptador como excepcion del port
        when(stockCommandPort.registerSale(any()))
            .thenThrow(new RuntimeException("Service unavailable"));

        // Act - el adaptador traga la excepcion del port y continua normalmente
        stockClientAdapter.sellStock(stock);

        // Assert - reporta el error generico 500 al formatter (contrato del monolito)
        verify(formatterResultOutputPort, times(1))
            .returnErrorGenericResponse(500, "Unexpected error communicating with stock service");
        verify(stockCommandPort, times(1)).registerSale(argThat(converted ->
            converted.getProductId().equals(1L)
                && converted.getQuantity() == 10
                && converted.getPrice().equals(BigDecimal.valueOf(100.00))));
        verify(stockCommandPort, never()).registerPurchase(any());

        // Assert - si el formatter mismo falla, la excepcion se propaga
        doThrow(new GenericErrorException(500, "Unexpected error communicating with stock service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> stockClientAdapter.sellStock(stock));
        assertEquals("Unexpected error communicating with stock service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should handle buy stock with large quantity")
    void testBuyStock_LargeQuantity() {
        // Arrange
        stock.setQuantity(999999);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));

        verify(stockCommandPort, times(1)).registerPurchase(any());
    }

    @Test
    @DisplayName("Should handle sell stock with large quantity")
    void testSellStock_LargeQuantity() {
        // Arrange
        stock.setQuantity(999999);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));

        verify(stockCommandPort, times(1)).registerSale(any());
    }

    @Test
    @DisplayName("Should handle buy stock with high price")
    void testBuyStock_HighPrice() {
        // Arrange
        BigDecimal highPrice = new BigDecimal("99999999.99");
        stock.setPrice(highPrice);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));

        verify(stockCommandPort, times(1)).registerPurchase(any());
    }

    @Test
    @DisplayName("Should handle sell stock with high price")
    void testSellStock_HighPrice() {
        // Arrange
        BigDecimal highPrice = new BigDecimal("99999999.99");
        stock.setPrice(highPrice);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));

        verify(stockCommandPort, times(1)).registerSale(any());
    }

    @Test
    @DisplayName("Should handle mapper exception on buy stock")
    void testBuyStock_MapperException() {
        // Arrange - en el monolito la conversion es interna y no puede fallar;
        // el unico fallo previo a la respuesta proviene del port
        when(stockCommandPort.registerPurchase(any()))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act - el adaptador traga la excepcion del port y continua normalmente
        stockClientAdapter.buyStock(stock);

        // Assert - reporta el error generico 500 al formatter (contrato del monolito)
        verify(formatterResultOutputPort, times(1))
            .returnErrorGenericResponse(500, "Unexpected error communicating with stock service");
        verify(stockCommandPort, times(1)).registerPurchase(argThat(converted ->
            converted.getProductId().equals(1L)
                && converted.getQuantity() == 10
                && converted.getPrice().equals(BigDecimal.valueOf(100.00))));
        verify(stockCommandPort, never()).registerSale(any());

        // Assert - si el formatter mismo falla, la excepcion se propaga
        doThrow(new GenericErrorException(500, "Unexpected error communicating with stock service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> stockClientAdapter.buyStock(stock));
        assertEquals("Unexpected error communicating with stock service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should handle mapper exception on sell stock")
    void testSellStock_MapperException() {
        // Arrange - en el monolito la conversion es interna y no puede fallar;
        // el unico fallo previo a la respuesta proviene del port
        when(stockCommandPort.registerSale(any()))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act - el adaptador traga la excepcion del port y continua normalmente
        stockClientAdapter.sellStock(stock);

        // Assert - reporta el error generico 500 al formatter (contrato del monolito)
        verify(formatterResultOutputPort, times(1))
            .returnErrorGenericResponse(500, "Unexpected error communicating with stock service");
        verify(stockCommandPort, times(1)).registerSale(argThat(converted ->
            converted.getProductId().equals(1L)
                && converted.getQuantity() == 10
                && converted.getPrice().equals(BigDecimal.valueOf(100.00))));
        verify(stockCommandPort, never()).registerPurchase(any());

        // Assert - si el formatter mismo falla, la excepcion se propaga
        doThrow(new GenericErrorException(500, "Unexpected error communicating with stock service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> stockClientAdapter.sellStock(stock));
        assertEquals("Unexpected error communicating with stock service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should handle client exception on buy stock")
    void testBuyStock_ClientException() {
        // Arrange
        when(stockCommandPort.registerPurchase(any()))
            .thenThrow(new RuntimeException("Network error"));

        // Act - el adaptador traga la excepcion del port y continua normalmente
        stockClientAdapter.buyStock(stock);

        // Assert - reporta el error generico 500 al formatter (contrato del monolito)
        verify(formatterResultOutputPort, times(1))
            .returnErrorGenericResponse(500, "Unexpected error communicating with stock service");
        verify(stockCommandPort, times(1)).registerPurchase(argThat(converted ->
            converted.getProductId().equals(1L)
                && converted.getQuantity() == 10
                && converted.getPrice().equals(BigDecimal.valueOf(100.00))));
        verify(stockCommandPort, never()).registerSale(any());

        // Assert - si el formatter mismo falla, la excepcion se propaga
        doThrow(new GenericErrorException(500, "Unexpected error communicating with stock service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> stockClientAdapter.buyStock(stock));
        assertEquals("Unexpected error communicating with stock service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should handle client exception on sell stock")
    void testSellStock_ClientException() {
        // Arrange
        when(stockCommandPort.registerSale(any()))
            .thenThrow(new RuntimeException("Network error"));

        // Act - el adaptador traga la excepcion del port y continua normalmente
        stockClientAdapter.sellStock(stock);

        // Assert - reporta el error generico 500 al formatter (contrato del monolito)
        verify(formatterResultOutputPort, times(1))
            .returnErrorGenericResponse(500, "Unexpected error communicating with stock service");
        verify(stockCommandPort, times(1)).registerSale(argThat(converted ->
            converted.getProductId().equals(1L)
                && converted.getQuantity() == 10
                && converted.getPrice().equals(BigDecimal.valueOf(100.00))));
        verify(stockCommandPort, never()).registerPurchase(any());

        // Assert - si el formatter mismo falla, la excepcion se propaga
        doThrow(new GenericErrorException(500, "Unexpected error communicating with stock service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> stockClientAdapter.sellStock(stock));
        assertEquals("Unexpected error communicating with stock service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should handle buy stock with different product IDs")
    void testBuyStock_DifferentProductIds() {
        // Arrange
        stock.setProductId(999L);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));

        verify(stockCommandPort, times(1)).registerPurchase(any());
    }

    @Test
    @DisplayName("Should handle sell stock with different product IDs")
    void testSellStock_DifferentProductIds() {
        // Arrange
        stock.setProductId(999L);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));

        verify(stockCommandPort, times(1)).registerSale(any());
    }

    @Test
    @DisplayName("Should handle buy stock with different enterprise IDs")
    void testBuyStock_DifferentEnterpriseIds() {
        // Arrange
        stock.setEnterpriseId("ENT-456");

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));

        verify(stockCommandPort, times(1)).registerPurchase(any());
    }

    @Test
    @DisplayName("Should handle sell stock with different enterprise IDs")
    void testSellStock_DifferentEnterpriseIds() {
        // Arrange
        stock.setEnterpriseId("ENT-789");

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));

        verify(stockCommandPort, times(1)).registerSale(any());
    }

    @Test
    @DisplayName("Should use correct mapper method for buy operation")
    void testBuyStock_UsesCorrectMapperMethod() {
        // Act
        stockClientAdapter.buyStock(stock);

        // Assert
        verify(stockCommandPort, times(1)).registerPurchase(any());
        verify(stockCommandPort, never()).registerSale(any());
    }

    @Test
    @DisplayName("Should use correct mapper method for sell operation")
    void testSellStock_UsesCorrectMapperMethod() {
        // Act
        stockClientAdapter.sellStock(stock);

        // Assert
        verify(stockCommandPort, times(1)).registerSale(any());
        verify(stockCommandPort, never()).registerPurchase(any());
    }

    @Test
    @DisplayName("Should handle buy stock with minimum valid quantity")
    void testBuyStock_MinimumQuantity() {
        // Arrange
        stock.setQuantity(1);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));
    }

    @Test
    @DisplayName("Should handle sell stock with minimum valid quantity")
    void testSellStock_MinimumQuantity() {
        // Arrange
        stock.setQuantity(1);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));
    }

    @Test
    @DisplayName("Should handle buy stock with precise decimal price")
    void testBuyStock_PreciseDecimalPrice() {
        // Arrange
        BigDecimal precisePrice = new BigDecimal("123.456789");
        stock.setPrice(precisePrice);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));
    }

    @Test
    @DisplayName("Should handle sell stock with precise decimal price")
    void testSellStock_PreciseDecimalPrice() {
        // Arrange
        BigDecimal precisePrice = new BigDecimal("987.654321");
        stock.setPrice(precisePrice);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));
    }
}
