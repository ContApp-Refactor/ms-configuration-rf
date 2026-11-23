package co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.response;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.enums.Currency;
import lombok.*;

import java.util.Set;

/**
 * @brief DTO de respuesta para bancos
 *
 * Contiene la información completa de un banco
 * para respuestas de la API REST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankRes {
    private Long id;
    private String code;
    private String name;
    private Set<Currency> currencies;
    private Boolean status;
    private String idEnterprise;
}
