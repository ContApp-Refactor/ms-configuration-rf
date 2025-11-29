package co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.ErrorCodeDefinition;
import lombok.Getter;

/**
 * @brief Códigos de error para excepciones de centros de costo
 *
 * Enumeración que define los códigos de error y mensajes asociados
 * para las excepciones relacionadas con operaciones de centros de costo.
 */
@Getter
public enum CostCentersErrorCode implements ErrorCodeDefinition {

    COST_CENTER_NOT_FOUND("COST_CENTER_NOT_FOUND", "Centro de costo no encontrado"),
    COST_CENTER_ALREADY_EXISTS("COST_CENTER_ALREADY_EXISTS", "Centro de costo ya existe"),
    COST_CENTER_HAS_CHILDREN("COST_CENTER_HAS_CHILDREN", "No se puede eliminar un centro de costo que tiene hijos"),
    COST_CENTER_INVALID_CODE_PREFIX("COST_CENTER_INVALID_CODE_PREFIX", "El código del centro de costo debe mantener el prefijo del padre"),
    COST_CENTER_IN_USE("COST_CENTER_IN_USE", "El centro de costo no puede ser modificado o eliminado porque tiene movimientos contables"),
    COST_CENTER_EXPORT_NO_DATA("COST_CENTER_EXPORT_NO_DATA", "No hay centros de costo para exportar"),
    COST_CENTER_EXPORT_ERROR("COST_CENTER_EXPORT_ERROR", "Error al generar el archivo de exportación");

    private final String code;
    private final String message;

    CostCentersErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}


