package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.validation.ValidDateRange;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@ValidDateRange
public class KardexFilterDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
}
