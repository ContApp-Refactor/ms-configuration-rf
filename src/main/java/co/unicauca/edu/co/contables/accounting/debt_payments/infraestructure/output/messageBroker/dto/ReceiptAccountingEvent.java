package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto;

public class ReceiptAccountingEvent extends EventDto<ReceiptEventDto> {

    public static final String RECEIPT_CREATED = "RECEIPT_CREATED";
    public static final String RECEIPT_VOIDED = "RECEIPT_VOIDED";

    public ReceiptAccountingEvent(String type, ReceiptEventDto data) {
        super(type, data);
    }
}
