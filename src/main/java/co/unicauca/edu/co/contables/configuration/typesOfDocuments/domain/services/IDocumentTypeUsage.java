package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services;

/**
 * @brief Puerto de entrada para gestión de uso de tipos de documento
 *
 * Define contrato para operaciones relacionadas con el contador de uso de tipos de documento,
 * utilizado cuando otros servicios notifican que han utilizado un tipo de documento.
 */
public interface IDocumentTypeUsage {
    /**
     * @brief Incrementa el contador de uso de un tipo de documento
     * @param documentTypeId ID del tipo de documento
     * @param enterpriseId ID de la empresa
     */
    void incrementUsageCount(Long documentTypeId, String enterpriseId);
}
