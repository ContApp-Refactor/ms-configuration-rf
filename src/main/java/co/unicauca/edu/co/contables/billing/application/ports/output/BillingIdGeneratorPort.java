package co.unicauca.edu.co.contables.billing.application.ports.output;

import java.util.UUID;

/** Generador de identificadores de documentos y eventos. */
public interface BillingIdGeneratorPort {

    UUID newId();
}
