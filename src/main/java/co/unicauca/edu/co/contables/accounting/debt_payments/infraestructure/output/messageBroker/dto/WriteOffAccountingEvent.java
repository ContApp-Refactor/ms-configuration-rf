package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto;

public class WriteOffAccountingEvent extends EventDto<PortfolioWriteOffEventDto> {

    public static final String WRITEOFF_CONFIRMED = "WRITEOFF_CONFIRMED";
    public static final String WRITEOFF_VOIDED = "WRITEOFF_VOIDED";

    public WriteOffAccountingEvent(String type, PortfolioWriteOffEventDto data) {
        super(type, data);
    }
}
