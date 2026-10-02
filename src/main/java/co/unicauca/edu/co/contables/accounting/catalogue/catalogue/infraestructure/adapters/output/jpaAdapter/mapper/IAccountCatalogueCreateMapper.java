package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;

/**
 * @brief Mapper JPA para operaciones de creación de cuentas contables
 *
 * Define contratos para conversión entre modelos de dominio y entidades JPA
 * en operaciones de creación, manejando jerarquía padre-hijo.
 */
public interface IAccountCatalogueCreateMapper {
   /**
    * @brief Convierte modelo de dominio a entidad JPA para creación
    * @param accountCatalogue modelo de dominio de cuenta a crear
    * @param parent entidad padre para establecer jerarquía
    * @return entidad JPA lista para persistencia con relaciones establecidas
    */
   AccountCatalogueEntity toEntity(AccountCatalogue accountCatalogue, AccountCatalogueEntity parent);

   /**
    * @brief Convierte entidad JPA a modelo de dominio
    * @param accountCatalogueEntity entidad JPA recuperada de BD
    * @return modelo de dominio equivalente
    */
   AccountCatalogue toModel(AccountCatalogueEntity accountCatalogueEntity);

}
