package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.AuxiliaryBookTemplate;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookTemplate.IAuxiliaryBookTemplateQueryRepositoryPort;

import java.util.List;

/**
 * @brief Adaptador para las operaciones de lectura de plantillas de libros auxiliares.
 *
 * Implementación reservada del puerto de consultas de plantillas;
 * actualmente retorna una lista vacía como placeholder para la
 * futura integración con el almacén de plantillas.
 */
public class AuxiliaryBookTemplateQueryAdapter implements IAuxiliaryBookTemplateQueryRepositoryPort {
    /**
     * @brief Obtiene todas las plantillas de libros auxiliares.
     * @return Lista de plantillas (vacía en la implementación actual).
     */
    @Override
    public List<AuxiliaryBookTemplate> findAll() {
        return List.of();
    }
}
