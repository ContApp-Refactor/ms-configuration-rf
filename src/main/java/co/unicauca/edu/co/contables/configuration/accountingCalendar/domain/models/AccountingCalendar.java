package co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.models;

import lombok.*;

import java.time.LocalDate;

/**
 * @brief Modelo de dominio para el calendario contable
 *
 * Representa el concepto de calendario contable en la capa de dominio,
 * conteniendo la información básica de fechas asociadas a una empresa.
 */
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


