package co.unicauca.edu.co.contables.billing.application.ports.output;

import co.unicauca.edu.co.contables.billing.domain.event.BillingDomainEvent;

/**
 * Bandeja de salida transaccional (EVT-1): agrega el evento en la MISMA transaccion del documento;
 * nada se publica en el hilo de la peticion (lo hace el relay en una tanda posterior).
 */
public interface BillingOutboxPort {

    void append(BillingDomainEvent event);
}
