package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper;

import java.util.List;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AuxiliaryAccountListRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.ItemAccountCatalogueSearchRes;

import org.mapstruct.Mapper;

/**
 * @brief Mapper para operaciones con cuentas auxiliares contables
 *
 * Define contratos para conversión entre modelos de dominio y DTOs de respuesta
 * en operaciones específicas de cuentas auxiliares (8 dígitos).
 */
@Mapper
public interface IAuxiliaryAccountRestMapper {
    
    /**
     * @brief Convierte lista de cuentas auxiliares a respuesta paginada
     * @param auxiliaryAccounts lista de cuentas auxiliares del dominio
     * @param idEnterprise ID de la empresa para contexto
     * @return DTO de respuesta con lista paginada de cuentas auxiliares
     */
    AuxiliaryAccountListRes toAuxiliaryAccountListRes(List<AccountCatalogue> auxiliaryAccounts, String idEnterprise);

    /**
     * @brief Convierte cuenta individual a item de búsqueda
     * @param accountCatalogue cuenta del dominio a convertir
     * @return DTO de item para respuestas de búsqueda individual
     */
    ItemAccountCatalogueSearchRes toItemAccountCatalogueSearchRes(AccountCatalogue accountCatalogue);

    /**
     * @brief Mapea referencia de cuenta padre a código string
     *
     * Método auxiliar para MapStruct que extrae código de cuenta padre.
     * @param parent referencia a cuenta padre
     * @return código de la cuenta padre o null si no existe
     */
    default String map(AccountCatalogue parent) {
        return parent != null && parent.getCode() != null ? parent.getCode() : null;
    }
}
