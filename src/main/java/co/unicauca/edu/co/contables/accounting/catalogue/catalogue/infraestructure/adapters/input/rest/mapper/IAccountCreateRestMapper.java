package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AccountCatalogueCreateRes;

/**
 * @brief Mapper para operaciones de creación de cuentas contables
 *
 * Define contratos para conversión entre DTOs de request/response y modelos de dominio
 * en operaciones de creación de cuentas, manejando jerarquía y asociaciones.
 */
public interface IAccountCreateRestMapper {
    /**
     * @brief Convierte modelo de dominio a respuesta de creación
     * @param accountCatalogue modelo de dominio de cuenta creada
     * @return DTO de respuesta con datos de la cuenta creada
     */
    AccountCatalogueCreateRes toCreateResponse(AccountCatalogue accountCatalogue);

    /**
     * @brief Convierte solicitud de creación a modelo de dominio
     * @param accountCatalogueCreateReq DTO de solicitud de creación
     * @param aux modelo auxiliar para jerarquía (cuenta padre)
     * @return modelo de dominio listo para persistencia
     */
    AccountCatalogue toDomain(AccountCatalogueCreateReq accountCatalogueCreateReq, AccountCatalogue aux);

}
