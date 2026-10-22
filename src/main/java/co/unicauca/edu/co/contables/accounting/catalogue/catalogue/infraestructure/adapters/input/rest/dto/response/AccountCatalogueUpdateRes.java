package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO para respuesta de actualización de cuenta contable
 *
 * Contiene los datos actualizados de la cuenta después de una operación
 * de modificación, confirmando los cambios realizados.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCatalogueUpdateRes {
    private long id;
    private String code;
    private String description;
    private String nature;
    private String financialStatus;
    private String classification;
    private String parent;
    private Boolean crossing;
    private Boolean costCenter;
    private Boolean status;
    private Integer usageCount;
}
