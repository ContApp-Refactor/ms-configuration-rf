package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper.IAccountCatalogueCreateMapper;

/**
 * @brief Implementación del mapper JPA para operaciones de creación de cuentas
 *
 * Convierte entre modelos de dominio y entidades JPA para operaciones de creación,
 * manejando jerarquía recursiva padre-hijo en proceso de mapeo.
 */
@Component
public class AccountCatalogueCreateMapper implements IAccountCatalogueCreateMapper{

    @Override
    public AccountCatalogueEntity toEntity(AccountCatalogue accountCatalogue, AccountCatalogueEntity parent) {

       if(accountCatalogue == null){
           return null;
       }

        AccountCatalogueEntity accountCatalogueEntity = AccountCatalogueEntity.builder()
                .idEnterprise(accountCatalogue.getIdEnterprise())
                .code(accountCatalogue.getCode())
                .description(accountCatalogue.getDescription())
                .nature(accountCatalogue.getNature())
                .financialStatus(accountCatalogue.getFinancialStatus())
                .classification(accountCatalogue.getClassification())
                .crossing(accountCatalogue.getCrossing())
                .costCenter(accountCatalogue.getCostCenter())
                .status(accountCatalogue.getStatus() != null ? accountCatalogue.getStatus() : true)
                .parent(parent)
                .build();

        if(accountCatalogue.getChildren() == null){
            return accountCatalogueEntity;
        }
        
        parent = accountCatalogueEntity;       

        List<AccountCatalogueEntity> children = new ArrayList<>();
        for(AccountCatalogue child: accountCatalogue.getChildren()){
            AccountCatalogueEntity childAccountCatalogue = toEntity(child, parent);
            if(childAccountCatalogue != null){
                children.add(childAccountCatalogue);
            }
        }
        accountCatalogueEntity.setChildren(children);

        return accountCatalogueEntity;
    }

    @Override
    public AccountCatalogue toModel(AccountCatalogueEntity accountCatalogueEntity) {
        if(accountCatalogueEntity == null){
            return null;
        }

        AccountCatalogue accountCatalogue = AccountCatalogue.builder()
                .idEnterprise(accountCatalogueEntity.getIdEnterprise())
                .id(accountCatalogueEntity.getId())
                .code(accountCatalogueEntity.getCode())
                .description(accountCatalogueEntity.getDescription())
                .nature(accountCatalogueEntity.getNature())
                .financialStatus(accountCatalogueEntity.getFinancialStatus())
                .classification(accountCatalogueEntity.getClassification())
                .crossing(accountCatalogueEntity.getCrossing())
                .costCenter(accountCatalogueEntity.getCostCenter())
                .status(accountCatalogueEntity.getStatus())
                .salesTaxes(accountCatalogueEntity.getSalesTaxes())
                .purchaseTaxes(accountCatalogueEntity.getPurchaseTaxes())
                .parent(auxParent(accountCatalogueEntity.getParent() == null ? null : accountCatalogueEntity.getParent()))
                .build();

        if(accountCatalogueEntity.getChildren() == null){
            return accountCatalogue;
        }

        List<AccountCatalogue> children = new ArrayList<>();
        for(AccountCatalogueEntity child: accountCatalogueEntity.getChildren()){
            AccountCatalogue childAccountCatalogue = toModel(child);
            if(childAccountCatalogue != null){
                children.add(childAccountCatalogue);
            }
        }
        accountCatalogue.setChildren(children);

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
