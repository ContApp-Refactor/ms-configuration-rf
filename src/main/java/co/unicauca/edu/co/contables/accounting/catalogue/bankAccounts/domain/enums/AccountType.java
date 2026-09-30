package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums;

/**
 * @brief Tipos de cuenta bancaria soportados en el sistema
 *
 * Enum que define las categorías de cuentas bancarias disponibles:
 * cuentas de ahorros y cuentas corrientes.
 */
public enum AccountType {
    AHORROS("Cuenta de Ahorros"),
    CORRIENTE("Cuenta Corriente");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return this.name();
    }

    @Override
    public String toString() {
        return this.name() + " - " + this.description;
    }
}
