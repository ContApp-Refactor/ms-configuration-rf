package co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookTemplate;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;

/**
 * @brief Puerto de salida para operaciones de escritura de plantillas de libros auxiliares.
 *
 * Define el contrato para persistir plantillas de exportación en el
 * sistema de almacenamiento.
 */
public interface IAuxiliaryBookTemplateCommandRepositoryPort {
    /**
     * @brief Persiste una plantilla de libro auxiliar.
     * @param auxiliaryBookTemplate Plantilla a guardar.
     * @return La plantilla guardada con los identificadores generados.
     */
    AuxiliaryBookTemplate registerAuxiliaryBookTemplate(AuxiliaryBookTemplate auxiliaryBookTemplate);
}
