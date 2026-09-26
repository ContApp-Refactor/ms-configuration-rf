package co.unicauca.edu.co.contables.accounting.debt_payments.application.output;

public interface ICostCenterEventPublisher {
    /**
     * Publishes an event when a cost center is used.
     * @param costCenterId The ID of the used cost center.
     * @param enterpriseId The ID of the enterprise associated with the cost center.
     */
    void publishCostCenterUsedEvent(Long costCenterId, String enterpriseId);
}
