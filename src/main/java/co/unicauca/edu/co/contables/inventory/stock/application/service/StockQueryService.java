package co.unicauca.edu.co.contables.inventory.stock.application.service;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockQueryPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.config.i18n.MessageKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockQueryService implements IStockQueryPort {

    private final IStockQueryRepositoryPort stockQueryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;

    @Override
    public Stock findByProductId(Long id) {
        log.info("Buscando stock con id: {}", id);
        Stock stock = stockQueryPort.findByProductId(id);
        if (stock == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, id));
        }
        return stock;
    }

    @Override
    public boolean existsByProductId(Long id) {
        boolean exists = stockQueryPort.existsByProductId(id);
        return exists;
    }
}
