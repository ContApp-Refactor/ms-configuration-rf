package co.unicauca.edu.co.contables.billing.domain.model;

/** Familia del documento: venta o compra. */
public enum DocumentKind {
    SALE(CounterpartyRole.CUSTOMER),
    PURCHASE(CounterpartyRole.SUPPLIER);

    private final CounterpartyRole counterpartyRole;

    DocumentKind(CounterpartyRole counterpartyRole) {
        this.counterpartyRole = counterpartyRole;
    }

    public CounterpartyRole counterpartyRole() {
        return counterpartyRole;
    }
}
