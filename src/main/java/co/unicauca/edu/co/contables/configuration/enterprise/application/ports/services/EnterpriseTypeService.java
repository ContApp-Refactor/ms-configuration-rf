package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.services;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.IEnterpriseTypeManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.IEnterpriseTypeOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que implementa las operaciones de gestión de tipos de empresa.
 * Gestiona la lógica de negocio para la consulta y recuperación de información
 * sobre los diferentes tipos de empresa en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class EnterpriseTypeService implements IEnterpriseTypeManagerPort {

    /**
     * Puerto de salida para operaciones de gestión de tipos de empresa.
     */
    private final IEnterpriseTypeOutputPort enterpriseTypeOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EnterpriseType> getAllEnterpriseTypes() {
        return enterpriseTypeOutputPort.getAllEnterpriseTypes();     
    } 
}