package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for filtering kardex records by date range
 * 
 * Used in query operations to specify the time period for retrieving inventory movements.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KardexByDateDTORequest {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
