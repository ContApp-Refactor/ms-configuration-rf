package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.response;

import java.time.LocalDate;
import java.util.Set;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Product;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.eFactureType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FactureGetResponse {
    private Long factId;
    private Long thId;
    private Long factCode;
    private eFactureType factureType;
    private String factObservations;
    private Double descounts;
    private Set<Product> factProducts;
    private Double factSubtotals;
    private Double facSalesTax;
    private Double facWithholdingSource;
    private LocalDate creationDate;
    private LocalDate updateDate;
}
