package co.unicauca.edu.co.contables.accounting.debt_payments.application.input;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;

public interface IAccountingEventPublisher {
    /**
     * Publish a event when a receipt is created.
     * @param receipt The created receipt.
     */
    void publishReceiptCreatedEvent(Receipt receipt);

    /**
     * Publish a event when a receipt is voided.
     * @param receipt The voided receipt.
     */
    void publishVoidReceiptEvent(Receipt receipt);

    /**
     * Publish a event when a portfolio write-off is confirmed.
     * @param writeOff The confirmed portfolio write-off domain object.
     */
    void publishWriteOffConfirmedEvent(PortfolioWriteOff writeOff);

    /**
     * Publish a event when a portfolio write-off is voided.
     * @param writeOff The voided portfolio write-off domain object.
     */
    void publishWriteOffVoidedEvent(PortfolioWriteOff writeOff);
}
