package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.impl;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AccountCatalogueUpdateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.IAccountUpdateRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.utils.AdjustEnumAccount;

/**
 * @brief Implementación del mapper para operaciones de actualización de cuentas
 *
 * Convierte entre DTOs de request/response y modelos de dominio para operaciones
 * de actualización, manejando cambios selectivos y referencias padre.
 */
@Component
public class AccountUpdateRestMapper implements IAccountUpdateRestMapper {

    private AdjustEnumAccount adjustEnum = new AdjustEnumAccount();

    /**
     * @brief Convierte solicitud de actualización a modelo de dominio
     *
     * Transforma DTO de request a entidad de dominio, ajustando enums y creando
     * referencia padre si se proporciona ID de cuenta padre.
     * @param accountCatalogueUpdateReq DTO con datos de actualización
     * @return entidad de dominio con cambios aplicados
     */
    @Override
    public AccountCatalogue toDomain(AccountCatalogueUpdateReq accountCatalogueUpdateReq) {
        if(accountCatalogueUpdateReq == null) {
            return null;
        }

        AccountCatalogue.AccountCatalogueBuilder builder = AccountCatalogue.builder()
                .idEnterprise(accountCatalogueUpdateReq.getIdEnterprise())
                .code(accountCatalogueUpdateReq.getCode())
                .description(accountCatalogueUpdateReq.getDescription())
                .financialStatus(adjustEnum.adjustFinancialStatusEnum(accountCatalogueUpdateReq.getFinancialStatus()))
                .nature(adjustEnum.adjustNatureEnum(accountCatalogueUpdateReq.getNature()))
                .classification(adjustEnum.adjustClassificationEnum(accountCatalogueUpdateReq.getClassification()))
                .crossing(accountCatalogueUpdateReq.getCrossing())
                .costCenter(accountCatalogueUpdateReq.getCostCenter());

        // Manejar el parent si se proporciona
        if (accountCatalogueUpdateReq.getParent() != null) {
            AccountCatalogue parent = AccountCatalogue.builder()
                    .id(accountCatalogueUpdateReq.getParent())
                    .build();
            builder.parent(parent);
        }

        return builder.build();
    }

    /**
     * @brief Convierte modelo de dominio a respuesta de actualización
     *
     * Transforma entidad de dominio a DTO de respuesta, convirtiendo enums a strings
     * y manejando referencias padre para comunicación REST.
     * @param accountCatalogue modelo de dominio actualizado
     * @return DTO de respuesta con datos de cuenta modificada
     */
    @Override
    public AccountCatalogueUpdateRes toUpdateResponse(AccountCatalogue accountCatalogue) {
        if(accountCatalogue == null) {
            return null;
        }

        return AccountCatalogueUpdateRes.builder()
                .id(accountCatalogue.getId())
                .code(accountCatalogue.getCode())
                .description(accountCatalogue.getDescription())
                .financialStatus(accountCatalogue.getFinancialStatus() != null ? accountCatalogue.getFinancialStatus().getState() : null)
                .nature(accountCatalogue.getNature() != null ? accountCatalogue.getNature().getState() : null)
                .classification(accountCatalogue.getClassification() != null ? accountCatalogue.getClassification().getState() : null)
                .crossing(accountCatalogue.getCrossing())
                .costCenter(accountCatalogue.getCostCenter())
                .status(accountCatalogue.getStatus())
                .usageCount(accountCatalogue.getUsageCount())
                .parent(accountCatalogue.getParent() != null ? accountCatalogue.getParent().getCode() : null)
                .build();
    }
    
}
