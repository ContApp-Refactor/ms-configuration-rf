package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for stock update request
 * 
 * Represents the data for updating stock information.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockDTORequest {
    @JsonIgnore
    private Long id;

    private Long idProduct;

    private int quantity;

    private Double price;


    private boolean status=true;


}
