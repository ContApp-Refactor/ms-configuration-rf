package co.unicauca.edu.co.contables.billing.domain.model;

/** Estado de devolucion de una factura. Siempre derivado, nunca almacenado (RET-9). */
public enum ReturnStatus {
    SIN_DEVOLUCION,
    PARCIAL,
    TOTAL
}
