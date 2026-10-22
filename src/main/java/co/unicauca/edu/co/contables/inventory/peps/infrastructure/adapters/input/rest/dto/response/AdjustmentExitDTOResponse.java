package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for adjustment exit response
 * 
 * Represents the details of a registered negative inventory adjustment.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdjustmentExitDTOResponse {
     private Long idKardex;
     private ZonedDateTime date;
     private String details;
     private int quantity;
     private BigDecimal unitPrice;
     private String type;
     private String factCode;
}
