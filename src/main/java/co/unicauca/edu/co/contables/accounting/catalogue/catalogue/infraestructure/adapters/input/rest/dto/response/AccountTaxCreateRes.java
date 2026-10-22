package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

import lombok.*;

/**
 * @brief DTO para respuesta de creación de asociación cuenta-impuesto
 *
 * Contiene la información completa de la asociación creada entre
 * una cuenta contable y un impuesto (venta o compra).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountTaxCreateRes {
    private Long id;
    private AccountCatalogue accountCatalogue;
    private Tax tax;
}
