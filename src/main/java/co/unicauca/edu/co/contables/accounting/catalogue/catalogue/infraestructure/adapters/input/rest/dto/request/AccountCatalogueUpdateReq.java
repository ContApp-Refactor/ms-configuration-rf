package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request;

import lombok.*;

/**
 * @brief DTO para solicitud de actualización de cuenta contable
 *
 * Contiene los campos modificables para actualizar una cuenta existente
 * en el catálogo, permitiendo cambios selectivos de propiedades.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCatalogueUpdateReq {

    private String idEnterprise; 
    private String code;
    private String description;
    private String nature;
    private String financialStatus;
    private String classification;
    private Long parent;
    private Boolean crossing;
    private Boolean costCenter;
    private Boolean status;
}
