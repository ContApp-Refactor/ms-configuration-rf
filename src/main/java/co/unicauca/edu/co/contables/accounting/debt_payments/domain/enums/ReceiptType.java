package co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums;

/**
 * @brief Enumeration representing the type of receipt.
 * This enum defines the different types of receipts,
 * such as invoice payments and direct income.
 */
public enum ReceiptType {
    INVOICE_PAYMENT(1L, "Invoice Payment"),
    DIRECT_INCOME(2L, "Direct Income");

    private Long id;
    private final String description;

    ReceiptType(Long id, String description) {
        this.id = id;
        this.description = description;
    }
    ReceiptType() {
        this.id = null;
        this.description = null;
    }

    public Long getId() {
        return id;
    }

    public void set(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }
}
