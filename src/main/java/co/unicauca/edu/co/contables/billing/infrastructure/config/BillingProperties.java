package co.unicauca.edu.co.contables.billing.infrastructure.config;

import java.math.BigDecimal;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades del modulo billing. Todas tienen valor por defecto en codigo (sin cambios de yml). */
@Getter
@Setter
@ConfigurationProperties(prefix = "billing")
public class BillingProperties {

    /** IVA por defecto (porcentaje) usado como referencia de catalogo (H0-1). */
    private BigDecimal defaultVatRate = BigDecimal.valueOf(19);

    /** Zona horaria para la fecha "hoy" de los documentos. */
    private String timeZone = "America/Bogota";

    private Outbox outbox = new Outbox();

    @Getter
    @Setter
    public static class Outbox {
        private boolean relayEnabled = true;
        private long relayDelayMs = 2000L;
        private long relayInitialDelayMs = 10000L;
        private int batchSize = 50;
        private int maxAttempts = 10;
        private Duration baseBackoff = Duration.ofSeconds(5);
        private Duration maxBackoff = Duration.ofMinutes(10);
        private Duration confirmTimeout = Duration.ofSeconds(5);
    }
}
