package co.unicauca.edu.co.contables.billing.application.service;

import co.unicauca.edu.co.contables.billing.domain.event.ReturnEventContext;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import co.unicauca.edu.co.contables.billing.domain.service.ReturnAvailabilityCalculator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Arma el {@link ReturnEventContext} que acompana a los eventos de emision y anulacion de una
 * devolucion (RET-9, RET-10): su ordinal dentro de la factura original y el estado de devolucion
 * de la original DESPUES de la operacion. Java plano, sin estado.
 */
public final class ReturnEventContextFactory {

    private final ReturnAvailabilityCalculator calculator;

    public ReturnEventContextFactory() {
        this(new ReturnAvailabilityCalculator());
    }

    public ReturnEventContextFactory(ReturnAvailabilityCalculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
    }

    /**
     * @param returnDocument la devolucion ya persistida en su estado final (EMITIDA tras emitir,
     *                       ANULADA tras anular); es la fuente de verdad sobre si misma
     * @param original       la factura original
     * @param returnsOfOriginal devoluciones de la original tal como las devuelve el repositorio; puede
     *                       incluir o no a {@code returnDocument} (se reemplaza por la instancia dada)
     */
    public ReturnEventContext forReturn(BillingDocument returnDocument, BillingDocument original,
            Collection<BillingDocument> returnsOfOriginal) {
        Objects.requireNonNull(returnDocument, "returnDocument");
        Objects.requireNonNull(original, "original");
        if (!returnDocument.type().isReturn()) {
            throw new IllegalArgumentException(returnDocument.type() + " no es una devolucion");
        }
        List<BillingDocument> all = withDocument(returnsOfOriginal, returnDocument);
        Integer ordinal = ReturnAvailabilityCalculator.ordinalsOf(all).get(returnDocument.id());
        ReturnStatus statusAfter = calculator.availability(original, all).status();
        return new ReturnEventContext(ordinal, statusAfter);
    }

    /** Copia de las devoluciones dadas donde {@code document} figura exactamente una vez. */
    static List<BillingDocument> withDocument(Collection<BillingDocument> returns, BillingDocument document) {
        List<BillingDocument> all = new ArrayList<>();
        if (returns != null) {
            for (BillingDocument other : returns) {
                if (!other.id().equals(document.id())) {
                    all.add(other);
                }
            }
        }
        all.add(document);
        return all;
    }
}
