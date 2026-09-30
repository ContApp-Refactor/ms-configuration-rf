package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AuxiliaryAccountListRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.ItemAccountCatalogueSearchRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.IAuxiliaryAccountRestMapper;

/**
 * @brief Implementación del mapper para operaciones con cuentas auxiliares
 *
 * Convierte entre modelos de dominio y DTOs de respuesta para operaciones
 * específicas de cuentas auxiliares, incluyendo listados y búsquedas.
 */
@Component
public class AuxiliaryAccountRestMapper implements IAuxiliaryAccountRestMapper {

    /**
     * @brief Convierte lista de cuentas auxiliares a respuesta paginada
     *
     * Transforma colección de cuentas auxiliares a DTO de respuesta con conteo total,
     * manejando casos de listas vacías o nulas.
     * @param auxiliaryAccounts lista de cuentas auxiliares del dominio
     * @param idEnterprise ID de la empresa para contexto
     * @return DTO de respuesta con lista formateada y estadísticas
     */
    @Override
    public AuxiliaryAccountListRes toAuxiliaryAccountListRes(List<AccountCatalogue> auxiliaryAccounts, String idEnterprise) {
        if (auxiliaryAccounts == null) {
            return AuxiliaryAccountListRes.builder()
                    .auxiliaryAccounts(List.of())
                    .totalCount(0)
                    .idEnterprise(idEnterprise)
                    .build();
        }
        
        List<ItemAccountCatalogueSearchRes> accountList = auxiliaryAccounts.stream()
                .map(this::toItemAccountCatalogueSearchRes)
                .collect(Collectors.toList());
        
        return AuxiliaryAccountListRes.builder()
                .auxiliaryAccounts(accountList)
                .totalCount(accountList.size())
                .idEnterprise(idEnterprise)
                .build();
    }

    /**
     * @brief Convierte cuenta individual a item de respuesta de búsqueda
     *
     * Transforma entidad de cuenta auxiliar a DTO de item para respuestas de búsqueda,
     * convirtiendo enums a strings y mapeando referencias padre.
     * @param accountCatalogue cuenta auxiliar del dominio a convertir
     * @return DTO de item con datos completos de la cuenta auxiliar
     */
    @Override
    public ItemAccountCatalogueSearchRes toItemAccountCatalogueSearchRes(AccountCatalogue accountCatalogue) {
        if (accountCatalogue == null) {
            return null;
        }
        
        return ItemAccountCatalogueSearchRes.builder()
                .id(accountCatalogue.getId())
                .code(accountCatalogue.getCode())
                .description(accountCatalogue.getDescription())
                .nature(accountCatalogue.getNature() != null ? accountCatalogue.getNature().getState() : null)
                .financialStatus(accountCatalogue.getFinancialStatus() != null ? accountCatalogue.getFinancialStatus().getState() : null)
                .classification(accountCatalogue.getClassification() != null ? accountCatalogue.getClassification().getState() : null)
                .crossing(accountCatalogue.getCrossing())
                .costCenter(accountCatalogue.getCostCenter())
                .status(accountCatalogue.getStatus())
                .usageCount(accountCatalogue.getUsageCount())
                .parent(map(accountCatalogue.getParent())) // Usa el método de mapeo definido en la interfaz
                .build();
    }
}
