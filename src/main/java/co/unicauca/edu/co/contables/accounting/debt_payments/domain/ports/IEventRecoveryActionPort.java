package co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports;


/**
 * @brief Port for defining recovery actions when event processing fails
 * 
 * Enables implementation of specific recovery strategies for different
 * types of events, providing resilience to message processing failures.
 * 
 * @param <T> Type of event for which recovery is executed
 */
public interface IEventRecoveryActionPort<T> {
    
    /**
     * @brief Executes recovery action when normal event processing fails
     * @param event The event that failed to be processed
     * @return True if recovery was successful, false otherwise
     */
    boolean executeRecoveryAction(T event);

    /**
     * @brief Determines if this port can handle recovery for the given event type
     * @param event The event to validate
     * @return True if can handle recovery, false otherwise
     */
    boolean canHandle(T event);
}
