package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.mapper.StockEntityMapper;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockQueryAdapter implements IStockQueryRepositoryPort {

    private final StockRepository stockRepository;
    private final StockEntityMapper stockEntityMapper;

    @Override
    public Stock findByProductId(Long id) {
        return stockRepository.findByProductId(id) != null ? stockEntityMapper.toDomain(stockRepository.findByProductId(id)) : null;
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return stockRepository.existsByProductId(productId);
    } 
}
