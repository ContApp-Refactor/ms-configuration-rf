package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.context;

import co.unicauca.edu.co.contables.billing.application.ports.output.VerificationCodeGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Codigo de verificacion provisional: UUID v4 en texto. */
@Component("billingUuidVerificationCodeGenerator")
public class BillingUuidVerificationCodeGenerator implements VerificationCodeGenerator {

    @Override
    public String next() {
        return UUID.randomUUID().toString();
    }
}
