package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.billing.application.ports.output.TaxCatalogPort;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Impuestos ACTIVOS de la empresa, resueltos <strong>en el mismo proceso</strong> con el puerto de
 * entrada del maestro de impuestos ({@link ITaxSearchInputPort#getActiveTaxes}). El campo
 * {@code interest} es el porcentaje; el nombre es el codigo del maestro ("Iva") y, sin codigo, la
 * descripcion. Nada se cachea: la tarifa puede cambiar entre peticiones.
 */
@Component("billingTaxCatalogAdapter")
public class BillingTaxCatalogAdapter implements TaxCatalogPort {

    private final ITaxSearchInputPort taxSearch;

    public BillingTaxCatalogAdapter(ITaxSearchInputPort taxSearch) {
        this.taxSearch = taxSearch;
    }

    @Override
    public List<TaxRef> listActiveTaxes(String enterpriseId) {
        List<Tax> taxes = taxSearch.getActiveTaxes(enterpriseId);
        if (taxes == null) {
            return List.of();
        }
        return taxes.stream()
                .filter(t -> t != null && t.getId() != null)
                .map(t -> new TaxRef(t.getId(), nameOf(t),
                        t.getInterest() == null ? BigDecimal.ZERO : BigDecimal.valueOf(t.getInterest())))
                .toList();
    }

    private static String nameOf(Tax tax) {
        if (tax.getCode() != null && !tax.getCode().isBlank()) {
            return tax.getCode();
        }
        if (tax.getDescription() != null && !tax.getDescription().isBlank()) {
            return tax.getDescription();
        }
        return "Impuesto";
    }
}
