package co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums;

/**
 * @brief Enumeration representing the status of an invoice.
 * This enum defines the possible states an invoice can be in,
 * such as pending, paid, pending written off, and written off.
 */
public enum InvoiceStatus {
    PENDING,
    PAID,
    PENDING_WRITTEN_OFF,
    WRITTEN_OFF
}
