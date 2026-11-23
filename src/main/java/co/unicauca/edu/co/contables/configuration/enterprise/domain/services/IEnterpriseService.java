package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.EnterpriseInfoDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;

import java.util.List;
import java.util.UUID;

/**
 * @brief Interfaz del servicio de dominio para empresas.
 *
 * Define las operaciones de negocio para crear, actualizar, consultar y
 * eliminar empresas, además de gestionar su estado y configuración de
 * inventario.
 */
public interface IEnterpriseService {

    /**
     * Crea una nueva empresa, incluyendo la creación en cascada de su
     * ubicación y de su tipo de persona.
     */
    Enterprise create(Enterprise enterprise);

    /**
     * Actualiza una empresa existente, reemplazando su ubicación.
     */
    void update(UUID id, Enterprise enterprise);

    void updateStatus(UUID id, StateEnum state);

    void updateInventoryConfiguration(UUID id, InventoryConfigurationTypeEnum inventoryConfigurationType);

    /** Elimina permanentemente una empresa (hard delete). */
    void delete(UUID id);

    Enterprise findById(UUID id);

    List<EnterpriseInfoDto> findAllActive();

    List<EnterpriseInfoDto> findAllInactive();

    List<EnterpriseInfoDto> search(String q);
}
