package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapper JPA para operaciones de búsqueda y consulta de cuentas contables
 *
 * Define contratos para conversión bidireccional entre entidades JPA y modelos de dominio
 * en operaciones de búsqueda, incluyendo manejo de jerarquía recursiva.
 */
@Mapper
public interface IItemAccountCatalogueSearchMapper {

    /**
     * @brief Convierte entidad JPA a modelo de dominio con jerarquía
     *
     * Transforma entidad JPA a modelo de dominio, incluyendo referencias a impuestos
     * y jerarquía padre-hijo mediante método auxiliar recursivo.
     * @param accountCatalogueEntity entidad JPA con datos completos de BD
     * @return modelo de dominio con todas las relaciones mapeadas
     */
    default AccountCatalogue toDomain(AccountCatalogueEntity accountCatalogueEntity) {
        if (accountCatalogueEntity == null) {
            return null;
        }
        return AccountCatalogue.builder()
                .id(accountCatalogueEntity.getId())
                .idEnterprise(accountCatalogueEntity.getIdEnterprise())
                .code(accountCatalogueEntity.getCode())
                .description(accountCatalogueEntity.getDescription())
                .nature(accountCatalogueEntity.getNature())
                .financialStatus(accountCatalogueEntity.getFinancialStatus())
                .classification(accountCatalogueEntity.getClassification())
                .crossing(accountCatalogueEntity.getCrossing())
                .costCenter(accountCatalogueEntity.getCostCenter())
                .status(accountCatalogueEntity.getStatus())
                // Inicializar colecciones vacías para evitar null pointer exceptions
                .salesTaxes(new ArrayList<>())
                .purchaseTaxes(new ArrayList<>())
                .amount(accountCatalogueEntity.getAmount())
                .usageCount(accountCatalogueEntity.getUsageCount())
                .parent(auxParent(
                        accountCatalogueEntity.getParent() == null ? null : accountCatalogueEntity.getParent()))
                .build();
    }

    /**
     * @brief Convierte modelo de dominio a entidad JPA para consultas
     *
     * Transforma modelo de dominio a entidad JPA ignorando campo tenantId
     * (manejado automáticamente por framework de multi-tenancy).
     * @param accountCatalogue modelo de dominio a convertir
     * @return entidad JPA equivalente sin campo tenantId
     */
    @Mapping(target = "tenantId", ignore = true)
    AccountCatalogueEntity toEntity(AccountCatalogue accountCatalogue);

    /**
     * @brief Convierte entidad JPA a modelo de dominio con árbol jerárquico completo
     *
     * Transforma entidad JPA a modelo de dominio incluyendo recursivamente todos los hijos,
     * creando estructura de árbol completa para representaciones jerárquicas.
     * @param accountCatalogueEntity entidad JPA raíz con colección de hijos
     * @return modelo de dominio con jerarquía completa padre-hijo mapeada
     */
    default AccountCatalogue toDomainTree(AccountCatalogueEntity accountCatalogueEntity) {
        if (accountCatalogueEntity == null) {
            return null;
        }

        AccountCatalogue accountCatalogue = AccountCatalogue.builder()
                .id(accountCatalogueEntity.getId())
                .idEnterprise(accountCatalogueEntity.getIdEnterprise())
                .code(accountCatalogueEntity.getCode())
                .description(accountCatalogueEntity.getDescription())
                .nature(accountCatalogueEntity.getNature())
                .financialStatus(accountCatalogueEntity.getFinancialStatus())
                .classification(accountCatalogueEntity.getClassification())
                .crossing(accountCatalogueEntity.getCrossing())
                .costCenter(accountCatalogueEntity.getCostCenter())
                .status(accountCatalogueEntity.getStatus())
                // Inicializar colecciones vacías para evitar null pointer exceptions
                .salesTaxes(new ArrayList<>())
                .purchaseTaxes(new ArrayList<>())
                .usageCount(accountCatalogueEntity.getUsageCount())
                .parent(auxParent(
                        accountCatalogueEntity.getParent() == null ? null : accountCatalogueEntity.getParent()))
                .build();

        List<AccountCatalogue> children = new ArrayList<>();
        for (AccountCatalogueEntity child : accountCatalogueEntity.getChildren()) {
            AccountCatalogue childAccountCatalogue = toDomainTree(child);
            if (childAccountCatalogue != null) {
                children.add(childAccountCatalogue);
            }
        }
        accountCatalogue.setChildren(children);

        return accountCatalogue;
    }

    /**
     * @brief Método auxiliar para crear referencia padre mínima
     *
     * Crea modelo de dominio simplificado con solo id y code para referencias padre,
     * evitando carga completa de jerarquía en mapeos recursivos.
     * @param accountCatalogue entidad padre (puede ser null)
     * @return modelo padre mínimo o con campos null si entrada es null
     */
    default AccountCatalogue auxParent(AccountCatalogueEntity accountCatalogue) {
        if (accountCatalogue == null) {
            AccountCatalogue accountCatalogueNull = AccountCatalogue.builder()
                    .id(null)
                    .code(null)
                    .build();

            return accountCatalogueNull;
        }

        AccountCatalogue accountCatalogueParent = AccountCatalogue.builder()
                .id(accountCatalogue.getId())
                .code(accountCatalogue.getCode())
                .build();

        return accountCatalogueParent;
    }

}
