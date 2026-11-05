package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response;

import java.util.List;

import lombok.*;

/**
 * @brief DTO para respuesta de lista de cuentas auxiliares
 *
 * Contiene listado paginado de cuentas auxiliares (8 dígitos) disponibles
 * para operaciones específicas como asignación de centros de costo o cruce.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuxiliaryAccountListRes {
    private List<ItemAccountCatalogueSearchRes> auxiliaryAccounts;
    private int totalCount;
    private String idEnterprise;
}
