package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AccountCatalogueListRes;

import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @brief Mapper para operaciones de búsqueda y listado de cuentas contables
 *
 * Define contratos para conversión entre modelos de dominio y DTOs de respuesta
 * en operaciones de búsqueda, incluyendo mapeo recursivo de jerarquías.
 */
@Mapper
public interface IAccountSearchRestMapper {
    /**
     * @brief Convierte modelo jerárquico de dominio a respuesta de lista
     *
     * Mapea recursivamente la estructura de árbol de cuentas, convirtiendo enums
     * a strings y manejando referencias padre-hijo para representación jerárquica.
     * @param accountCatalogue modelo de dominio con jerarquía completa
     * @return DTO de respuesta con estructura de árbol para UI
     */
    default AccountCatalogueListRes toAccountCatalogueListRes(AccountCatalogue accountCatalogue) {

        if (accountCatalogue == null) {
            return null;
        }
        List<AccountCatalogueListRes> children = accountCatalogue.getChildren().stream()
                .map(this::toAccountCatalogueListRes)
                .collect(Collectors.toList());
        return AccountCatalogueListRes.builder()
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
                .children(children)
                .parent(accountCatalogue.getParent() != null ? accountCatalogue.getParent().getCode() : null)
                .build();
    }
}
