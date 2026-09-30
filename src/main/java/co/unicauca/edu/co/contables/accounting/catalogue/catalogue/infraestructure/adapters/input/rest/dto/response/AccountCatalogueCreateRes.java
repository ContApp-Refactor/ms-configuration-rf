package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para respuesta de creación de cuenta contable
 *
 * Contiene los datos de la cuenta recién creada incluyendo el ID generado
 * y toda la información confirmada del proceso de creación.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCatalogueCreateRes {
    private Long id;
    private String idEnterprise;
    private String code;
    private String description;
    private String nature;
    private String financialStatus;
    private String classification;
    private String parent;
    private Boolean crossing;
    private Boolean costCenter;
    private Boolean status;

}
