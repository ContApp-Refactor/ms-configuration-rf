package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto;

public class InvoiceDueReminderEvent extends EventDto<InvoiceDueReminderEventDto> {

    public static final String TYPE = "INVOICE_DUE_REMINDER";

    public InvoiceDueReminderEvent(InvoiceDueReminderEventDto data) {
        super(TYPE, data);
    }
}
