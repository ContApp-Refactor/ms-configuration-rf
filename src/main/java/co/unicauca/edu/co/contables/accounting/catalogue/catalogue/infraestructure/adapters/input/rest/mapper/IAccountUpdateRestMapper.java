package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AccountCatalogueUpdateRes;

/**
 * @brief Mapper para operaciones de actualización de cuentas contables
 *
 * Define contratos para conversión entre DTOs de request/response y modelos de dominio
 * en operaciones de actualización de cuentas, permitiendo cambios selectivos.
 */
public interface  IAccountUpdateRestMapper {
    /**
     * @brief Convierte solicitud de actualización a modelo de dominio
     * @param accountCatalogueUpdateReq DTO de solicitud de actualización
     * @return modelo de dominio con cambios aplicados
     */
    AccountCatalogue toDomain(AccountCatalogueUpdateReq accountCatalogueUpdateReq);

    /**
     * @brief Convierte modelo de dominio a respuesta de actualización
     * @param accountCatalogue modelo de dominio actualizado
     * @return DTO de respuesta con datos de la cuenta actualizada
     */
    AccountCatalogueUpdateRes toUpdateResponse(AccountCatalogue accountCatalogue);
}
