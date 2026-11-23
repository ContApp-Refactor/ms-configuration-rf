package co.unicauca.edu.co.contables.inventory.peps.application.service.command;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IStockClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service for stock integration
 * 
 * Handles communication with external stock services to synchronize
 * inventory changes resulting from kardex movements.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockIntegrationService {
    private final IStockClientPort stockClient;

     /**
     * @brief Creates a Stock object from Kardex data
     * @param kardex Source kardex record
     * @return Created Stock object
     */
    public Stock createStock(Kardex kardex) {
        return Stock.builder()
            .productId(kardex.getProduct().getProductId())
            .quantity(kardex.getQuantity())
            .price(kardex.getUnitPrice())
            .build();
    }

    
    /**
     * @brief Calls stock service to update inventory
     * @param stock Stock information to update
     * @param isBuy True if purchase (increase), false if sale (decrease)
     */
    public void callApiStockService(Stock stock, boolean isBuy) {
        try {
            if (isBuy) {
                stockClient.buyStock(stock);
            } else {
                stockClient.sellStock(stock);
            }
            log.info("{} stock updated successfully for product: {}", (isBuy ? "Purchase/Return" : "Sale"), stock.getProductId());
        } catch (Exception e) {
          log.warn("Failed to update stock for {} - Kardex will be saved but stock service was not synchronized: {}", 
                (isBuy ? "purchase" : "sale"), e.getMessage());
        }
    }


    
}
