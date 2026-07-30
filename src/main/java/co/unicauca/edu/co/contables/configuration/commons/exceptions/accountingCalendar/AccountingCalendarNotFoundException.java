package co.unicauca.edu.co.contables.configuration.commons.exceptions.accountingCalendar;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para calendario contable no encontrado
 *
 * Se lanza cuando no se encuentra un calendario contable específico
 * en las operaciones del sistema de configuración.
 */
public class AccountingCalendarNotFoundException extends BaseBusinessException {

    public AccountingCalendarNotFoundException() {
        super(AccountingCalendarErrorCode.NOT_FOUND);
    }
}


 