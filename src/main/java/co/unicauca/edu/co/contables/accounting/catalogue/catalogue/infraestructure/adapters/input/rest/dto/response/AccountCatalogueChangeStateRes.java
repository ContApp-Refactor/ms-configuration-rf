package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO para respuesta de cambio de estado de cuenta contable
 *
 * Contiene confirmación del cambio de estado (activación/desactivación)
 * de una cuenta junto con mensaje informativo del resultado.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCatalogueChangeStateRes {
    private Long id;
    private String code;
    private String description;
    private Boolean status;
    private String message;
}
