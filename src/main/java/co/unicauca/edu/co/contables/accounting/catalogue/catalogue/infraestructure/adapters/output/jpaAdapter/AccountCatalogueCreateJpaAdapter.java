package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueCreateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper.IAccountCatalogueCreateMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * @brief Adaptador JPA para operaciones de creación de cuentas contables
 *
 * Gestiona creación de cuentas con lógica compleja de jerarquía:
 * - Creación de cuentas padre-hijo recursiva
 * - Activación automática de jerarquía padre
 * - Validación de unicidad de códigos
 */
@Component
@Data
public class AccountCatalogueCreateJpaAdapter implements IAccountCatalogueCreateOutputPort {

    private final IAccountCatalogueRepository accountCatalogueRepository;

    private final IAccountCatalogueCreateMapper accountCatalogueCreateMapper;

    /**
     * @brief Crea cuenta contable con validación y activación jerárquica automática
     *
     * Proceso complejo de creación que incluye:
     * - Validación de unicidad de código
     * - Creación recursiva de jerarquía padre-hijo
     * - Activación automática de jerarquía padre según reglas PUC
     * @param accountCatalogue modelo de dominio con datos de la cuenta a crear
     * @return cuenta creada con ID asignado o cuenta existente si código ya existe
     */
    
    @Override
    @Transactional
    public AccountCatalogue createAccountCatalogue(AccountCatalogue accountCatalogue) {

        AccountCatalogueEntity parent = null;
        if (accountCatalogue.getParent() != null) {
             parent = accountCatalogueRepository.findByCode(accountCatalogue.getParent().getCode(), accountCatalogue.getIdEnterprise());       
        }
        

        AccountCatalogueEntity accountCatalogueEntity = accountCatalogueCreateMapper.toEntity(accountCatalogue, parent);
        if(accountCatalogueEntity==null){
            return null;
        }
        if(accountCatalogueRepository.findByCode(accountCatalogueEntity.getCode(), accountCatalogueEntity.getIdEnterprise())==null){
            accountCatalogueEntity=accountCatalogueRepository.save(accountCatalogueEntity);
            
            activateParentHierarchy(accountCatalogueEntity);
        }else{
            accountCatalogueEntity=accountCatalogueRepository.findByCode(accountCatalogueEntity.getCode(),accountCatalogueEntity.getIdEnterprise());
        }
        return accountCatalogueCreateMapper.toModel(accountCatalogueEntity);
    }

    /**
     * @brief Crea múltiples cuentas contables en batch
     * @details Usa saveAll para reducir round-trips.
     * @param accountCatalogues lista de cuentas ya validadas y sin duplicados
     * @return lista de cuentas creadas con IDs asignados
     */
    @Override
    @Transactional
    public List<AccountCatalogue> createAllAccountCatalogues(List<AccountCatalogue> accountCatalogues) {
        List<AccountCatalogueEntity> entitiesToSave = new ArrayList<>();
        
        // Pre-cargar todos los padres únicos en batch para evitar N+1
        Set<String> parentCodes = accountCatalogues.stream()
                .filter(acc -> acc.getParent() != null && acc.getParent().getCode() != null)
                .map(acc -> acc.getParent().getCode())
                .collect(java.util.stream.Collectors.toSet());
        
        Map<String, AccountCatalogueEntity> parentsMap = new HashMap<>();
        if (!parentCodes.isEmpty()) {
            List<AccountCatalogueEntity> parents = accountCatalogueRepository.findByCodesIn(
                    new ArrayList<>(parentCodes), 
                    accountCatalogues.get(0).getIdEnterprise());
            for (AccountCatalogueEntity parent : parents) {
                parentsMap.put(parent.getCode(), parent);
            }
        }
        
        for (AccountCatalogue accountCatalogue : accountCatalogues) {
            AccountCatalogueEntity parent = null;
            if (accountCatalogue.getParent() != null && accountCatalogue.getParent().getCode() != null) {
                parent = parentsMap.get(accountCatalogue.getParent().getCode());
            }
            
            AccountCatalogueEntity entity = accountCatalogueCreateMapper.toEntity(accountCatalogue, parent);
            if (entity != null) {
                entitiesToSave.add(entity);
            }
        }
        
        // Batch save - útil para importaciones sin dependencias jerárquicas en el mismo lote
        List<AccountCatalogueEntity> savedEntities = accountCatalogueRepository.saveAll(entitiesToSave);
        
        // Convertir a dominio
        List<AccountCatalogue> result = new ArrayList<>();
        for (AccountCatalogueEntity entity : savedEntities) {
            result.add(accountCatalogueCreateMapper.toModel(entity));
        }
        
        return result;
    }

    /**
     * @brief Activa jerarquía padre completa siguiendo reglas PUC
     *
     * Calcula todos los códigos padre basados en jerarquía PUC y los activa
     * de forma masiva para mantener consistencia cuando se crea una cuenta hija.
     * @param child cuenta recién creada desde donde calcular jerarquía padre
     */
    private void activateParentHierarchy(AccountCatalogueEntity child) {
        List<String> parentCodes = getAllParentCodes(child.getCode());
        if (!parentCodes.isEmpty()) {
            accountCatalogueRepository.updateStatusByCodes(true, parentCodes, child.getIdEnterprise(), child.getTenantId());
        }
    }

    /**
     * @brief Calcula jerarquía completa de códigos padre según reglas PUC
     *
     * Recorre jerarquía desde cuenta hija hacia arriba aplicando reglas de
     * truncamiento PUC (Plan Único de Cuentas) para determinar códigos padre.
     * @param code código de cuenta hija para calcular jerarquía padre
     * @return lista completa de códigos padre desde más cercano hasta raíz
     */
    private List<String> getAllParentCodes(String code) {
        List<String> parentCodes = new ArrayList<>();
        String currentCode = code;
        while (currentCode.length() > 1) {
            String parentCode = getParentCode(currentCode);
            if (parentCode != null) {
                parentCodes.add(parentCode);
                currentCode = parentCode;
            } else {
                break;
            }
        }
        return parentCodes;
    }

    /**
     * @brief Calcula código padre inmediato según longitud PUC
     *
     * Aplica reglas de truncamiento basadas en longitud del código:
     * 1 dígito = raíz, 2 dígitos = clase, 4 dígitos = grupo, etc.
     * @param code código de cuenta para calcular padre inmediato
     * @return código del padre directo o null si es cuenta raíz
     */
    private String getParentCode(String code) {
        switch (code.length()) {
            case 1: return null; // Clase, no tiene padre
            case 2: return code.substring(0, 1); // Grupo -> Clase
            case 4: return code.substring(0, 2); // Cuenta -> Grupo
            case 6: return code.substring(0, 4); // Subcuenta -> Cuenta
            case 8: return code.substring(0, 6); // Auxiliar -> Subcuenta
            default: return null;
        }
    }
}
