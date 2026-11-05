package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.record;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;

public record ProductPartition(List<Product> newProducts, List<Product> existingProducts) {}
    
