package co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @brief DTO de solicitud para crear por mes
 *
 * Estructura de datos para solicitudes de creación de registros del calendario
 * contable en lote para un mes específico de una empresa.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingCalendarCreateMonthReq {

    @NotBlank
    private String idEnterprise;

    @Min(value = 2000)
    @Max(value = 9999)
    private int year;

    @Min(value = 1)
    @Max(value = 12)
    private int month;
}



