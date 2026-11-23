package co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.product;

public interface IProductCommandPort {
    /**
     * @brief Deletes a product by its ID and enterprise ID
    * @param productId Product ID to delete
    * @return Status message indicating operation result
    */
    String deleteById(Long productId);
    
    /**
     * @brief Deletes all products for a specific enterprise
    * @param enterpriseId Enterprise ID
    * @return Status message indicating operation result
    */
    String deleteAllByEnterpriseId(String enterpriseId);
}
