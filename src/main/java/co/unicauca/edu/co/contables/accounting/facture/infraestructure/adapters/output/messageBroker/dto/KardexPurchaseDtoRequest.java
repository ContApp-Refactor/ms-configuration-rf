package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for receiving purchase operations in the Kardex system
 * Contains fields necessary for registering a product purchase
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexPurchaseDtoRequest {

    private int quantity;

    private Long factCode;

    private BigDecimal unitPrice;

    private Long productId;

    private String details;
}
