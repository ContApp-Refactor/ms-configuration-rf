package co.unicauca.edu.co.contables.accounting.debt_payments.application.input;

public interface IInvoiceNotificationUseCase {
    /**
     * Process and publish due invoices.
     */
    public void processAndPublishDueInvoices();
}
