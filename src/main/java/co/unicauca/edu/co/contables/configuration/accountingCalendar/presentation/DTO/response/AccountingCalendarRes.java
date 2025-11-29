package co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.response;

import lombok.*;

import java.time.LocalDate;

/**
 * @brief DTO de respuesta para el calendario contable
 *
 * Representa la estructura de datos enviada como respuesta en las operaciones
 * del calendario contable, incluyendo ID, empresa, tenant y fecha.
 */
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


