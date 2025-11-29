package co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @brief DTO de solicitud para crear por año
 *
 * Estructura de datos para solicitudes de creación de registros del calendario
 * contable en lote para un año completo de una empresa.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingCalendarCreateYearReq {

    @NotBlank
    private String idEnterprise;

    @Min(value = 2000)
    @Max(value = 9999)
    private int year;
}



