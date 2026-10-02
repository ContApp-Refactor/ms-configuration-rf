package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueChangeStateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper.IAccountCatalogueUpdateMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;

import lombok.AllArgsConstructor;

/**
 * @brief Adaptador JPA para operaciones de cambio de estado de cuentas contables
 *
 * Gestiona activación/desactivación de cuentas con lógica jerárquica compleja:
 * - Al activar: activa recursivamente toda la jerarquía padre
 * - Al desactivar: desactiva todos los descendientes en cascada
 */
@Component
@AllArgsConstructor
public class AccountCatalogueChangeStateJpaAdapter implements IAccountCatalogueChangeStateOutputPort {

    private final IAccountCatalogueRepository accountCatalogueRepository;
    private final IAccountCatalogueUpdateMapper accountCatalogueUpdateMapper;

    /**
     * @brief Cambia estado de cuenta aplicando reglas jerárquicas complejas
     *
     * Gestiona cambio de estado con lógica de negocio específica:
     * - Activación: activa recursivamente jerarquía padre
     * - Desactivación: desactiva todos descendientes en batches para performance
     * @param id identificador único de la cuenta
     * @param status nuevo estado (true=activo, false=inactivo)
     * @return cuenta actualizada con nuevo estado aplicado
     */
    @Override
    @Transactional
    public AccountCatalogue changeState(Long id, Boolean status) {
        AccountCatalogueEntity accountCatalogueEntity = accountCatalogueRepository.findById(id).orElse(null);

        if (accountCatalogueEntity == null) {
            return null;
        }

        // Cambiar el estado de la cuenta
        accountCatalogueEntity.setStatus(status);
        accountCatalogueEntity = accountCatalogueRepository.save(accountCatalogueEntity);

        if (status) {
            // Si se está activando, activar toda la jerarquía de padres
            activateParentHierarchy(accountCatalogueEntity);
        } else {
            // Si se está inactivando, inactivar todos los hijos recursivamente
            List<Long> descendantIds = accountCatalogueRepository.findDescendantIds(accountCatalogueEntity.getId(), accountCatalogueEntity.getIdEnterprise(), accountCatalogueEntity.getTenantId());
            if (!descendantIds.isEmpty()) {
                updateStatusInBatches(status, descendantIds, accountCatalogueEntity.getIdEnterprise(), accountCatalogueEntity.getTenantId());
            }
        }

        return accountCatalogueUpdateMapper.toAccountCatalogue(accountCatalogueEntity);
    }

    /**
     * @brief Activa jerarquía padre de forma recursiva ascendente
     *
     * Navega hacia arriba en jerarquía activando cada padre inactivo encontrado.
     * Esencial para mantener consistencia cuando se activa una cuenta hija.
     * @param child cuenta desde donde iniciar activación ascendente
     */
    private void activateParentHierarchy(AccountCatalogueEntity child) {
        if (child.getParent() != null) {
            AccountCatalogueEntity parent = child.getParent();

            // Si el padre no está activo, activarlo
            if (!parent.getStatus()) {
                parent.setStatus(true);
                accountCatalogueRepository.save(parent);

                // Continuar activando recursivamente hacia arriba
                activateParentHierarchy(parent);
            }
        }
    }

    /**
     * @brief Actualiza estado de cuentas en batches para optimizar performance
     *
     * Divide lista grande de IDs en batches más pequeños para evitar problemas
     * de rendimiento y límites de BD en operaciones IN masivas.
     * @param status estado a aplicar a todas las cuentas del batch
     * @param ids lista completa de IDs a actualizar
     * @param idEnterprise filtro de empresa para aislamiento de datos
     * @param tenantId filtro de tenant para multi-tenancy
     */
    private void updateStatusInBatches(Boolean status, List<Long> ids, String idEnterprise, String tenantId) {
        int batchSize = 500; // Tamaño del batch, ajustable
        for (int i = 0; i < ids.size(); i += batchSize) {
            int end = Math.min(i + batchSize, ids.size());
            List<Long> batch = ids.subList(i, end);
            accountCatalogueRepository.updateStatusByIds(status, batch, idEnterprise, tenantId);
        }
    }
}
