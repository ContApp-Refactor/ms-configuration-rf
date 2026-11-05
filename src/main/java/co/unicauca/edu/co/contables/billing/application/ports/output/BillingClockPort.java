package co.unicauca.edu.co.contables.billing.application.ports.output;

import java.time.Instant;
import java.time.LocalDate;

/** Reloj del modulo (permite fijar el tiempo en pruebas). */
public interface BillingClockPort {

    Instant now();

    LocalDate today();
}
