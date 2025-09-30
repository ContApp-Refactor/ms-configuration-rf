package co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.models;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingCalendar {
    private Long id;
    private String idEnterprise;    
    private LocalDate date;
    private String tenantId;
}


