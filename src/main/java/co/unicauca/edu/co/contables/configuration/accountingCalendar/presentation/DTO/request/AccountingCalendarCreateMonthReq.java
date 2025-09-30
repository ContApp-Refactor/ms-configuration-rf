package co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingCalendarCreateMonthReq {

    @NotBlank
    private String idEnterprise;

    @Min(2000)
    private int year;

    @Min(1)
    @Max(12)
    private int month;
}



