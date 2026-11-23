package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.enums;

/**
 *  @brief Enumeration of invoice/movement event types
 * 
 * Defines the various types of commercial and non-commercial operations
 * that can trigger inventory movements.
 */
public enum EventFactureType {
    PURCHASE,           ///< Purchase of goods
    SALE,               ///< Sale of goods
    RETURNONSALE,       ///< Return of sold goods
    RETURNONPURCHASE,   ///< Return of purchased goods
    NONCOMMERCIALEXIT,  ///< Inventory exit not related to a sale (e.g., damage, internal use)
    NONCOMMERCIALENTRY  ///< Inventory entry not related to a purchase (e.g., adjustment)
}
