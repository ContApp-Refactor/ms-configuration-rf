package co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * @brief DTO de solicitud para crear un registro individual
 *
 * Estructura de datos para solicitudes de creación de un único registro
 * en el calendario contable, con validaciones de formato de fecha.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingCalendarCreateReq {

    @NotBlank
    private String idEnterprise;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", 
             message = "La fecha debe tener el formato YYYY-MM-DD")
    private String date;
}


