package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO para respuesta de búsqueda individual de cuenta contable
 *
 * Contiene datos completos de una cuenta específica encontrada por código,
 * utilizado en operaciones de búsqueda y consulta individual.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemAccountCatalogueSearchRes {
    Long id;
    String code;
    private  String description;
    private String nature;
    private String financialStatus;
    private String classification;
    private String parent;
    private Boolean crossing;
    private Boolean costCenter;
    private Boolean status;
    private Integer usageCount;
}
