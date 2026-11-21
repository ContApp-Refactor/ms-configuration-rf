package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAccountingResultPublisher.AccountingResultReply;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryRabbitDtos.AccountingResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpringEventsTreasuryAccountingResultPublisher")
class SpringEventsTreasuryAccountingResultPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsTreasuryAccountingResultPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsTreasuryAccountingResultPublisher(eventPublisher);
    }

    @Test
    @DisplayName("publica el resultado aceptado con todos los campos mapeados")
    void publicaResultadoAceptado() {
        publisher.publish(new AccountingResultReply(
                "evt-src", "PAYMENT_VOUCHER.CREATE.RESULT", "tenant-a", "ENT-1", "corr-1",
                "CREATE", "PAYMENT_VOUCHER", 42L, true, 99L, null));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(AccountingResult.class);
        AccountingResult payload = (AccountingResult) captor.getValue();
        assertThat(payload.eventId()).isNotBlank();
        assertThat(payload.sourceEventId()).isEqualTo("evt-src");
        assertThat(payload.operation()).isEqualTo("CREATE");
        assertThat(payload.documentType()).isEqualTo("PAYMENT_VOUCHER");
        assertThat(payload.documentId()).isEqualTo(42L);
        assertThat(payload.accepted()).isTrue();
        assertThat(payload.accountingEntryId()).isEqualTo(99L);
        assertThat(payload.reason()).isNull();
        assertThat(payload.tenantId()).isEqualTo("tenant-a");
        assertThat(payload.enterpriseId()).isEqualTo("ENT-1");
    }

    @Test
    @DisplayName("publica el rechazo con el motivo y sin asiento")
    void publicaRechazo() {
        publisher.publish(new AccountingResultReply(
                "evt-src-2", "PAYABLE_WRITEOFF.VOID.RESULT", "tenant-b", "ENT-2", "corr-2",
                "VOID", "PAYABLE_WRITEOFF", 7L, false, null, "documento no encontrado"));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        AccountingResult payload = (AccountingResult) captor.getValue();
        assertThat(payload.sourceEventId()).isEqualTo("evt-src-2");
        assertThat(payload.operation()).isEqualTo("VOID");
        assertThat(payload.documentType()).isEqualTo("PAYABLE_WRITEOFF");
        assertThat(payload.documentId()).isEqualTo(7L);
        assertThat(payload.accepted()).isFalse();
        assertThat(payload.accountingEntryId()).isNull();
        assertThat(payload.reason()).isEqualTo("documento no encontrado");
        assertThat(payload.tenantId()).isEqualTo("tenant-b");
        assertThat(payload.enterpriseId()).isEqualTo("ENT-2");
    }
}
