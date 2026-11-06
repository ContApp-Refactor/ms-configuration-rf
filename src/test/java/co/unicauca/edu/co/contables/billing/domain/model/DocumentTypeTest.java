package co.unicauca.edu.co.contables.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** DOC-1 / D-D: cuatro tipos de documento, prefijos y relacion factura-devolucion. */
class DocumentTypeTest {

    @Test
    void DOC_1_prefixes_per_type() {
        assertThat(DocumentType.SALE_INVOICE.prefix()).isEqualTo("FV");
        assertThat(DocumentType.PURCHASE_INVOICE.prefix()).isEqualTo("FC");
        assertThat(DocumentType.SALE_RETURN.prefix()).isEqualTo("NC");
        assertThat(DocumentType.PURCHASE_RETURN.prefix()).isEqualTo("DC");
    }

    @Test
    void DOC_1_kind_and_return_flag_per_type() {
        assertThat(DocumentType.SALE_INVOICE.kind()).isEqualTo(DocumentKind.SALE);
        assertThat(DocumentType.SALE_RETURN.kind()).isEqualTo(DocumentKind.SALE);
        assertThat(DocumentType.PURCHASE_INVOICE.kind()).isEqualTo(DocumentKind.PURCHASE);
        assertThat(DocumentType.PURCHASE_RETURN.kind()).isEqualTo(DocumentKind.PURCHASE);

        assertThat(DocumentType.SALE_INVOICE.isReturn()).isFalse();
        assertThat(DocumentType.PURCHASE_INVOICE.isInvoice()).isTrue();
        assertThat(DocumentType.SALE_RETURN.isReturn()).isTrue();
        assertThat(DocumentType.PURCHASE_RETURN.isInvoice()).isFalse();
    }

    @Test
    void DOC_1_counterparty_role_follows_kind() {
        assertThat(DocumentKind.SALE.counterpartyRole()).isEqualTo(CounterpartyRole.CUSTOMER);
        assertThat(DocumentKind.PURCHASE.counterpartyRole()).isEqualTo(CounterpartyRole.SUPPLIER);
    }

    @Test
    void RET_1_returnType_of_invoices() {
        assertThat(DocumentType.SALE_INVOICE.returnType()).isEqualTo(DocumentType.SALE_RETURN);
        assertThat(DocumentType.PURCHASE_INVOICE.returnType()).isEqualTo(DocumentType.PURCHASE_RETURN);
    }

    @Test
    void RET_1_originType_of_returns() {
        assertThat(DocumentType.SALE_RETURN.originType()).isEqualTo(DocumentType.SALE_INVOICE);
        assertThat(DocumentType.PURCHASE_RETURN.originType()).isEqualTo(DocumentType.PURCHASE_INVOICE);
    }

    @Test
    void RET_1_returnType_on_a_return_and_originType_on_an_invoice_are_illegal() {
        assertThatThrownBy(DocumentType.SALE_RETURN::returnType).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(DocumentType.PURCHASE_INVOICE::originType).isInstanceOf(IllegalStateException.class);
    }
}
