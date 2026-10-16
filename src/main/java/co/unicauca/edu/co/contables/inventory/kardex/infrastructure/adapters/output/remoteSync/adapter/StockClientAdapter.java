package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.adapter;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.external.IStockClientPort;
import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockCommandPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief In-process adapter for the stock module (paso 7).
 *
 * Replaces the former WebClient proxy to lb://STOCK: stock already lives in
 * this monolith, so purchases and sales are registered through
 * {@link IStockCommandPort} directly.
 */
@Component("kardexStockClientAdapter")
@RequiredArgsConstructor
@Slf4j
public class StockClientAdapter implements IStockClientPort {

    private final IStockCommandPort stockCommandPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    @Override
    public void buyStock(Stock stock) {
        try {
            stockCommandPort.registerPurchase(toStockModuleDomain(stock));
        } catch (Exception e) {
            log.error("Unexpected error when calling stock service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500,
                    "Unexpected error communicating with stock service");
        }
    }

    @Override
    public void sellStock(Stock stock) {
        try {
            stockCommandPort.registerSale(toStockModuleDomain(stock));
        } catch (Exception e) {
            log.error("Unexpected error when calling stock service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500,
                    "Unexpected error communicating with stock service");
        }
    }

    private co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock toStockModuleDomain(Stock source) {
        co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock target =
                new co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock();
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity());
        target.setPrice(source.getPrice());
        return target;
    }
}
