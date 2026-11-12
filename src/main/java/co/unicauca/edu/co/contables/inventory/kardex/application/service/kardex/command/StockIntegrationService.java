package co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.command;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.external.IStockClientPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service for integrating inventory movements with external stock system
 * 
 * Handles communication with stock service to maintain real-time
 * inventory synchronization across systems.
 */
@Service("kardexStockIntegrationService")@RequiredArgsConstructor
@Slf4j
public class StockIntegrationService {
    
    private final IStockClientPort stockClient;

    /**
     * @brief Creates a Stock object from Kardex data
     * @param kardex Source kardex record
     * @return Stock object for external API call
     */
    public Stock createStock(Kardex kardex) {
        return Stock.builder()
            .productId(kardex.getProductId())
            .quantity(kardex.getQuantity())
            .price(kardex.getBalanceUnitPrice())
            .build();
    }

    /**
     * @brief Calls stock service API to update inventory
     * @param stock Stock data to send
     * @param isBuy True for purchase/return operations, false for sales
     * @throws RuntimeException propagated from the stock client so the enclosing transaction
     *         (kardex movement and any caller, e.g. billing) rolls back
     */
    public void callApiStockService(Stock stock, boolean isBuy) {
        if (isBuy) {
            stockClient.buyStock(stock);
        } else {
            stockClient.sellStock(stock);
        }
        log.info("{} stock updated successfully for product: {}", (isBuy ? "Purchase/Return" : "Sale"), stock.getProductId());
    }
}
