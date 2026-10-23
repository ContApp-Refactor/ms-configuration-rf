package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.context;

import co.unicauca.edu.co.contables.billing.application.ports.output.BillingIdGeneratorPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Generador de UUID aleatorios. */
@Component("billingUuidGeneratorAdapter")
public class BillingUuidGeneratorAdapter implements BillingIdGeneratorPort {

    @Override
    public UUID newId() {
        return UUID.randomUUID();
    }
}
