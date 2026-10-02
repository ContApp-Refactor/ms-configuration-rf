package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input;

/**
 * @brief Puerto de entrada para gestión de uso de impuestos
 *
 * Define contrato para operaciones relacionadas con el contador de uso de impuestos,
 * utilizado cuando otros servicios notifican que han utilizado un impuesto.
 */
public interface ITaxUsageInputPort {
    /**
     * @brief Incrementa el contador de uso de un impuesto
     * @param taxId ID del impuesto
     * @param enterpriseId ID de la empresa
     */
    void incrementUsageCount(Long taxId, String enterpriseId);
}
