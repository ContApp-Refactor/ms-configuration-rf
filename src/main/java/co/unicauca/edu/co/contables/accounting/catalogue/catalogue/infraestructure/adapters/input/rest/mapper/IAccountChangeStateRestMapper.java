package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AccountCatalogueChangeStateRes;

/**
 * @brief Mapper para operaciones de cambio de estado de cuentas contables
 *
 * Define contratos para conversión entre modelos de dominio y DTOs de respuesta
 * en operaciones de activación/desactivación de cuentas.
 */
public interface IAccountChangeStateRestMapper {
    
    /**
     * @brief Convierte modelo de dominio a respuesta de cambio de estado
     *
     * Genera mensaje automático basado en el estado (activado/desactivado)
     * y construye respuesta completa con confirmación del cambio.
     * @param accountCatalogue modelo de dominio con estado modificado
     * @return DTO de respuesta con confirmación y mensaje informativo
     */
    default AccountCatalogueChangeStateRes toChangeStateResponse(AccountCatalogue accountCatalogue) {
        if (accountCatalogue == null) {
            return null;
        }

        String statusMessage = accountCatalogue.getStatus() ? "Cuenta activada exitosamente" : "Cuenta desactivada exitosamente";

        return AccountCatalogueChangeStateRes.builder()
                .id(accountCatalogue.getId())
                .code(accountCatalogue.getCode())
                .description(accountCatalogue.getDescription())
                .status(accountCatalogue.getStatus())
                .message(statusMessage)
                .build();
    }
}
