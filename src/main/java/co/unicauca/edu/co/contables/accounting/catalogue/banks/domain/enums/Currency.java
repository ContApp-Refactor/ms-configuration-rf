package co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.enums;

/**
 * @brief Monedas soportadas por los bancos en el sistema
 *
 * Enum que define las divisas admitidas para operaciones bancarias:
 * pesos colombianos, dólares, euros, libras, francos y yenes.
 */
public enum Currency {
    COP("Peso Colombiano"),
    USD("Dólar Estadounidense"),
    EUR("Euro"),
    GBP("Libra Esterlina"),
    CHF("Franco Suizo"),
    JPY("Yen Japonés");

    private final String description;

    Currency(String description) {
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
