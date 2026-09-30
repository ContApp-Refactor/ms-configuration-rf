package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueUpdateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper.IAccountCatalogueUpdateMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * @brief Adaptador JPA para operaciones de actualización con cambios jerárquicos
 *
 * Gestiona actualizaciones de cuentas con lógica compleja:
 * - Actualización de campos individuales de cuenta padre
 * - Cambio automático de códigos en jerarquía completa al cambiar código padre
 * - Propagación recursiva de cambios de código hacia descendientes
 */
@Component
@Data
@RequiredArgsConstructor
@Slf4j
public class AccountCatalogueUpdateJpaAdapter implements IAccountCatalogueUpdateOutputPort {


    private final  IAccountCatalogueRepository accountCatalogueRepository;

    private final IAccountCatalogueUpdateMapper accountCatalogueUpdateMapper;
    
    /**
     * @brief Actualiza cuenta con propagación automática de cambios de código jerárquico
     *
     * Proceso que realiza:
     * - Validación de existencia de cuenta
     * - Actualización de campos principales
     * - Detección de cambios de código
     * - Propagación recursiva de cambios de código a toda jerarquía descendiente
     * @param id identificador único de cuenta a actualizar
     * @param accountCatalogue datos actualizados para aplicar a la cuenta
     * @return cuenta actualizada con todos los cambios aplicados o null si no existe
     */
    @Override
    @Transactional
    public AccountCatalogue updateAccountCatalogue(long id, AccountCatalogue accountCatalogue) {
        AccountCatalogueEntity accountCatalogueEntity = accountCatalogueRepository.findByIdAndIdEnterprise(id, accountCatalogue.getIdEnterprise());

        if(accountCatalogueEntity == null){
            return null;
        }

        // Guardar el código anterior para comparar
        String oldCode = accountCatalogueEntity.getCode();
        String newCode = accountCatalogue.getCode();

        // Preservar el usageCount actual antes de actualizar
        Integer currentUsageCount = accountCatalogueEntity.getUsageCount();

        // Actualizar los campos de la cuenta padre
        accountCatalogueEntity.setCode(newCode);
        accountCatalogueEntity.setDescription(accountCatalogue.getDescription());
        accountCatalogueEntity.setClassification(accountCatalogue.getClassification());
        accountCatalogueEntity.setFinancialStatus(accountCatalogue.getFinancialStatus());
        accountCatalogueEntity.setNature(accountCatalogue.getNature());
        accountCatalogueEntity.setCrossing(accountCatalogue.getCrossing());
        accountCatalogueEntity.setCostCenter(accountCatalogue.getCostCenter());
        accountCatalogueEntity.setAmount(accountCatalogue.getAmount());

        // Aquí se restaura para contoador de uso para evitar que otros servicios como BalanceUpdateService lo sobreescriban
        accountCatalogueEntity.setUsageCount(currentUsageCount);

        accountCatalogueEntity = accountCatalogueRepository.save(accountCatalogueEntity);

        // Si el código cambió, actualizar códigos de hijos en cascada
        if (!oldCode.equals(newCode)) {
            updateChildrenCodes(accountCatalogueEntity.getId(), oldCode, newCode, accountCatalogue.getIdEnterprise());
        }

        // Recargar la entidad con JOIN FETCH para evitar LazyInitializationException en el mapper
        AccountCatalogueEntity savedEntity = accountCatalogueRepository.findByIdAndIdEnterprise(
            accountCatalogueEntity.getId(), 
            accountCatalogue.getIdEnterprise()
        );

        return accountCatalogueUpdateMapper.toAccountCatalogue(savedEntity);
    }

    /**
     * @brief Incrementa el contador de uso de forma atómica
     * @details Utiliza una query optimizada para incrementar el usageCount sin cargar
     * la entidad completa. Esto permite incrementar el contador de forma segura
     * en entornos concurrentes sin race conditions.
     * Usa REQUIRES_NEW para forzar commit inmediato y evitar que otras transacciones lean valores obsoletos.
     * @param id ID de la cuenta cuyo contador se va a incrementar
     * @return Cuenta actualizada con el nuevo valor de usageCount, o null si no existe
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AccountCatalogue incrementUsageCount(long id) {

        // Ejecutar UPDATE atómico en la base de datos
        int updatedRows = accountCatalogueRepository.incrementUsageCount(id);

        if (updatedRows == 0) {
            return null;
        }

        // Recuperar la cuenta actualizada
        AccountCatalogueEntity updatedEntity = accountCatalogueRepository.findById(id).orElse(null);
        if (updatedEntity != null) {            
            return accountCatalogueUpdateMapper.toAccountCatalogue(updatedEntity);
        }

        return null;
    }

    /**
     * @brief Actualiza solo el amount de una cuenta e indica que esa cuenta esta en uso
     * @details Usa UPDATE selectivo para evitar race conditions con usageCount.
     * Este método es especialmente útil para AccountBalanceUpdateService.
     * @param id ID de la cuenta
     * @param amount Nuevo valor del amount
     * @return true si se actualizó correctamente
     */
    @Override
    @Transactional
    public boolean updateAmount(long id, java.math.BigDecimal amount) {
        AccountCatalogueEntity accountCatalogueEntity = accountCatalogueRepository.findById(id).orElse(null);
        if (accountCatalogueEntity == null) {
            return false;
        } else {
            int usageCount = accountCatalogueEntity.getUsageCount();
            accountCatalogueEntity.setAmount(amount);
            accountCatalogueEntity.setUsageCount(usageCount + 1); //Incrementa el usageCount para indicar uso
            accountCatalogueRepository.save(accountCatalogueEntity);
        }
        return true;
    }

    /**
     * @brief Propaga cambios de código padre a toda jerarquía descendiente
     *
     * Algoritmo recursivo que actualiza códigos de forma jerárquica:
     * - Reemplaza prefijo del código padre en todos los descendientes
     * - Mantiene estructura PUC (Plan Único de Cuentas) al cambiar códigos
     * - Actualiza recursivamente hacia abajo en la jerarquía
     * @param parentId ID del padre cuyos cambios de código se propagan
     * @param oldParentCode código anterior del padre para reemplazar
     * @param newParentCode código nuevo del padre para aplicar
     * @param idEnterprise filtro de empresa para aislamiento de datos
     */
    private void updateChildrenCodes(Long parentId, String oldParentCode, String newParentCode, String idEnterprise) {
        // Obtener todos los hijos directos del padre
        List<AccountCatalogueEntity> children = accountCatalogueRepository.findByParentIdAndIdEnterprise(parentId, idEnterprise);

        for (AccountCatalogueEntity child : children) {
            String oldChildCode = child.getCode();

            // Verificar que el código del hijo comience con el código del padre antiguo
            if (oldChildCode.startsWith(oldParentCode)) {
                // Reemplazar el prefijo del código padre antiguo por el nuevo
                String newChildCode = newParentCode + oldChildCode.substring(oldParentCode.length());

                // Actualizar el código del hijo
                child.setCode(newChildCode);
                accountCatalogueRepository.save(child);

                // Actualizar recursivamente los códigos de los descendientes de este hijo
                updateChildrenCodes(child.getId(), oldChildCode, newChildCode, idEnterprise);
            }
        }
    }
}
