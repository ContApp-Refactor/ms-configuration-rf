package co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums;

/**
 * @brief Enumeration representing the write-off status of a debt.
 * This enum defines the different states a write-off can be in, such as pending confirmation, confirmed, and voided.
 */
public enum WriteOffStatus {
    PENDING_CONFIRMATION, // Guardado, pero sin afectar saldos ni contabilidad.
    CONFIRMED,            // Afecta saldos y notifica a contabilidad.
    VOIDED                // Anulado, revierte saldos y notifica a contabilidad.
}
