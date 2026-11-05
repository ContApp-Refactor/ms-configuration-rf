package co.unicauca.edu.co.contables.notifications.domain.exception;

public class MessageProcessingErrorNotFoundException extends RuntimeException {
    public MessageProcessingErrorNotFoundException(String message) {
        super(message);
    }
}