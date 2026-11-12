package co.unicauca.edu.co.contables.billing.application.ports.output;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Catalogo de impuestos de la empresa, de solo lectura. Billing lo usa para las retenciones: el
 * impuesto elegido debe existir, estar ACTIVO y ser de la empresa; de el se toma la tarifa (%).
 */
public interface TaxCatalogPort {

    /** Impuestos ACTIVOS de la empresa. */
    List<TaxRef> listActiveTaxes(String enterpriseId);

    /** Impuesto activo de la empresa, o vacio si no existe, esta inactivo o es de otra empresa. */
    default Optional<TaxRef> findActiveTax(String enterpriseId, long taxId) {
        return listActiveTaxes(enterpriseId).stream().filter(t -> t.id() == taxId).findFirst();
    }

    /** {@code ratePct} es la tarifa en porcentaje tal como esta en el maestro (puede venir fuera de 0..100). */
    record TaxRef(long id, String name, BigDecimal ratePct) {
    }
}
