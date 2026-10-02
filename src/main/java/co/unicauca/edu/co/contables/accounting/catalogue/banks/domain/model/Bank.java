package co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.enums.Currency;
import lombok.*;

import java.util.Set;

/**
 * @brief Modelo de dominio que representa un banco
 *
 * Contiene la información esencial de un banco incluyendo
 * código, nombre, monedas admitidas y estado operativo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bank {
    private Long id;
    private String code;
    private String name;
    private Set<Currency> currencies;
    @Builder.Default
    private Boolean status = true;
    private String idEnterprise;
}
