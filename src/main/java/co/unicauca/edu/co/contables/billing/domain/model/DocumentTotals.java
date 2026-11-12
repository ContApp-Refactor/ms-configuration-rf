package co.unicauca.edu.co.contables.billing.domain.model;

import java.util.List;

/** Totales del documento: suma de las lineas ya redondeadas (D6). */
public record DocumentTotals(Money subtotal, Money tax, Money total) {

    public static DocumentTotals zero() {
        return new DocumentTotals(Money.zero(), Money.zero(), Money.zero());
    }

    public static DocumentTotals fromLines(List<DocumentLine> lines) {
        Money subtotal = Money.zero();
        Money tax = Money.zero();
        Money total = Money.zero();
        for (DocumentLine line : lines) {
            subtotal = subtotal.plus(line.subtotal());
            tax = tax.plus(line.taxAmount());
            total = total.plus(line.total());
        }
        return new DocumentTotals(subtotal, tax, total);
    }
}
