package co.unicauca.edu.co.contables.inventory.kardex.domain.port.product;

public interface IProductEventPort {
    void publishUsedProductEvent(Long productId, Integer quantityUsed);

}
