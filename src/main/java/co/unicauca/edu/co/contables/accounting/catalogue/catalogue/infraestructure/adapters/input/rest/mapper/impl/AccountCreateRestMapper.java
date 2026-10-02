package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AccountCatalogueCreateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.IAccountCreateRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.utils.AdjustEnumAccount;

/**
 * @brief Implementación del mapper para operaciones de creación de cuentas
 *
 * Convierte entre DTOs de request/response y modelos de dominio para operaciones
 * de creación, manejando jerarquía recursiva y ajuste de enums.
 */
@Component
public class AccountCreateRestMapper implements IAccountCreateRestMapper {

    private AdjustEnumAccount adjustEnum = new AdjustEnumAccount();

    /**
     * @brief Convierte modelo de dominio a respuesta de creación
     *
     * Transforma entidad de dominio a DTO de respuesta, convirtiendo enums a strings
     * y manejando referencias padre para comunicación REST.
     * @param accountCatalogueRes modelo de dominio de cuenta creada
     * @return DTO de respuesta con datos formateados para API
     */
    @Override
    public AccountCatalogueCreateRes toCreateResponse(AccountCatalogue accountCatalogueRes) {
        if (accountCatalogueRes == null) {
            return null;
        }

        AccountCatalogueCreateRes accountCatalogue = AccountCatalogueCreateRes.builder()
                .idEnterprise(accountCatalogueRes.getIdEnterprise())
                .id(accountCatalogueRes.getId())
                .code(accountCatalogueRes.getCode())
                .description(accountCatalogueRes.getDescription())
                .financialStatus(accountCatalogueRes.getFinancialStatus() != null ? accountCatalogueRes.getFinancialStatus().getState() : null)
                .nature(accountCatalogueRes.getNature() != null ? accountCatalogueRes.getNature().getState() : null)
                .classification(accountCatalogueRes.getClassification() != null ? accountCatalogueRes.getClassification().getState() : null)
                .crossing(accountCatalogueRes.getCrossing())
                .costCenter(accountCatalogueRes.getCostCenter())
                .status(accountCatalogueRes.getStatus())
                .parent(accountCatalogueRes.getParent() != null ? accountCatalogueRes.getParent().getCode() : null)
                .build();

        return accountCatalogue;
    }

    /**
     * @brief Convierte solicitud de creación a modelo de dominio con jerarquía
     *
     * Transforma DTO de request a entidad de dominio, ajustando enums y procesando
     * recursivamente la jerarquía de cuentas padre-hijo para creación masiva.
     * @param accountCatalogueCreateReq DTO de solicitud con datos de cuenta
     * @param padre referencia a cuenta padre para jerarquía
     * @return entidad de dominio completa con jerarquía procesada
     */
    @Override
    public AccountCatalogue toDomain(AccountCatalogueCreateReq accountCatalogueCreateReq, AccountCatalogue padre) {
        if (accountCatalogueCreateReq == null) {
            return null;
        }

        AccountCatalogue accountCatalogue = AccountCatalogue.builder()
                .idEnterprise(accountCatalogueCreateReq.getIdEnterprise())
                .code(accountCatalogueCreateReq.getCode())
                .description(accountCatalogueCreateReq.getDescription())
                .financialStatus(adjustEnum.adjustFinancialStatusEnum(accountCatalogueCreateReq.getFinancialStatus()))
                .nature(adjustEnum.adjustNatureEnum(accountCatalogueCreateReq.getNature()))
                .classification(adjustEnum.adjustClassificationEnum(accountCatalogueCreateReq.getClassification()))
                .crossing(accountCatalogueCreateReq.getCrossing())
                .costCenter(accountCatalogueCreateReq.getCostCenter())
                .parent(padre)
                .build();

        if (accountCatalogueCreateReq.getChildren() == null) {
            return accountCatalogue;
        }

        padre = accountCatalogue;

        List<AccountCatalogue> children = new ArrayList<>();
        for (AccountCatalogueCreateReq child : accountCatalogueCreateReq.getChildren()) {
            AccountCatalogue childAccountCatalogue = toDomain(child, padre);
            if (childAccountCatalogue != null) {
                children.add(childAccountCatalogue);
            }
        }
        accountCatalogue.setChildren(children);

        return accountCatalogue;
    }
}
