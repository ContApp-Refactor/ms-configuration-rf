package co.unicauca.edu.co.contables.billing.application.ports.output.model;

/**
 * El broker no confirmo el evento (nack, timeout, devuelto sin ruta o error de comunicacion). No
 * es verificada: el relay la trata como intento fallido y reprograma con backoff.
 */
public class EventPublicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EventPublicationException(String message) {
        super(message);
    }

    public EventPublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
