package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for receiving sales operations in the Kardex system
 * Contains fields necessary for registering a product sale
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexSalesDtoRequest {
    private int quantity;

    private Long factCode;

    private Long productId;

    private String details;
}

