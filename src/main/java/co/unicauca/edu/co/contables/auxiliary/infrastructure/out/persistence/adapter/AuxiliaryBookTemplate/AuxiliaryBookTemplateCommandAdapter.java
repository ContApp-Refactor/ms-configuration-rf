package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.AuxiliaryBookTemplate;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookTemplate.IAuxiliaryBookTemplateCommandRepositoryPort;

/**
 * @brief Adaptador para las operaciones de escritura de plantillas de libros auxiliares.
 *
 * Implementación reservada del puerto de comandos de plantillas;
 * actualmente retorna {@code null} y sirve como placeholder para la
 * futura integración con el almacén de plantillas.
 */
public class AuxiliaryBookTemplateCommandAdapter implements IAuxiliaryBookTemplateCommandRepositoryPort {
    /**
     * @brief Persiste una plantilla de libro auxiliar.
     * @param auxiliaryBookTemplate Plantilla a guardar.
     * @return Plantilla persistida con los identificadores generados (no implementado).
     */
    @Override
    public AuxiliaryBookTemplate registerAuxiliaryBookTemplate(AuxiliaryBookTemplate auxiliaryBookTemplate) {
        return null;
    }
}
