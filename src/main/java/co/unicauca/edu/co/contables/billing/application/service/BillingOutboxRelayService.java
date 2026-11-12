package co.unicauca.edu.co.contables.billing.application.service;

import co.unicauca.edu.co.contables.billing.application.ports.input.RelayBillingOutboxUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingEventPublisherPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingOutboxRelayPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingTransactionRunnerPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.OutboxClaim;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.PendingOutboxMessage;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Relay del outbox (EVT-4, RNF-05). Java plano: NO es transaccional por si mismo, cada fila abre
 * su propia transaccion nueva.
 *
 * <p>Flujo: reclamar filas vencidas (consulta entre tenants, sin tenant en el contexto) y, por cada
 * una, {@code runAsTenant(tenant de la fila)} -> transaccion nueva -> bloquear la fila (sigue
 * PENDING y vencida) -> publicar y esperar confirmacion -> marcar PUBLISHED, o registrar el intento
 * fallido con backoff / FAILED definitivo. El tenant se fija ANTES de abrir la transaccion porque el
 * resolvedor de Hibernate valida las sesiones existentes. Entrega al menos una vez: si el proceso cae
 * tras el envio y antes del commit, la fila se reenvia.
 *
 * <p>Riesgo conocido: corre en el unico hilo del planificador de Spring, que comparte con otros
 * ejecutores; cada publicacion puede esperar hasta el timeout de confirmacion (5 s). Para no
 * monopolizarlo con un broker caido, el lote se corta tras {@value #MAX_CONSECUTIVE_FAILURES} fallos
 * consecutivos; las filas no intentadas siguen PENDING y vencidas para la proxima corrida.
 */
public class BillingOutboxRelayService implements RelayBillingOutboxUseCase {

    /** Fallos seguidos tras los cuales se interrumpe el lote (el broker probablemente esta caido). */
    static final int MAX_CONSECUTIVE_FAILURES = 3;

    private static final System.Logger LOG = System.getLogger(BillingOutboxRelayService.class.getName());

    private enum Outcome { PUBLISHED, FAILED, SKIPPED }

    private final BillingOutboxRelayPort relayPort;
    private final BillingEventPublisherPort publisher;
    private final BillingExecutionContextPort context;
    private final BillingTransactionRunnerPort transactions;
    private final BillingClockPort clock;
    private final OutboxRetryPolicy retryPolicy;
    private final int batchSize;

    public BillingOutboxRelayService(BillingOutboxRelayPort relayPort, BillingEventPublisherPort publisher,
            BillingExecutionContextPort context, BillingTransactionRunnerPort transactions, BillingClockPort clock,
            OutboxRetryPolicy retryPolicy, int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize debe ser >= 1");
        }
        this.relayPort = Objects.requireNonNull(relayPort, "relayPort");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.context = Objects.requireNonNull(context, "context");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
        this.batchSize = batchSize;
    }

    @Override
    public int relayDue() {
        List<OutboxClaim> claims = relayPort.findDueClaims(clock.now(), batchSize);
        int published = 0;
        int consecutiveFailures = 0;
        for (OutboxClaim claim : claims) {
            Outcome outcome = relayClaim(claim);
            if (outcome == Outcome.PUBLISHED) {
                published++;
                consecutiveFailures = 0;
            } else if (outcome == Outcome.FAILED) {
                consecutiveFailures++;
                if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    LOG.log(System.Logger.Level.WARNING,
                            "Relay de facturacion interrumpido tras {0} fallos consecutivos; "
                                    + "las filas restantes se reintentan en la proxima corrida",
                            consecutiveFailures);
                    break;
                }
            }
        }
        return published;
    }

    /** Aisla el fallo de una fila: nada de lo que ocurra aqui impide procesar las siguientes. */
    private Outcome relayClaim(OutboxClaim claim) {
        Outcome[] result = {Outcome.SKIPPED};
        try {
            context.runAsTenant(claim.tenantId(),
                    () -> transactions.inNewTransaction(() -> result[0] = relayOne(claim.id())));
        } catch (RuntimeException ex) {
            LOG.log(System.Logger.Level.ERROR,
                    "Fallo procesando la fila " + claim.id() + " del outbox de facturacion (tenant "
                            + claim.tenantId() + "); se reintentara",
                    ex);
            return Outcome.FAILED;
        }
        return result[0];
    }

    /** Se ejecuta dentro del tenant de la fila y de una transaccion nueva. */
    private Outcome relayOne(long id) {
        Optional<PendingOutboxMessage> locked = relayPort.lockPending(id);
        if (locked.isEmpty()) {
            return Outcome.SKIPPED;
        }
        PendingOutboxMessage message = locked.get();
        try {
            publisher.publish(message);
        } catch (RuntimeException ex) {
            int attempts = message.attempts() + 1;
            OutboxRetryPolicy.Decision decision = retryPolicy.decide(attempts, clock.now());
            relayPort.markAttemptFailed(id, attempts, decision.nextAttemptAt(), decision.exhausted(),
                    describe(ex));
            LOG.log(decision.exhausted() ? System.Logger.Level.ERROR : System.Logger.Level.WARNING,
                    "Evento {0} ({1}) no publicado, intento {2}{3}: {4}", message.eventId(), message.eventType(),
                    attempts, decision.exhausted() ? " (agotado: FAILED definitivo)" : "", describe(ex));
            return Outcome.FAILED;
        }
        relayPort.markPublished(id, clock.now());
        return Outcome.PUBLISHED;
    }

    private static String describe(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getName() : message;
    }
}
