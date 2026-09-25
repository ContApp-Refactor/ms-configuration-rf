package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.services;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.IEnterpriseUpdateManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.IEnterpriseUpdateOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio que implementa las operaciones de actualización de empresas.
 * Gestiona la lógica de negocio para la modificación de información y estado
 * de las empresas en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
public class EnterpriseUpdateService implements IEnterpriseUpdateManagerPort {

    /**
     * Puerto de salida para operaciones de actualización de empresas.
     */
    @Autowired
    private IEnterpriseUpdateOutputPort enterpriseUpdateOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEnterprise(UUID id, Enterprise enterprise) {
        enterpriseUpdateOutputPort.updateEnterprise(id, enterprise);   
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEnterpriseStatus(UUID id, StateEnum state) {  
        enterpriseUpdateOutputPort.updateEnterpriseStatus(id, state);
    }
    
    /** {@inheritDoc} */
    @Override
    public void deleteEnterprise(UUID id) {
        enterpriseUpdateOutputPort.deleteEnterprise(id);
    }

    @Override
    public void updateEnterpriseInventoryConfiguration(UUID id,
            InventoryConfigurationTypeEnum inventoryConfigurationType) {
        enterpriseUpdateOutputPort.updateEnterpriseInventoryConfiguration(id, inventoryConfigurationType);
    }
    
}
