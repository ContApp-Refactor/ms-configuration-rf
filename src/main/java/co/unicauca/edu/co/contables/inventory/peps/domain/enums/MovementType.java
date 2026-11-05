package co.unicauca.edu.co.contables.inventory.peps.domain.enums;

/**
 * @brief Enumeration of inventory movement types
 * 
 * Defines the various types of transactions that can affect inventory levels
 * and valuation, including commercial and non-commercial movements.
 */
public enum MovementType {
    /** @brief Entry of goods through purchase */
    PURCHASE,

    /** @brief Exit of goods through sale */
    SALE, 

    /** @brief Initial inventory balance */
    OPENING_BALANCE,

    /** @brief Return of goods to supplier */
    PURCHASERETURN,

    /** @brief Return of goods from customer */
    SALESRETURN,

    /** @brief Exit of goods for non-commercial reasons (e.g., damage, internal use) */
    NONCOMMERCIALEXIT,

    /** @brief Entry of goods for non-commercial reasons (e.g., surplus, gifts) */
    NONCOMMERCIALENTRY,

    ADJUSTMENTEXIT,

    ADJUSTMENTENTRY
}
