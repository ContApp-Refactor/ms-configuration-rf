package co.unicauca.edu.co.contables.accounting.debt_payments.application.output;

public interface IThirdEventPublisher {
    /**
     * @brief Publish an event indicating that a third party has been used.
     * @param thirdId The ID of the third party.
     * @param enterpriseId The ID of the enterprise.
     */
    void publishThirdUsedEvent(Long thirdId, String enterpriseId);
}
