package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.scheduler;

import co.unicauca.edu.co.contables.billing.application.ports.input.RelayBillingOutboxUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara el relay del outbox de facturacion cada {@code billing.outbox.relay-delay-ms} (2 s) tras
 * una espera inicial de {@code billing.outbox.relay-initial-delay-ms} (10 s), para dar tiempo a que
 * arranque el contexto y se declare la topologia. {@code @EnableScheduling} ya esta activo en
 * {@code ConfigurationApplication}. Se apaga con {@code billing.outbox.relay-enabled=false} y no
 * existe en el perfil {@code test}.
 *
 * <p>Riesgo conocido: Spring usa por defecto UN solo hilo para todos los {@code @Scheduled} del
 * monolito (incluidos los de tesoreria). Una corrida del relay puede bloquearlo hasta
 * (fallos consecutivos tolerados x timeout de confirmacion) = 3 x 5 s en el peor caso; el lote se
 * corta tras 3 fallos seguidos para acotarlo. Si el relay empezara a retrasar otros ejecutores,
 * habilitar un {@code TaskScheduler} con mas hilos (configuracion global, fuera de este modulo).
 *
 * <p>Atrapa y registra cualquier excepcion: una excepcion no capturada no debe afectar al hilo del
 * planificador.
 */
@Slf4j
@Component("billingOutboxRelayScheduler")
@Profile("!test")
@ConditionalOnProperty(name = "billing.outbox.relay-enabled", havingValue = "true", matchIfMissing = true)
public class BillingOutboxRelayScheduler {

    private final RelayBillingOutboxUseCase relay;

    public BillingOutboxRelayScheduler(RelayBillingOutboxUseCase relay) {
        this.relay = relay;
    }

    @Scheduled(fixedDelayString = "${billing.outbox.relay-delay-ms:2000}",
            initialDelayString = "${billing.outbox.relay-initial-delay-ms:10000}")
    public void relay() {
        try {
            int published = relay.relayDue();
            if (published > 0) {
                log.debug("Relay de facturacion: {} evento(s) publicado(s)", published);
            }
        } catch (Exception ex) {
            log.error("Fallo el relay del outbox de facturacion; se reintenta en la proxima corrida", ex);
        }
    }
}
