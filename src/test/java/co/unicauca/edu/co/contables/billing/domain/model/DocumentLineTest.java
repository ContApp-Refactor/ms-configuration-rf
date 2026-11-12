package co.unicauca.edu.co.contables.billing.domain.model;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.qty;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import org.junit.jupiter.api.Test;

/** DOC-2, DOC-3, DOC-5, DOC-7 (RN-09..RN-14): linea con precio/IVA efectivo = override ?? catalogo. */
class DocumentLineTest {

    @Test
    void RN_09_without_overrides_the_line_follows_the_catalog() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("2"), null, null);

        assertThat(line.unitPrice()).isEqualTo(money("100.00"));
        assertThat(line.taxRate()).isEqualTo(rate("19"));
        assertThat(line.priceOverridden()).isFalse();
        assertThat(line.taxOverridden()).isFalse();
        assertThat(line.subtotal()).isEqualTo(money("200.00"));
        assertThat(line.taxAmount()).isEqualTo(money("38.00"));
        assertThat(line.total()).isEqualTo(money("238.00"));
    }

    @Test
    void DOC_5_three_units_of_3_33_with_19_percent_gives_999_190_1189() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "3.33", "19"), qty("3"), null, null);

        assertThat(line.subtotal()).isEqualTo(Money.ofCents(999L));
        assertThat(line.taxAmount()).isEqualTo(Money.ofCents(190L));
        assertThat(line.total()).isEqualTo(Money.ofCents(1189L));
    }

    @Test
    void RN_10_price_override_wins_over_catalog_and_tax_still_follows_catalog() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("1"), money("150.00"), null);

        assertThat(line.unitPrice()).isEqualTo(money("150.00"));
        assertThat(line.catalogUnitPrice()).isEqualTo(money("100.00"));
        assertThat(line.priceOverridden()).isTrue();
        assertThat(line.taxOverridden()).isFalse();
        assertThat(line.taxRate()).isEqualTo(rate("19"));
        assertThat(line.total()).isEqualTo(money("178.50"));
    }

    @Test
    void RN_12_tax_override_is_independent_from_price_override() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("1"), null, rate("5"));

        assertThat(line.unitPrice()).isEqualTo(money("100.00"));
        assertThat(line.priceOverridden()).isFalse();
        assertThat(line.taxRate()).isEqualTo(rate("5"));
        assertThat(line.catalogTaxRate()).isEqualTo(rate("19"));
        assertThat(line.taxOverridden()).isTrue();
        assertThat(line.taxAmount()).isEqualTo(money("5.00"));
    }

    @Test
    void RN_12_zero_tax_override_is_a_real_override_not_a_clear() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("1"), null, TaxRate.zero());

        assertThat(line.taxOverridden()).isTrue();
        assertThat(line.taxAmount()).isEqualTo(Money.zero());
    }

    @Test
    void RN_11_clearing_an_override_makes_the_line_follow_the_catalog_again() {
        ProductSnapshot catalog = product(5L, "100.00", "19");
        DocumentLine overridden = DocumentLine.forInvoice(1, catalog, qty("1"), money("150.00"), rate("5"));
        DocumentLine cleared = DocumentLine.forInvoice(1, catalog, qty("1"), null, null);

        assertThat(overridden.unitPrice()).isEqualTo(money("150.00"));
        assertThat(cleared.unitPrice()).isEqualTo(money("100.00"));
        assertThat(cleared.taxRate()).isEqualTo(rate("19"));
        assertThat(cleared.priceOverridden()).isFalse();
        assertThat(cleared.taxOverridden()).isFalse();
    }

    @Test
    void DOC_7_negative_price_override_is_rejected_as_invalid_line() {
        assertThatThrownBy(() -> DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("1"),
                money("-0.01"), null))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
    }

    @Test
    void DOC_7_zero_price_is_allowed() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("4"), Money.zero(), null);

        assertThat(line.unitPrice()).isEqualTo(Money.zero());
        assertThat(line.total()).isEqualTo(Money.zero());
    }

    @Test
    void DOC_7_negative_catalog_price_without_override_is_rejected() {
        assertThatThrownBy(() -> DocumentLine.forInvoice(1, product(5L, "-1.00", "19"), qty("1"), null, null))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
    }

    @Test
    void DOC_7_line_number_must_be_positive() {
        assertThatThrownBy(() -> DocumentLine.forInvoice(0, product(5L, "100.00", "19"), qty("1"), null, null))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
    }

    @Test
    void RN_10_withCatalog_moves_non_overridden_values_with_the_new_catalog() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("1"), null, null);

        DocumentLine refreshed = line.withCatalog(product(5L, "200.00", "5"));

        assertThat(refreshed.unitPrice()).isEqualTo(money("200.00"));
        assertThat(refreshed.taxRate()).isEqualTo(rate("5"));
        assertThat(refreshed.catalogUnitPrice()).isEqualTo(money("200.00"));
        // la original es inmutable
        assertThat(line.unitPrice()).isEqualTo(money("100.00"));
    }

    @Test
    void RN_10_withCatalog_keeps_overrides_but_updates_catalog_reference() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("1"), money("150.00"), null);

        DocumentLine refreshed = line.withCatalog(product(5L, "200.00", "5"));

        assertThat(refreshed.unitPrice()).isEqualTo(money("150.00"));
        assertThat(refreshed.catalogUnitPrice()).isEqualTo(money("200.00"));
        assertThat(refreshed.priceOverridden()).isTrue();
        assertThat(refreshed.taxRate()).isEqualTo(rate("5"));
        assertThat(refreshed.taxOverridden()).isFalse();
    }

    @Test
    void DOC_2_withCatalog_refreshes_product_data_and_keeps_quantity_and_line_number() {
        DocumentLine line = DocumentLine.forInvoice(3, product(5L, "100.00", "19"), qty("2.5"), null, null);
        ProductSnapshot renamed = new ProductSnapshot(5L, "NEW-CODE", "Nuevo nombre", 9L, "Caja",
                money("100.00"), rate("19"), true);

        DocumentLine refreshed = line.withCatalog(renamed);

        assertThat(refreshed.productCode()).isEqualTo("NEW-CODE");
        assertThat(refreshed.productName()).isEqualTo("Nuevo nombre");
        assertThat(refreshed.unitOfMeasureId()).isEqualTo(9L);
        assertThat(refreshed.unitOfMeasureName()).isEqualTo("Caja");
        assertThat(refreshed.quantity()).isEqualTo(qty("2.5"));
        assertThat(refreshed.lineNo()).isEqualTo(3);
    }

    @Test
    void DOC_2_withCatalog_of_another_product_is_a_programming_error() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("1"), null, null);

        assertThatThrownBy(() -> line.withCatalog(product(6L, "100.00", "19")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void D9_renumber_changes_only_the_line_number() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("2"), money("150.00"), null);

        DocumentLine renumbered = line.renumber(7);

        assertThat(renumbered.lineNo()).isEqualTo(7);
        assertThat(renumbered).isEqualTo(DocumentLine.restore(7, null, 5L, line.productCode(), line.productName(),
                line.unitOfMeasureId(), line.unitOfMeasureName(), line.quantity(), line.catalogUnitPrice(),
                line.catalogTaxRate(), line.unitPriceOverride(), line.taxRateOverride()));
        assertThat(line.lineNo()).isEqualTo(1);
    }

    @Test
    void D9_invoice_lines_have_no_source_line() {
        DocumentLine line = DocumentLine.forInvoice(1, product(5L, "100.00", "19"), qty("1"), null, null);

        assertThat(line.sourceLineNo()).isNull();
    }

    @Test
    void RN_14_restore_rebuilds_the_same_line_with_same_derived_amounts() {
        DocumentLine original = DocumentLine.forInvoice(2, product(8L, "3.33", "19"), qty("3"), null, rate("10"));

        DocumentLine restored = DocumentLine.restore(original.lineNo(), original.sourceLineNo(),
                original.productId(), original.productCode(), original.productName(), original.unitOfMeasureId(),
                original.unitOfMeasureName(), original.quantity(), original.catalogUnitPrice(),
                original.catalogTaxRate(), original.unitPriceOverride(), original.taxRateOverride());

        assertThat(restored).isEqualTo(original);
        assertThat(restored.total()).isEqualTo(original.total());
        assertThat(restored.taxRate()).isEqualTo(rate("10"));
    }
}
