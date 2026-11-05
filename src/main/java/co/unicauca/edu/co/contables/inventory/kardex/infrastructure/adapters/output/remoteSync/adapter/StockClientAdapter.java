package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.adapter;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.external.IStockClientPort;
import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockCommandPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief In-process adapter for the stock module (paso 7).
 *
 * Replaces the former WebClient proxy to lb://STOCK: stock already lives in
 * this monolith, so purchases and sales are registered through
 * {@link IStockCommandPort} directly.
 *
 * Error contract: business errors of the stock module are NOT hidden as a
 * generic 500 any more (callers such as billing need to tell "insufficient
 * stock" from "service down"):
 * <ul>
 *   <li>{@link BaseException} (e.g. stock not found, 404) is propagated as is;</li>
 *   <li>the stock domain rules ({@code IllegalStateException} "Stock is not active",
 *       {@code IllegalArgumentException} invalid amount, i.e. not enough stock on a sale)
 *       become a 400 business rule error;</li>
 *   <li>anything else is still reported as the generic 500 error.</li>
 * </ul>
 */
@Component("kardexStockClientAdapter")
@RequiredArgsConstructor
@Slf4j
public class StockClientAdapter implements IStockClientPort {

    static final String GENERIC_ERROR = "Unexpected error communicating with stock service";
    static final String INSUFFICIENT_STOCK = "Insufficient stock in stock module: ";

    private final IStockCommandPort stockCommandPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    @Override
    public void buyStock(Stock stock) {
        try {
            stockCommandPort.registerPurchase(toStockModuleDomain(stock));
        } catch (BaseException e) {
            throw e;
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Stock module rejected the purchase of product {}: {}", stock.getProductId(), e.getMessage());
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when calling stock service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500, GENERIC_ERROR);
        }
    }

    @Override
    public void sellStock(Stock stock) {
        try {
            stockCommandPort.registerSale(toStockModuleDomain(stock));
        } catch (BaseException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Stock module rejected the sale of product {}: {}", stock.getProductId(), e.getMessage());
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, INSUFFICIENT_STOCK + e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Stock module rejected the sale of product {}: {}", stock.getProductId(), e.getMessage());
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when calling stock service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500, GENERIC_ERROR);
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
