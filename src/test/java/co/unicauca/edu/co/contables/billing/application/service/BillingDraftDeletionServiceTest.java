package co.unicauca.edu.co.contables.billing.application.service;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** DRF-5 (RN-07, RN-23b): solo un BORRADOR puede eliminarse. */
@ExtendWith(MockitoExtension.class)
class BillingDraftDeletionServiceTest {

    private static final UUID ID = uuid(1);

    @Mock
    private BillingDocumentRepositoryPort repository;

    private BillingDraftDeletionService service;

    @BeforeEach
    void setUp() {
        service = new BillingDraftDeletionService(repository, new BillingDocumentLoader(repository));
    }

    private void stored(BillingDocument doc) {
        when(repository.findById(ENTERPRISE_ID, ID)).thenReturn(Optional.of(doc));
    }

    private static void assertCode(Throwable thrown, BillingErrorCode code) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode()).isEqualTo(code);
    }

    @Test
    void DRF_5_deleting_a_draft_loads_it_then_deletes_it() {
        BillingDocument doc = withVersion(readySaleDraft(), 2L);
        stored(doc);

        service.delete(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID, 2L);

        InOrder order = inOrder(repository);
        order.verify(repository).findById(ENTERPRISE_ID, ID);
        order.verify(repository).delete(doc);
    }

    @Test
    void DRF_5_a_partial_draft_can_be_deleted() {
        BillingDocument doc = withVersion(draftSale(), 0L);
        stored(doc);

        service.delete(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID, null);

        verify(repository).delete(doc);
    }

    @Test
    void DRF_5_a_purchase_draft_can_be_deleted() {
        BillingDocument doc = withVersion(readyPurchaseDraft(), 1L);
        stored(doc);

        service.delete(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, ID, 1L);

        verify(repository).delete(doc);
    }

    @Test
    void DRF_5_deleting_an_emitted_invoice_fails_409_and_keeps_the_row() {
        stored(withVersion(emittedSale(), 1L));

        Throwable thrown = catchThrowable(() -> service.delete(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID, 1L));

        assertCode(thrown, BillingErrorCode.BILLING_INVALID_STATE);
        verify(repository, never()).delete(any());
    }

    @Test
    void DRF_5_deleting_an_annulled_invoice_fails_409_and_keeps_the_row() {
        stored(withVersion(annulledSale(), 2L));

        Throwable thrown = catchThrowable(() -> service.delete(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID, 2L));

        assertCode(thrown, BillingErrorCode.BILLING_INVALID_STATE);
        verify(repository, never()).delete(any());
    }

    @Test
    void DRF_3_deleting_with_a_stale_version_fails_409_and_keeps_the_row() {
        stored(withVersion(readySaleDraft(), 5L));

        Throwable thrown = catchThrowable(() -> service.delete(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID, 4L));

        assertCode(thrown, BillingErrorCode.BILLING_CONCURRENT_MODIFICATION);
        verify(repository, never()).delete(any());
    }

    @Test
    void DRF_5_deleting_an_unknown_document_fails_404() {
        when(repository.findById(ENTERPRISE_ID, ID)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> service.delete(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID, 1L));

        assertCode(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        verify(repository, never()).delete(any());
    }

    @Test
    void PUR_3_deleting_through_the_wrong_type_behaves_as_not_found() {
        stored(withVersion(readySaleDraft(), 1L));

        Throwable thrown = catchThrowable(() -> service.delete(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, ID, 1L));

        assertCode(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        verify(repository, never()).delete(any());
    }
}
