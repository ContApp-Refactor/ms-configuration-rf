package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for adjustment entry response
 * 
 * Represents the details of a registered positive inventory adjustment.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdjustmentEntryDTOResponse {
    private Long idKardex;
    private ZonedDateTime date;
    private String details;
    private int quantity;
    private BigDecimal unitPrice;
    private String type;
    private String factCode;
}
