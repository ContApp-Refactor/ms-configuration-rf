package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.transaction;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_A;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.input.RelayBillingOutboxUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingEventPublisherPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingOutboxRelayPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingTransactionRunnerPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.EventPublicationException;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.OutboxClaim;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.PendingOutboxMessage;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingProperties;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * D13: el adaptador de entrada del relay arma el servicio con las propiedades de {@code billing.outbox}
 * y NO es transaccional (cada fila abre su propia transaccion REQUIRES_NEW dentro del servicio; una
 * transaccion externa mantendria una sesion Hibernate abierta con el tenant equivocado).
 */
class BillingOutboxRelayUseCaseAdapterTest {

    private BillingOutboxRelayPort relayPort;
    private BillingEventPublisherPort publisher;
    private BillingExecutionContextPort context;
    private BillingTransactionRunnerPort transactions;
    private BillingClockPort clock;
    private BillingProperties properties;

    @BeforeEach
    void setUp() {
        relayPort = mock(BillingOutboxRelayPort.class);
        publisher = mock(BillingEventPublisherPort.class);
        context = mock(BillingExecutionContextPort.class);
        transactions = mock(BillingTransactionRunnerPort.class);
        clock = mock(BillingClockPort.class);
        properties = new BillingProperties();
        when(clock.now()).thenReturn(NOW);
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(1)).run();
            return null;
        }).when(context).runAsTenant(anyString(), any(Runnable.class));
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(transactions).inNewTransaction(any(Runnable.class));
    }

    private BillingOutboxRelayUseCaseAdapter adapter() {
        return new BillingOutboxRelayUseCaseAdapter(relayPort, publisher, context, transactions, clock, properties);
    }

    private static PendingOutboxMessage message(int attempts) {
        return new PendingOutboxMessage(1L, "ev-1", "FacturaVentaEmitida", TENANT_A, "billing.documents.exchange",
                "sale-invoice.emitted.v1", "{}", attempts, NOW);
    }

    @Test
    void EVT_4_delegates_to_the_relay_service_and_returns_the_published_count() {
        when(relayPort.findDueClaims(NOW, 50)).thenReturn(List.of(new OutboxClaim(1L, TENANT_A)));
        when(relayPort.lockPending(1L)).thenReturn(Optional.of(message(0)));

        int published = adapter().relayDue();

        assertThat(published).isEqualTo(1);
        verify(publisher).publish(message(0));
        verify(relayPort).markPublished(1L, NOW);
    }

    @Test
    void EVT_4_the_configured_batch_size_is_used() {
        properties.getOutbox().setBatchSize(7);
        when(relayPort.findDueClaims(NOW, 7)).thenReturn(List.of());

        adapter().relayDue();

        verify(relayPort).findDueClaims(NOW, 7);
    }

    @Test
    void EVT_4_the_configured_max_attempts_decide_when_a_failure_becomes_definitive() {
        properties.getOutbox().setMaxAttempts(1);
        when(relayPort.findDueClaims(NOW, 50)).thenReturn(List.of(new OutboxClaim(1L, TENANT_A)));
        when(relayPort.lockPending(1L)).thenReturn(Optional.of(message(0)));
        doThrow(new EventPublicationException("nack")).when(publisher).publish(message(0));

        adapter().relayDue();

        verify(relayPort).markAttemptFailed(eq(1L), eq(1), any(), eq(true), eq("nack"));
    }

    @Test
    void D13_the_adapter_is_not_transactional_at_class_or_method_level() {
        assertThat(BillingOutboxRelayUseCaseAdapter.class.isAnnotationPresent(Transactional.class)).isFalse();
        List<Method> annotated = Arrays.stream(BillingOutboxRelayUseCaseAdapter.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Transactional.class))
                .toList();
        assertThat(annotated).isEmpty();
    }

    @Test
    void D13_is_a_billing_component_excluded_from_the_test_profile() {
        Component component = BillingOutboxRelayUseCaseAdapter.class.getAnnotation(Component.class);
        Profile profile = BillingOutboxRelayUseCaseAdapter.class.getAnnotation(Profile.class);

        assertThat(component.value()).isEqualTo("billingOutboxRelayUseCaseAdapter");
        assertThat(profile.value()).containsExactly("!test");
        assertThat(RelayBillingOutboxUseCase.class).isAssignableFrom(BillingOutboxRelayUseCaseAdapter.class);
    }
}
