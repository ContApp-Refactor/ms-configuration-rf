package co.unicauca.edu.co.contables.billing.application.service;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand.WithholdingCommand;
import co.unicauca.edu.co.contables.billing.application.ports.output.TaxCatalogPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.TaxCatalogPort.TaxRef;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.TaxRate;
import co.unicauca.edu.co.contables.billing.domain.model.Withholding;
import co.unicauca.edu.co.contables.billing.domain.model.WithholdingCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte retenciones pedidas (codigo + impuesto) en {@link Withholding} con la tarifa vigente del
 * impuesto de la empresa, y refresca las ya guardadas. Solo impuestos ACTIVOS de la empresa; el
 * catalogo se consulta una sola vez por operacion y solo si hay retenciones.
 */
final class WithholdingResolver {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TaxCatalogPort catalog;

    WithholdingResolver(TaxCatalogPort catalog) {
        this.catalog = catalog;
    }

    /** Valida y resuelve lo pedido en el borrador; cualquier problema es un 422. */
    List<Withholding> resolve(String enterpriseId, DocumentType type, List<WithholdingCommand> requested) {
        if (requested == null || requested.isEmpty()) {
            return List.of();
        }
        Map<Long, TaxRef> taxes = null;
        List<Withholding> result = new ArrayList<>(requested.size());
        for (WithholdingCommand item : requested) {
            if (item == null || item.code() == null) {
                throw new BillingException(BillingErrorCode.BILLING_WITHHOLDING_INVALID,
                        "El codigo de la retencion es obligatorio");
            }
            item.code().requireAllowedFor(type);
            if (item.taxId() == null) {
                throw new BillingException(BillingErrorCode.BILLING_WITHHOLDING_TAX_INVALID,
                        "La retencion " + item.code() + " requiere un impuesto (taxId)");
            }
            if (taxes == null) {
                taxes = activeTaxes(enterpriseId);
            }
            result.add(build(item.code(), item.taxId(), taxes.get(item.taxId())));
        }
        return result;
    }

    /**
     * Misma retencion con la tarifa y el nombre vigentes. {@code strict}: un impuesto que ya no
     * existe o esta inactivo es BILLING_WITHHOLDING_TAX_INVALID (emision). No estricto: se conserva
     * lo guardado (lectura de un borrador) y un fallo del catalogo tampoco bloquea.
     */
    List<Withholding> refresh(String enterpriseId, List<Withholding> current, boolean strict) {
        if (current == null || current.isEmpty()) {
            return List.of();
        }
        Map<Long, TaxRef> taxes;
        try {
            taxes = activeTaxes(enterpriseId);
        } catch (RuntimeException ex) {
            if (strict) {
                throw ex;
            }
            return current;
        }
        List<Withholding> result = new ArrayList<>(current.size());
        for (Withholding withholding : current) {
            TaxRef tax = taxes.get(withholding.taxId());
            if (tax == null && !strict) {
                result.add(withholding);
                continue;
            }
            try {
                result.add(build(withholding.code(), withholding.taxId(), tax));
            } catch (BillingException ex) {
                if (strict) {
                    throw ex;
                }
                result.add(withholding);
            }
        }
        return result;
    }

    private Map<Long, TaxRef> activeTaxes(String enterpriseId) {
        if (catalog == null) {
            throw new BillingException(BillingErrorCode.BILLING_INTERNAL_ERROR,
                    "El catalogo de impuestos no esta disponible");
        }
        Map<Long, TaxRef> map = new HashMap<>();
        for (TaxRef tax : catalog.listActiveTaxes(enterpriseId)) {
            map.put(tax.id(), tax);
        }
        return map;
    }

    private static Withholding build(WithholdingCode code, long taxId, TaxRef tax) {
        if (tax == null) {
            throw new BillingException(BillingErrorCode.BILLING_WITHHOLDING_TAX_INVALID,
                    "El impuesto " + taxId + " no existe o esta inactivo en la empresa");
        }
        BigDecimal pct = tax.ratePct() == null ? BigDecimal.ZERO : tax.ratePct().setScale(2, RoundingMode.HALF_UP);
        if (pct.signum() < 0 || pct.compareTo(HUNDRED) > 0) {
            throw new BillingException(BillingErrorCode.BILLING_WITHHOLDING_TAX_INVALID,
                    "La tarifa del impuesto " + tax.name() + " debe estar entre 0 y 100");
        }
        return new Withholding(code, taxId, tax.name(), TaxRate.of(pct));
    }
}
