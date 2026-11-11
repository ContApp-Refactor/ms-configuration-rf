package co.unicauca.edu.co.contables.billing.domain.model;

import java.util.List;

/**
 * Totales del documento: suma de las lineas ya redondeadas (D6). {@code withholdings} es la suma de
 * las retenciones (siempre positiva aqui) y {@code total} ya es NETO: subtotal + impuestos - retenciones.
 */
public record DocumentTotals(Money subtotal, Money tax, Money total, Money withholdings) {

    /** Sin retenciones. */
    public DocumentTotals(Money subtotal, Money tax, Money total) {
        this(subtotal, tax, total, Money.zero());
    }

    public static DocumentTotals zero() {
        return new DocumentTotals(Money.zero(), Money.zero(), Money.zero(), Money.zero());
    }

    public static DocumentTotals fromLines(List<DocumentLine> lines) {
        return fromLines(lines, List.of());
    }

    /** Totales de las lineas menos las retenciones, calculadas sobre el subtotal completo. */
    public static DocumentTotals fromLines(List<DocumentLine> lines, List<Withholding> withholdings) {
        Money subtotal = Money.zero();
        Money tax = Money.zero();
        Money total = Money.zero();
        for (DocumentLine line : lines) {
            subtotal = subtotal.plus(line.subtotal());
            tax = tax.plus(line.taxAmount());
            total = total.plus(line.total());
        }
        Money withheld = Money.zero();
        for (Withholding withholding : withholdings) {
            withheld = withheld.plus(withholding.amountOn(subtotal));
        }
        return new DocumentTotals(subtotal, tax, total.plus(Money.of(withheld.value().negate())), withheld);
    }
}
