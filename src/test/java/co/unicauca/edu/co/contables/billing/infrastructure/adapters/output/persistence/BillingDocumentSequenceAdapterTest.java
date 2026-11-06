package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_ENTERPRISE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingTransactionRunnerPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentSequenceEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository.BillingDocumentSequenceJpaRepository;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;

/**
 * EMT-3 (D11) con repositorio simulado: incremento de la fila bloqueada, alta de la fila faltante en
 * transaccion nueva, carrera de altas (la perdedora relee) y traduccion de fallos de bloqueo a
 * {@code BILLING_SEQUENCE_BUSY}. El comportamiento con bloqueos reales esta en
 * {@code BillingDocumentSequenceAdapterConcurrencyTest}.
 */
@ExtendWith(MockitoExtension.class)
class BillingDocumentSequenceAdapterTest {

    @Mock
    private BillingDocumentSequenceJpaRepository repository;
    @Mock
    private BillingTransactionRunnerPort transactions;
    @Mock
    private BillingClockPort clock;

    private BillingDocumentSequenceAdapter adapter;

    @BeforeEach
    void setUp() {
        lenient().when(clock.now()).thenReturn(NOW);
        adapter = new BillingDocumentSequenceAdapter(repository, transactions, clock);
    }

    private static BillingDocumentSequenceEntity row(String enterpriseId, DocumentType type, long last) {
        BillingDocumentSequenceEntity entity = new BillingDocumentSequenceEntity();
        entity.setEnterpriseId(enterpriseId);
        entity.setDocumentType(type.name());
        entity.setPrefix(type.prefix());
        entity.setLastNumber(last);
        entity.setUpdatedAt(NOW.minusSeconds(3600));
        return entity;
    }

