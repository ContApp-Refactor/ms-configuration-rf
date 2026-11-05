package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output;

/**
 * @brief Puerto de salida para gestión de uso de impuestos
 *
 * Define contrato para operaciones de persistencia relacionadas con
 * el contador de uso de impuestos en el repositorio.
 */
public interface ITaxUsageOutputPort {
    /**
     * @brief Incrementa el contador de uso de un impuesto
     * @param taxId ID del impuesto
     * @param enterpriseId ID de la empresa
     */
    void incrementUsageCount(Long taxId, String enterpriseId);
}
