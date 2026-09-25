package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.services;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.IEnterpriseSearchManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.EnterpriseInfoDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Servicio que implementa las operaciones de búsqueda de empresas.
 * Gestiona la lógica de negocio para la consulta y recuperación de información
 * de empresas, tanto activas como inactivas.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class EnterpriseSearchService implements IEnterpriseSearchManagerPort {

    /**
     * Puerto de salida para operaciones de búsqueda de empresas.
     */
    private final IEnterpriseSearchOutputPort enterpriseSearchOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EnterpriseInfoDto> getAllEnterprises() {
        return enterpriseSearchOutputPort.getAllEnterprises();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EnterpriseInfoDto> getAllEnterprisesInactive() {
        return enterpriseSearchOutputPort.getAllEnterprisesInactive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enterprise getEnterpriseById(UUID id) {
        return enterpriseSearchOutputPort.getEnterpriseById(id);
    }
}
