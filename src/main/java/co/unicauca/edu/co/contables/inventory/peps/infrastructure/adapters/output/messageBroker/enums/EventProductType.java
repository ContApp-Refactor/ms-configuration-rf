package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.enums;

/**
 * @brief Enumeration of product lifecycle event types
 * 
 * Defines the types of changes that can occur to a product entity
 * propagated via the message broker.
 */
public enum EventProductType {
    CREATED, ///< Product creation event
    UPDATED, ///< Product update event
    DELETED  ///< Product deletion event
}
