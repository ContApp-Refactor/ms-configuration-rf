package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.record;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;

public record StockPartition(List<Stock> newProducts, List<Stock> existingProducts) {}
    
