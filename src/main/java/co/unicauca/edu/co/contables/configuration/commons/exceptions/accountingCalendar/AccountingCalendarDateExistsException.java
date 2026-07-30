package co.unicauca.edu.co.contables.configuration.commons.exceptions.accountingCalendar;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para fecha existente en calendario contable
 *
 * Se lanza cuando ya existe una fecha para el día especificado
 * en el calendario contable, evitando duplicados.
 */
public class AccountingCalendarDateExistsException extends BaseBusinessException {

    public AccountingCalendarDateExistsException() {
        super(AccountingCalendarErrorCode.DATE_ALREADY_EXISTS);
    }
}
