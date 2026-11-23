package co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external;

import java.time.LocalDate;

/**
 * @brief Output port for configuration service client
 * 
 * Provides interface for validating configuration and business rules
 * against external configuration services.
 */
public interface IConfigClientPort {
    /**
     * @brief Validates if a date is within a valid accounting period
     * @param enterpriseId Enterprise identifier
     * @param date Date to validate
     * @return True if date is valid for accounting, false otherwise
     */
    boolean isValidAccountingDate(String enterpriseId, LocalDate date);
}
