package co.unicauca.edu.co.contables.billing.domain.model;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_INVOICE_NUMBER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.qty;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.supplier;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** DRF-6 (HU-12, RN-24): duplicar un borrador de factura. */
class BillingDocumentDuplicateTest {

    private static final LocalDate DUPLICATE_DAY = LocalDate.of(2026, 3, 10);

    @Test
    void DRF_6_duplicate_creates_a_new_draft_with_same_counterparty_and_lines_and_todays_date() {
        BillingDocument source = readySaleDraft();

        BillingDocument copy = source.duplicateAsDraft(uuid(77), DUPLICATE_DAY, LATER, OTHER_USER);

        assertThat(copy.id()).isEqualTo(uuid(77));
        assertThat(copy.id()).isNotEqualTo(source.id());
        assertThat(copy.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(copy.type()).isEqualTo(DocumentType.SALE_INVOICE);
        assertThat(copy.enterpriseId()).isEqualTo(ENTERPRISE_ID);
        assertThat(copy.number()).isNull();
        assertThat(copy.version()).isNull();
        assertThat(copy.counterparty()).isEqualTo(customer());
        assertThat(copy.lines()).isEqualTo(source.lines());
        assertThat(copy.totals()).isEqualTo(source.totals());
        assertThat(copy.issueDate()).isEqualTo(DUPLICATE_DAY);
        assertThat(copy.createdAt()).isEqualTo(LATER);
        assertThat(copy.createdBy()).isEqualTo(OTHER_USER);
        assertThat(copy.updatedAt()).isEqualTo(LATER);
        assertThat(copy.emittedAt()).isNull();
    }

    @Test
    void DRF_6_duplicate_preserves_override_flags() {
        BillingDocument source = draftSale();
        source.replaceInvoiceDraft(customer(), TODAY, null, List.of(
                DocumentLine.forInvoice(1, product(1L, "100.00", "19"), qty("2"), money("150.00"), rate("5")),
                lineOf(2, product(2L), "1")), NOW, USER);

        BillingDocument copy = source.duplicateAsDraft(uuid(78), DUPLICATE_DAY, LATER, USER);

        assertThat(copy.lines().get(0).priceOverridden()).isTrue();
        assertThat(copy.lines().get(0).taxOverridden()).isTrue();
        assertThat(copy.lines().get(0).unitPrice()).isEqualTo(money("150.00"));
        assertThat(copy.lines().get(1).priceOverridden()).isFalse();
        assertThat(copy.lines().get(1).taxOverridden()).isFalse();
    }

    @Test
    void DRF_6_duplicate_does_not_modify_the_source() {
        BillingDocument source = withVersion(readySaleDraft(), 2L);
        BillingDocumentState before = source.state();

        source.duplicateAsDraft(uuid(79), DUPLICATE_DAY, LATER, USER);

        assertThat(source.state()).isEqualTo(before);
    }

    @Test
    void DRF_6_duplicate_of_a_purchase_does_not_copy_the_supplier_invoice_number() {
        BillingDocument source = readyPurchaseDraft();
        assertThat(source.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);

        BillingDocument copy = source.duplicateAsDraft(uuid(80), DUPLICATE_DAY, LATER, USER);

        assertThat(copy.type()).isEqualTo(DocumentType.PURCHASE_INVOICE);
        assertThat(copy.supplierInvoiceNumber()).isNull();
        assertThat(copy.counterparty()).isEqualTo(supplier());
        assertThat(copy.lines()).hasSize(1);
    }

    @Test
    void DRF_6_duplicate_of_a_partial_draft_is_a_partial_draft() {
        BillingDocument source = draftSale();

        BillingDocument copy = source.duplicateAsDraft(uuid(81), DUPLICATE_DAY, LATER, USER);

        assertThat(copy.counterparty()).isNull();
        assertThat(copy.lines()).isEmpty();
        assertThat(copy.totals()).isEqualTo(DocumentTotals.zero());
    }

    @Test
    void DRF_6_duplicate_of_a_return_draft_is_not_allowed() {
        BillingDocument returnDraft = BillingDocument.restore(
                co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSaleReturnState("motivo"));

        assertThatThrownBy(() -> returnDraft.duplicateAsDraft(uuid(82), DUPLICATE_DAY, LATER, USER))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_INVALID_STATE);
    }
}
