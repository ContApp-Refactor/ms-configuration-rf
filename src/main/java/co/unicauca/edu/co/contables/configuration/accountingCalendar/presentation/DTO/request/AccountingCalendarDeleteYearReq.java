package co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @brief DTO de solicitud para eliminar por año
 *
 * Estructura de datos para solicitudes de eliminación de registros del calendario
 * contable filtrados por empresa y año específico.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingCalendarDeleteYearReq {

    @NotBlank
    private String idEnterprise;

    @Min(2000)
    private int year;
}



