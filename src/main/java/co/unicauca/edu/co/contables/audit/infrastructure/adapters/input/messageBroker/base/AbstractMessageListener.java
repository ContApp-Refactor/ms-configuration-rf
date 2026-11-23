package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.base;

import java.util.Optional;

import org.springframework.amqp.core.Message;

import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * Clase base abstracta para todos los message listeners de RabbitMQ.
 * Proporciona funcionalidad común para el manejo de mensajes sin lógica de DLQ.
 * 
 * @param <T> Tipo del evento/mensaje a procesar
 */
@Slf4j
public abstract class AbstractMessageListener<T> {

    protected final ObjectMapper objectMapper;

    /**
     * Puerto para el manejo de errores de procesamiento.
     * Debe ser inyectado por las clases hijas.
     */
    protected IMessageErrorHandlingPort messageErrorHandlingPort;

    protected AbstractMessageListener(
            IMessageErrorHandlingPort messageErrorHandlingPort,
            ObjectMapper objectMapper) {
        this.messageErrorHandlingPort = messageErrorHandlingPort;
        this.objectMapper = objectMapper;
    }

    /**
     * Método principal para manejar mensajes entrantes.
     * Implementa la lógica común de validación, procesamiento y acknowledgment.
     */
    protected void handleMessage(T event, Channel channel, long deliveryTag) {
        try {
            log.info("Received {} message from queue", getEntityType());
            Optional<String> validationError = validateEvent(event);
            if (validationError.isPresent()) {
                log.warn("Invalid {} event: {}", getEntityType(), validationError.get());
                handleValidationError(event, validationError.get());
                acknowledgeMessage(channel, deliveryTag);
                return;
            }
            processEvent(event);
            acknowledgeMessage(channel, deliveryTag);
            log.info("{} message processed successfully", getEntityType());
        } catch (Exception e) {
            handleProcessingError(e, event, channel, deliveryTag);
        }
    }

    /**
     * Procesa el evento específico. Debe ser implementado por cada listener.
     */
    protected abstract void processEvent(T event);

    /**
     * Valida si el evento es válido para procesamiento.
     */
    protected abstract Optional<String> validateEvent(T event);

    /**
     * Retorna el tipo de entidad que maneja este listener (para logging).
     */
    protected abstract String getEntityType();

    /**
     * Maneja errores durante el procesamiento del mensaje.
     */
    private void handleProcessingError(Exception e, T event, Channel channel, long deliveryTag) {
        try {
            log.error("Error processing {} message: {}", getEntityType(), e.getMessage(), e);

            // Guardar error en base de datos
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String errorDescription = buildErrorDescription(e);
                String errorStage = resolveErrorStage(e);
                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, getEntityType(),
                        errorStage);
            }

            acknowledgeMessage(channel, deliveryTag); // ACK para evitar reenvío
        } catch (Exception ackException) {
            log.error("Error acknowledging message: {}", ackException.getMessage());
        }
    }

    private String buildErrorDescription(Exception e) {
        StringBuilder sb = new StringBuilder();

        // Tipo de excepción
        sb.append("[").append(e.getClass().getSimpleName()).append("] ");

        // Mensaje principal
        sb.append(e.getMessage() != null ? e.getMessage() : "No message");

        // Causa raíz si existe y es diferente
        Throwable cause = getRootCause(e);
        if (cause != null && cause != e) {
            sb.append(" | Caused by: [")
                    .append(cause.getClass().getSimpleName())
                    .append("] ")
                    .append(cause.getMessage() != null ? cause.getMessage() : "No message");
        }

        // Primer frame relevante del stack (filtrando frameworks)
        StackTraceElement[] stack = e.getStackTrace();
        for (StackTraceElement frame : stack) {
            if (frame.getClassName().startsWith("co.unicauca.edu.co.contables.audit")) { // ajusta tu paquete base
                sb.append(" | at ").append(frame.getClassName())
                        .append(".").append(frame.getMethodName())
                        .append(":").append(frame.getLineNumber());
                break;
            }
        }

        return sb.toString();
    }

    private Throwable getRootCause(Throwable t) {
        Throwable cause = t.getCause();
        while (cause != null && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private String resolveErrorStage(Exception e) {
        String exName = e.getClass().getSimpleName();
        if (e instanceof RuntimeException || exName.contains("Validation"))
            return "VALIDATION";
        if (exName.contains("Mapping") || exName.contains("JsonMapping")
                || exName.contains("JsonParse") || e instanceof IllegalArgumentException)
            return "MAPPING";
        if (exName.contains("DataAccess") || exName.contains("Persistence")
                || exName.contains("Jpa") || exName.contains("Sql"))
            return "PERSISTENCE";
        return "UNKNOWN";
    }

    /**
     * Maneja errores de validación de eventos.
     */
    private void handleValidationError(T event, String reason) {
        if (messageErrorHandlingPort != null) {
            messageErrorHandlingPort.saveProcessingError(
                    extractEventType(event),
                    "Validation failed: " + reason,
                    convertEventToJson(event),
                    getEntityType(),
                    "VALIDATION");
        }
    }

    /**
     * Envía acknowledgment del mensaje.
     */
    private void acknowledgeMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to acknowledge message: {}", e.getMessage());
        }
    }

    /**
     * Método de utilidad para extraer contenido del mensaje como String.
     */
    protected String getMessageBodyAsString(Message message) {
        try {
            return new String(message.getBody());
        } catch (Exception e) {
            log.warn("Error converting message body to string: {}", e.getMessage());
            return "unavailable";
        }
    }

    /**
     * Extrae el tipo de evento del mensaje. Debe ser implementado por cada
     * listener.
     * 
     * @param event El evento del cual extraer el tipo
     * @return String representando el tipo de evento, o null si no se puede
     *         determinar
     */
    protected abstract String extractEventType(T event);

    /**
     * Convierte el evento a JSON para almacenamiento en BD. Debe ser implementado
     * por cada listener.
     * 
     * @param event El evento a convertir
     * @return String en formato JSON con los datos del evento
     */
    protected abstract String convertEventToJson(T event);

    /**
     * Obtiene el mensaje de error específico de validación si está disponible.
     * 
     * @return String con el mensaje de error específico, o null si no hay mensaje
     *         específico
     */
    protected String getValidationErrorMessage() {
        return null; // Implementación por defecto
    }

    protected boolean isValidJsonSize(Object data, int maxBytes) {
        try {
            String json = objectMapper.writeValueAsString(data);
            return json.length() <= maxBytes;
        } catch (Exception e) {
            return false;
        }
    }
}
