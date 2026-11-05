package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.context;

import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Reloj del sistema con zona horaria configurable (billing.time-zone). */
@Component("billingClockAdapter")
public class BillingClockAdapter implements BillingClockPort {

    private final ZoneId zone;

    public BillingClockAdapter(@Value("${billing.time-zone:America/Bogota}") String zoneId) {
        this.zone = ZoneId.of(zoneId);
    }

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public LocalDate today() {
        return LocalDate.now(zone);
    }
}
