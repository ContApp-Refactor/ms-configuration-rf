package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.enums.EventType;

public class ProductSyncEvent extends EventDto<ProductSyncDto, EventType> {

    public ProductSyncEvent(ProductSyncDto data, EventType type) {
        super(data, type);
    }
}
