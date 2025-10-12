package co.unicauca.edu.co.contables.configuration.commons.exceptions.accountingCalendar;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

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
