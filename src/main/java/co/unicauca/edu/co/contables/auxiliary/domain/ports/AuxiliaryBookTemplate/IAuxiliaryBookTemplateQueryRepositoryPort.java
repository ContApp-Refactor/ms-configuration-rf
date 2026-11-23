package co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookTemplate;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;

import java.util.List;

/**
 * @brief Puerto de salida para operaciones de consulta de plantillas de libros auxiliares.
 *
 * Define el contrato para recuperar las plantillas de exportación
 * registradas en el sistema.
 */
public interface IAuxiliaryBookTemplateQueryRepositoryPort {
    /**
     * @brief Recupera todas las plantillas registradas.
     * @return Lista de objetos {@link AuxiliaryBookTemplate}.
     */
    List<AuxiliaryBookTemplate> findAll();
}
