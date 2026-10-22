package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;

/**
 * @brief Mapper JPA para operaciones de actualización de cuentas contables
 *
 * Define contratos para conversión de entidades JPA a modelos de dominio
 * en operaciones de actualización y consulta post-modificación.
 */
public interface IAccountCatalogueUpdateMapper {
    /**
     * @brief Convierte entidad JPA a modelo de dominio para respuesta de actualización
     * @param accountCatalogueEntity entidad JPA con datos actualizados
     * @return modelo de dominio para retorno en respuesta de actualización
     */
    AccountCatalogue toAccountCatalogue(AccountCatalogueEntity accountCatalogueEntity);
}
