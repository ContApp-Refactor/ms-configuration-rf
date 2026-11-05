package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for a kardex record report
 * 
 * Represents a complete kardex movement entry, including entry/exit details and balance.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KardexRecordsDTOResponse {
    private Long idKardex;
    private ZonedDateTime date;
    private String detail;

    private Integer entryQuantity;
    private BigDecimal entryUnitPrice;
    private BigDecimal entryTotalPrice;

    private List<SaleDetailDTO> outputDetails;  
    
    private List<BalanceDTO> balance;


}