    private static void assertSequenceBusy(Throwable thrown) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode()).isEqualTo(BillingErrorCode.BILLING_SEQUENCE_BUSY);
    }

    // ---------- fila existente ----------

    @Test
    void EMT_3_an_existing_row_is_locked_incremented_and_persisted() {
        BillingDocumentSequenceEntity existing = row(ENTERPRISE_ID, DocumentType.SALE_INVOICE, 7L);
        when(repository.findForUpdate(ENTERPRISE_ID, "SALE_INVOICE")).thenReturn(Optional.of(existing));

        long next = adapter.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE);

        assertThat(next).isEqualTo(8L);
        assertThat(existing.getLastNumber()).isEqualTo(8L);
        assertThat(existing.getUpdatedAt()).isEqualTo(NOW);
        verify(repository).saveAndFlush(existing);
        verifyNoInteractions(transactions);
    }

    @Test
    void EMT_3_the_lookup_is_scoped_by_enterprise_and_document_type() {
        BillingDocumentSequenceEntity existing = row(OTHER_ENTERPRISE_ID, DocumentType.PURCHASE_RETURN, 0L);
        when(repository.findForUpdate(OTHER_ENTERPRISE_ID, "PURCHASE_RETURN")).thenReturn(Optional.of(existing));

        long next = adapter.nextValue(OTHER_ENTERPRISE_ID, DocumentType.PURCHASE_RETURN);

        assertThat(next).isEqualTo(1L);
        verify(repository).findForUpdate(OTHER_ENTERPRISE_ID, "PURCHASE_RETURN");
    }

    // ---------- fila faltante ----------

    @Test
    void EMT_3_a_missing_row_is_created_in_a_new_transaction_and_then_locked_again() {
        BillingDocumentSequenceEntity created = row(ENTERPRISE_ID, DocumentType.SALE_INVOICE, 0L);
        when(repository.findForUpdate(ENTERPRISE_ID, "SALE_INVOICE"))
                .thenReturn(Optional.empty(), Optional.of(created));
        doAnswer(call -> {
            ((Runnable) call.getArgument(0)).run();
            return null;
        }).when(transactions).inNewTransaction(any(Runnable.class));

        long next = adapter.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE);

        assertThat(next).isEqualTo(1L);
        InOrder order = inOrder(repository, transactions);
        order.verify(repository).findForUpdate(ENTERPRISE_ID, "SALE_INVOICE");
        order.verify(transactions).inNewTransaction(any(Runnable.class));
        order.verify(repository).saveAndFlush(any(BillingDocumentSequenceEntity.class));
        order.verify(repository).findForUpdate(ENTERPRISE_ID, "SALE_INVOICE");
    }

    @Test
    void EMT_3_the_created_row_starts_at_zero_with_the_prefix_of_the_type() {
        BillingDocumentSequenceEntity relocked = row(OTHER_ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, 0L);
        when(repository.findForUpdate(OTHER_ENTERPRISE_ID, "PURCHASE_INVOICE"))
                .thenReturn(Optional.empty(), Optional.of(relocked));
        doAnswer(call -> {
            ((Runnable) call.getArgument(0)).run();
            return null;
        }).when(transactions).inNewTransaction(any(Runnable.class));

        adapter.nextValue(OTHER_ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE);

        ArgumentCaptor<BillingDocumentSequenceEntity> captor =
                ArgumentCaptor.forClass(BillingDocumentSequenceEntity.class);
        verify(repository, times(2)).saveAndFlush(captor.capture());
        BillingDocumentSequenceEntity inserted = captor.getAllValues().get(0);
        assertThat(inserted).isNotSameAs(relocked);
        assertThat(inserted.getEnterpriseId()).isEqualTo(OTHER_ENTERPRISE_ID);
        assertThat(inserted.getDocumentType()).isEqualTo("PURCHASE_INVOICE");
        assertThat(inserted.getPrefix()).isEqualTo("FC");
        assertThat(inserted.getLastNumber()).isZero();
        assertThat(inserted.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void EMT_3_losing_the_insert_race_rereads_the_row_created_by_the_winner() {
        BillingDocumentSequenceEntity winners = row(ENTERPRISE_ID, DocumentType.SALE_INVOICE, 4L);
        when(repository.findForUpdate(ENTERPRISE_ID, "SALE_INVOICE"))
                .thenReturn(Optional.empty(), Optional.of(winners));
        doThrow(new DataIntegrityViolationException("uk_billing_seq")).when(transactions)
                .inNewTransaction(any(Runnable.class));

        long next = adapter.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE);

        assertThat(next).isEqualTo(5L);
        verify(repository, times(2)).findForUpdate(ENTERPRISE_ID, "SALE_INVOICE");
    }

    @Test
    void EMT_3_a_row_that_is_still_missing_after_the_creation_is_an_internal_error() {
        when(repository.findForUpdate(ENTERPRISE_ID, "SALE_INVOICE")).thenReturn(Optional.empty());
        doAnswer(call -> null).when(transactions).inNewTransaction(any(Runnable.class));

        Throwable thrown = catchThrowable(() -> adapter.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE));

        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode()).isEqualTo(BillingErrorCode.BILLING_INTERNAL_ERROR);
    }

    // ---------- traduccion de fallos de bloqueo ----------

    @Test
    void EMT_3_a_spring_lock_acquisition_failure_is_sequence_busy() {
        when(repository.findForUpdate(ENTERPRISE_ID, "SALE_INVOICE"))
                .thenThrow(new CannotAcquireLockException("lock"));

        assertSequenceBusy(catchThrowable(() -> adapter.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE)));
    }

    @Test
    void EMT_3_a_spring_query_timeout_is_sequence_busy() {
        when(repository.findForUpdate(ENTERPRISE_ID, "SALE_INVOICE")).thenThrow(new QueryTimeoutException("timeout"));

        assertSequenceBusy(catchThrowable(() -> adapter.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE)));
    }

    @Test
    void EMT_3_a_jpa_lock_timeout_is_sequence_busy() {
        when(repository.findForUpdate(ENTERPRISE_ID, "SALE_INVOICE")).thenThrow(new LockTimeoutException("timeout"));

        assertSequenceBusy(catchThrowable(() -> adapter.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE)));
    }

    @Test
    void EMT_3_a_jpa_pessimistic_lock_failure_is_sequence_busy() {
        when(repository.findForUpdate(ENTERPRISE_ID, "SALE_INVOICE"))
                .thenThrow(new PessimisticLockException("lock"));

        assertSequenceBusy(catchThrowable(() -> adapter.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE)));
    }

    @Test
    void EMT_3_a_lock_failure_while_incrementing_is_also_sequence_busy() {
        BillingDocumentSequenceEntity existing = row(ENTERPRISE_ID, DocumentType.SALE_INVOICE, 7L);
        when(repository.findForUpdate(ENTERPRISE_ID, "SALE_INVOICE")).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(existing)).thenThrow(new CannotAcquireLockException("lock"));

        assertSequenceBusy(catchThrowable(() -> adapter.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE)));
    }

    @Test
    void EMT_3_unrelated_failures_are_not_translated() {
        IllegalStateException failure = new IllegalStateException("boom");
        when(repository.findForUpdate(ENTERPRISE_ID, "SALE_INVOICE")).thenThrow(failure);

        Throwable thrown = catchThrowable(() -> adapter.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE));

        assertThat(thrown).isSameAs(failure);
    }
}
