package co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums;

public enum AccountingEntryStatus {
    /** El asiento es válido y está activo. */
    ACTIVE,
    /** El asiento ha sido anulado por un asiento de reversión. */
    VOIDED
}
