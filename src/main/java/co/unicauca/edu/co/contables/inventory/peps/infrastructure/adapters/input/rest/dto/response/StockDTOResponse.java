package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for stock response
 * 
 * Represents the current stock status of a product.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockDTOResponse {
    
    private Long id;

    private Long idProduct;

    private int quantity;

    private Double price;


    private boolean status;

}
