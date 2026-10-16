package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.remoteSync.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.GenericErrorException;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter.StockClientAdapter;
import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockCommandPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;

/**
 * @brief Unit tests for StockClientAdapter
 *
 * Antes de la migración este adaptador llamaba por HTTP a {@code lb://STOCK}.
 * Ahora delega en {@link IStockCommandPort} del monolito y proyecta el modelo
 * de peps sobre el de stock copiando solo {@code productId}, {@code quantity}
 * y {@code price}: los únicos campos que viajaban por HTTP.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockClientAdapter Tests")
class StockClientAdapterUnitTest {

    @Mock
    private IStockCommandPort stockCommandPort;

    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;

    @InjectMocks
    private StockClientAdapter stockClientAdapter;

    private Stock pepsStock;

    @BeforeEach
    void setUp() {
        pepsStock = Stock.builder()
            .id(55L)
            .productId(10L)
            .enterpriseId("ENT-001")
            .quantity(3)
            .price(new BigDecimal("1500.50"))
            .status(true)
            .build();
    }

    @Test
    @DisplayName("Should register a purchase through the in-process stock port")
    void testBuyStock_DelegatesToRegisterPurchase() {
        stockClientAdapter.buyStock(pepsStock);

        verify(stockCommandPort, times(1)).registerPurchase(any());
        verify(stockCommandPort, never()).registerSale(any());
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should register a sale through the in-process stock port")
    void testSellStock_DelegatesToRegisterSale() {
        stockClientAdapter.sellStock(pepsStock);

        verify(stockCommandPort, times(1)).registerSale(any());
        verify(stockCommandPort, never()).registerPurchase(any());
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should copy productId, quantity and price when buying")
    void testBuyStock_ProjectionCopiesHttpFields() {
        stockClientAdapter.buyStock(pepsStock);

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock> captor =
            ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock.class);
        verify(stockCommandPort, times(1)).registerPurchase(captor.capture());

        var projected = captor.getValue();
        assertEquals(10L, projected.getProductId());
        assertEquals(3, projected.getQuantity());
        assertEquals(0, new BigDecimal("1500.50").compareTo(projected.getPrice()));
    }

    @Test
    @DisplayName("Should drop the fields the stock module resolves on its own")
    void testBuyStock_ProjectionDropsLocalFields() {
        stockClientAdapter.buyStock(pepsStock);

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock> captor =
            ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock.class);
        verify(stockCommandPort).registerPurchase(captor.capture());

        var projected = captor.getValue();
        assertNull(projected.getId(), "the local id belongs to the stock module, not to peps");
        assertNull(projected.getEnterpriseId(), "enterprise resolution belongs to the stock module");
        assertNull(projected.getName());
        assertFalse(projected.isState(),
            "state is not copied: the stock module resolves it from the existing record");
    }

    @Test
    @DisplayName("Should copy the same fields when selling")
    void testSellStock_ProjectionCopiesHttpFields() {
        stockClientAdapter.sellStock(pepsStock);

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock> captor =
            ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock.class);
        verify(stockCommandPort).registerSale(captor.capture());

        var projected = captor.getValue();
        assertEquals(10L, projected.getProductId());
        assertEquals(3, projected.getQuantity());
        assertEquals(0, new BigDecimal("1500.50").compareTo(projected.getPrice()));
    }

    @Test
    @DisplayName("Should not mutate the incoming peps Stock")
    void testBuyStock_DoesNotMutateSource() {
        stockClientAdapter.buyStock(pepsStock);

        assertEquals(55L, pepsStock.getId());
        assertEquals("ENT-001", pepsStock.getEnterpriseId());
        assertTrue(pepsStock.isStatus());
        assertEquals(3, pepsStock.getQuantity());
    }

    @Test
    @DisplayName("Should swallow a failure and report 500 when buying fails")
    void testBuyStock_CommandPortThrows_ReportsGenericError() {
        when(stockCommandPort.registerPurchase(any())).thenThrow(new IllegalStateException("stock unavailable"));

        stockClientAdapter.buyStock(pepsStock);

        verify(formatterResultOutputPort)
            .returnErrorGenericResponse(500, "Unexpected error communicating with stock service");
    }

    @Test
    @DisplayName("Should swallow a failure and report 500 when selling fails")
    void testSellStock_CommandPortThrows_ReportsGenericError() {
        when(stockCommandPort.registerSale(any())).thenThrow(new IllegalStateException("stock unavailable"));

        stockClientAdapter.sellStock(pepsStock);

        verify(formatterResultOutputPort)
            .returnErrorGenericResponse(500, "Unexpected error communicating with stock service");
    }

    @Test
    @DisplayName("Should propagate the error when the formatter itself refuses the request")
    void testBuyStock_FormatterThrows_PropagatesError() {
        when(stockCommandPort.registerPurchase(any())).thenThrow(new IllegalStateException("stock unavailable"));
        doThrow(new GenericErrorException(500, "Unexpected error communicating with stock service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> stockClientAdapter.buyStock(pepsStock));

        assertEquals("Unexpected error communicating with stock service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should propagate the error on the sale path too")
    void testSellStock_FormatterThrows_PropagatesError() {
        when(stockCommandPort.registerSale(any())).thenThrow(new IllegalStateException("stock unavailable"));
        doThrow(new GenericErrorException(500, "Unexpected error communicating with stock service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> stockClientAdapter.sellStock(pepsStock));

        assertEquals("Unexpected error communicating with stock service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should forward negative quantities untouched (validation belongs to the stock module)")
    void testSellStock_ForwardsNegativeQuantity() {
        pepsStock.setQuantity(-4);

        stockClientAdapter.sellStock(pepsStock);

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock> captor =
            ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock.class);
        verify(stockCommandPort).registerSale(captor.capture());
        assertEquals(-4, captor.getValue().getQuantity());
    }
}
