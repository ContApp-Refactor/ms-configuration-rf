package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper.Impl;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper.IAccountCatalogueUpdateMapper;

/**
 * @brief Implementación del mapper JPA para operaciones de actualización de cuentas
 *
 * Convierte entidades JPA a modelos de dominio para respuestas de actualización,
 * incluyendo referencias a impuestos y jerarquía padre.
 */
@Component
public class AccountCatalogueUpdateMapper implements IAccountCatalogueUpdateMapper{


    @Override
    public AccountCatalogue toAccountCatalogue(AccountCatalogueEntity accountCatalogueEntity) {
        if(accountCatalogueEntity == null){
            return null;
        }

        AccountCatalogue accountCatalogue = AccountCatalogue.builder()
                .id(accountCatalogueEntity.getId())
                .code(accountCatalogueEntity.getCode())
                .description(accountCatalogueEntity.getDescription())
                .nature(accountCatalogueEntity.getNature())
                .financialStatus(accountCatalogueEntity.getFinancialStatus())
                .classification(accountCatalogueEntity.getClassification())
                .crossing(accountCatalogueEntity.getCrossing())
                .costCenter(accountCatalogueEntity.getCostCenter())
                .status(accountCatalogueEntity.getStatus())
                .salesTaxes(new ArrayList<>())
                .purchaseTaxes(new ArrayList<>())
                .amount(accountCatalogueEntity.getAmount())
                .usageCount(accountCatalogueEntity.getUsageCount())
                .parent(auxParent(accountCatalogueEntity.getParent() == null ? null : accountCatalogueEntity.getParent()))
                .build();
        
        return accountCatalogue;
    }

    private AccountCatalogue auxParent(AccountCatalogueEntity accountCatalogue){
        if(accountCatalogue == null){
            return AccountCatalogue.builder()
                    .id(null)
                    .code(null)
                    .build();
        }

        return AccountCatalogue.builder()
                .id(accountCatalogue.getId())
                .code(accountCatalogue.getCode())
                .build();
    }
    
}
