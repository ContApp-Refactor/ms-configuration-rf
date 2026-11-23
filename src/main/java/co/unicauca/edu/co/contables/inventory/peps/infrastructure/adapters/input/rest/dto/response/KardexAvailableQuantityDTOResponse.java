package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for available quantity in a kardex lot
 * 
 * Represents a purchase lot with its current available quantity.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KardexAvailableQuantityDTOResponse {
     private Long idKardex;
    private ZonedDateTime date;
    private String details;
    private int availableQuantity;
    private BigDecimal unitPrice;
    private String type;
    private String factCode;
}
