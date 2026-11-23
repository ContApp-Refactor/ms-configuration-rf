package co.unicauca.edu.co.contables.accounting.debt_payments.application.output;

public interface IAccountEventPublisher {
    /**
     * Publishes an event when a new account is created.
     * @param account The ID of the created account.
     * @param enterpriseId The ID of the enterprise associated with the account.
     */
    void publishAccountCreatedEvent(Long account, String enterpriseId);
}
