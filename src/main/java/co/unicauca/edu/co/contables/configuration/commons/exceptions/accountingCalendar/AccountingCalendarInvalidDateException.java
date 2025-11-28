package co.unicauca.edu.co.contables.configuration.commons.exceptions.accountingCalendar;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para fecha inválida en calendario contable
 *
 * Se lanza cuando se proporciona una fecha inválida en operaciones
 * relacionadas con el calendario contable, permitiendo mensajes personalizados.
 */
public class AccountingCalendarInvalidDateException extends BaseBusinessException {

    public AccountingCalendarInvalidDateException() {
        super(AccountingCalendarErrorCode.INVALID_DATE);
    }

    public AccountingCalendarInvalidDateException(String customMessage) {
        super(AccountingCalendarErrorCode.INVALID_DATE, customMessage);
    }

    public AccountingCalendarInvalidDateException(String customMessage, Throwable cause) {
        super(AccountingCalendarErrorCode.INVALID_DATE, customMessage, cause);
    }
}
