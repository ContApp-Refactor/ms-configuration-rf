package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.input.RelayBillingOutboxUseCase;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * EVT-4, RNF-05: el planificador solo dispara el relay y NUNCA deja escapar una excepcion (si lo
 * hiciera, el hilo unico del planificador de Spring podria quedar afectado). Los intervalos salen de
 * {@code billing.outbox.*} con los valores por defecto del diseno (2 s entre corridas, 10 s de espera
 * inicial). Se apaga con {@code billing.outbox.relay-enabled=false} y no existe en el perfil test.
 */
class BillingOutboxRelaySchedulerTest {

    private RelayBillingOutboxUseCase relay;
    private BillingOutboxRelayScheduler scheduler;

    @BeforeEach
    void setUp() {
        relay = mock(RelayBillingOutboxUseCase.class);
        scheduler = new BillingOutboxRelayScheduler(relay);
    }

    private static Method relayMethod() throws NoSuchMethodException {
        return BillingOutboxRelayScheduler.class.getMethod("relay");
    }

    @Test
    void EVT_4_each_tick_runs_the_relay_once() {
        when(relay.relayDue()).thenReturn(3);

        scheduler.relay();

        verify(relay).relayDue();
    }

    @Test
    void EVT_4_a_runtime_failure_is_swallowed() {
        when(relay.relayDue()).thenThrow(new IllegalStateException("db caida"));

        assertThatCode(scheduler::relay).doesNotThrowAnyException();
    }

    @Test
    void EVT_4_a_checked_looking_failure_wrapped_in_runtime_is_swallowed_too() {
        when(relay.relayDue()).thenThrow(new RuntimeException(new java.io.IOException("red")));

        assertThatCode(scheduler::relay).doesNotThrowAnyException();
    }

    @Test
    void EVT_4_a_failing_tick_does_not_prevent_the_next_one() {
        when(relay.relayDue()).thenThrow(new IllegalStateException("db caida")).thenReturn(1);

        scheduler.relay();
        scheduler.relay();

        verify(relay, times(2)).relayDue();
    }

    @Test
    void D13_delays_come_from_billing_outbox_properties_with_the_design_defaults() throws Exception {
        Scheduled scheduled = relayMethod().getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.fixedDelayString()).isEqualTo("${billing.outbox.relay-delay-ms:2000}");
        assertThat(scheduled.initialDelayString()).isEqualTo("${billing.outbox.relay-initial-delay-ms:10000}");
    }

    @Test
    void D13_is_disabled_by_property_enabled_by_default_and_absent_in_the_test_profile() {
        ConditionalOnProperty conditional = BillingOutboxRelayScheduler.class.getAnnotation(ConditionalOnProperty.class);
        Profile profile = BillingOutboxRelayScheduler.class.getAnnotation(Profile.class);
        Component component = BillingOutboxRelayScheduler.class.getAnnotation(Component.class);

        assertThat(conditional).isNotNull();
        assertThat(conditional.name()).containsExactly("billing.outbox.relay-enabled");
        assertThat(conditional.havingValue()).isEqualTo("true");
        assertThat(conditional.matchIfMissing()).isTrue();
        assertThat(profile.value()).containsExactly("!test");
        assertThat(component.value()).isEqualTo("billingOutboxRelayScheduler");
    }
}
