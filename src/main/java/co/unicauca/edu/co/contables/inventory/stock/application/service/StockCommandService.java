package co.unicauca.edu.co.contables.inventory.stock.application.service;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockCommandPort;
import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockStatusPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockStatusRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.config.i18n.MessageKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockCommandService implements IStockCommandPort, IStockStatusPort {

    private final IStockCommandRepositoryPort stockCommandPort;
    private final IStockQueryRepositoryPort stockQueryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IStockStatusRepositoryPort stockStatusPort;
    private final IMessageServicePort messageService;


    @Override
    public void inactivate(Long productId) {
        if (!stockQueryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, productId));
        }

        stockStatusPort.inactivate(productId);
    }

    @Override
    public void activate(Long productId) {
        if (!stockQueryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, productId));
        }

        stockStatusPort.activate(productId);
    }

    @Override
    public Stock registerSale(Stock stock) {
        Stock oldStock = stockQueryPort.findByProductId(stock.getProductId());
        if(oldStock == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, stock.getProductId()));
        }
        // The Sell method will throw an exception if the stock is not active or if the amount is invalid.
        oldStock.sell(stock.getQuantity(), stock.getPrice());
        log.info("Stock after sale: {}", oldStock);
        return stockCommandPort.registerSale(oldStock);
    }

    @Override
    public Stock registerPurchase(Stock stock) {
        Stock oldStock = stockQueryPort.findByProductId(stock.getProductId());
        if(oldStock == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, stock.getProductId()));
        }
        // The Buy method will throw an exception if the stock is not active or if the amount or price is invalid.
        oldStock.buy(stock.getQuantity(), stock.getPrice());
        log.info("Stock after purchase: {}", oldStock);
        return stockCommandPort.registerPurchase(oldStock);
    }

    @Override
    public Stock save(Stock stock) {
        if (stockQueryPort.existsByProductId(stock.getProductId())) {
            formatterResultOutputPort.returnEntityAlreadyExistsErrorResponse(400, messageService.getMessage(MessageKeys.ERROR_DUPLICATE_RECORD,"Product:", stock.getProductId()));
        }
        return stockCommandPort.save(stock);
    }

    @Override
    public Stock update(Long productId, String name) {
        if (!stockQueryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, productId));
        }
        return stockCommandPort.update(productId, name);
    }

    @Override
    public String deleteAllByEnterpriseId(String enterpriseId) {
        log.info("Executing delete all stock operation for enterprise: {}", enterpriseId);
        String result = stockCommandPort.deleteAllByEnterpriseId(enterpriseId);
        log.info("Delete all stock operation completed for enterprise {}: {}", enterpriseId, result);
        return result;
    }
}

