package co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingCalendarRes {
    private Long id;
    private String idEnterprise;
    private String tenantId;
    private LocalDate date;
}


