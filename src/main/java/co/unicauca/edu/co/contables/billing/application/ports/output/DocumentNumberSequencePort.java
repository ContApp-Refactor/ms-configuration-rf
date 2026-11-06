package co.unicauca.edu.co.contables.billing.application.ports.output;

import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;

/**
 * Secuencia de numeracion por (tenant, empresa, tipo de documento) (EMT-3). Debe invocarse dentro de
 * la transaccion de emision: el bloqueo de la fila de secuencia se libera con el commit/rollback y
 * asi el numero es consecutivo y sin huecos.
 */
public interface DocumentNumberSequencePort {

    /**
     * Reserva y devuelve el siguiente consecutivo (empieza en 1). Bloquea la secuencia de forma
     * pesimista; si no la obtiene a tiempo lanza {@code BILLING_SEQUENCE_BUSY}.
     */
    long nextValue(String enterpriseId, DocumentType type);
}
