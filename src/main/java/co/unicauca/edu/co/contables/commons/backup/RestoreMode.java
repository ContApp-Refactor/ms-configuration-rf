package co.unicauca.edu.co.contables.commons.backup;

/**
 * @brief Modo de restore.
 *
 * D4 (actualizado): con ZIP v2 se puede crear una empresa nueva ({@link #NEW_ENTERPRISE}
 * o {@link #DUPLICATE}) o restaurar sobre una empresa existente ({@link #IN_PLACE}):
 * purga de todos los módulos + restauración en una sola transacción.
 * D3: en {@link #DUPLICATE} se permite NIT repetido; en {@link #NEW_ENTERPRISE}
 * un NIT ya existente en el tenant da 409.
 */
public enum RestoreMode {
    /** Restaurar un ZIP como empresa nueva. */
    NEW_ENTERPRISE,
    /** Duplicar una empresa existente (export + restore interno). */
    DUPLICATE,
    /** Sobrescribir los datos de una empresa existente (purga y reemplaza todo lo que trae la copia). */
    IN_PLACE;

    public boolean createsEnterprise() {
        return this != IN_PLACE;
    }
}
