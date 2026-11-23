package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.ItemAccountCatalogueSearchRes;

import org.mapstruct.Mapper;

/**
 * @brief Mapper para transformación de items individuales de búsqueda
 *
 * Define contratos para conversión entre modelos de dominio y DTOs de respuesta
 * en operaciones de búsqueda individual de cuentas contables.
 */
@Mapper
public interface IItemAccountSearchRestMapper {
    /**
     * @brief Convierte cuenta de dominio a item de respuesta de búsqueda
     *
     * Transforma modelo de dominio a DTO de respuesta para APIs de búsqueda individual,
     * convirtiendo enums a strings y manejando referencias padre.
     * @param accountCatalogue modelo de dominio de cuenta contable
     * @return DTO de respuesta con datos completos de la cuenta
     */
   default ItemAccountCatalogueSearchRes toItemAccountCatalogueSearch(AccountCatalogue accountCatalogue){
       if(accountCatalogue==null){
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
               .parent(accountCatalogue.getParent() != null ? accountCatalogue.getParent().getCode() : null)
               .usageCount(accountCatalogue.getUsageCount())
               .build();
   }
}
