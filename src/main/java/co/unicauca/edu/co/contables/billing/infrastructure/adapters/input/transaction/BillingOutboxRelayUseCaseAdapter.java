package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.transaction;

import co.unicauca.edu.co.contables.billing.application.ports.input.RelayBillingOutboxUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingEventPublisherPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingOutboxRelayPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingTransactionRunnerPort;
import co.unicauca.edu.co.contables.billing.application.service.BillingOutboxRelayService;
import co.unicauca.edu.co.contables.billing.application.service.OutboxRetryPolicy;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada del relay: construye {@link BillingOutboxRelayService} con las propiedades de
 * {@code billing.outbox} y delega. Deliberadamente NO es {@code @Transactional} (D13): el reclamo se
 * hace sin tenant y cada fila abre su propia transaccion REQUIRES_NEW DESPUES de fijar su tenant; una
 * transaccion externa dejaria una sesion Hibernate abierta con el tenant equivocado.
 *
 * <p>Excluido del perfil {@code test}: depende del publicador, que necesita el {@code rabbitTemplate}
 * (tambien excluido de ese perfil).
 */
@Component("billingOutboxRelayUseCaseAdapter")
@Profile("!test")
public class BillingOutboxRelayUseCaseAdapter implements RelayBillingOutboxUseCase {

    private final BillingOutboxRelayService relayService;

    public BillingOutboxRelayUseCaseAdapter(BillingOutboxRelayPort relayPort, BillingEventPublisherPort publisher,
            BillingExecutionContextPort context, BillingTransactionRunnerPort transactions, BillingClockPort clock,
            BillingProperties properties) {
        BillingProperties.Outbox outbox = properties.getOutbox();
        OutboxRetryPolicy policy = new OutboxRetryPolicy(outbox.getMaxAttempts(), outbox.getBaseBackoff(),
                outbox.getMaxBackoff());
        this.relayService = new BillingOutboxRelayService(relayPort, publisher, context, transactions, clock, policy,
                outbox.getBatchSize());
    }

    @Override
    public int relayDue() {
        return relayService.relayDue();
    }
}
