package co.unicauca.edu.co.contables.billing.application.ports.output;

import co.unicauca.edu.co.contables.billing.application.ports.output.model.EventPublicationException;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.PendingOutboxMessage;

/** Publica un mensaje del outbox en el broker y espera su confirmacion. */
public interface BillingEventPublisherPort {

    /**
     * Envia el mensaje y bloquea hasta la confirmacion del broker.
     *
     * @throws EventPublicationException si el broker rechaza, no confirma a tiempo, devuelve el
     *         mensaje sin ruta o hay un error de comunicacion
     */
    void publish(PendingOutboxMessage message) throws EventPublicationException;
}
