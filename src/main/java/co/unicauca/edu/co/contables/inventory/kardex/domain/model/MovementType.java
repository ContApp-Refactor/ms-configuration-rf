package co.unicauca.edu.co.contables.inventory.kardex.domain.model;

/**
 * @brief Enumeration of inventory movement types
 * 
 */
public enum MovementType {
    PURCHASE("Compra"),
    SALE("Venta"),
    SALESRETURN("Devolución de venta"),
    PURCHASERETURN("Devolución de compra"),
    NONCOMMERCIALEXIT("Salida no comercial"),
    NONCOMMERCIALENTRY("Entrada no comercial"),

    ADJUSTMENTEXIT("Ajuste de inventario salida"),
    ADJUSTMENTENTRY("Ajuste de inventario entrada");

    private final String description;

    MovementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
