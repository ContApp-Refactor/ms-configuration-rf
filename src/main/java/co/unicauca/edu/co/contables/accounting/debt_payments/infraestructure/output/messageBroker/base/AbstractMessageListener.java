package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.base;

import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.ValidationException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase base abstracta para todos los message listeners de RabbitMQ.
 * Proporciona funcionalidad común para el manejo de mensajes sin lógica de DLQ.
 * 
 * @param <T> Tipo del evento/mensaje a procesar
 */
@Slf4j
public abstract class AbstractMessageListener<T> {

    /**
     * Puerto para el manejo de errores de procesamiento.
     * Debe ser inyectado por las clases hijas.
     */
    protected IMessageErrorHandlingPort messageErrorHandlingPort;

    /**
     * Puerto para ejecutar acciones de recuperación cuando falla el procesamiento.
     * Opcional - puede ser inyectado por las clases hijas si necesitan recuperación
     * automática.
     */
    protected IEventRecoveryActionPort<T> eventRecoveryActionPort;

    /**
     * Método principal para manejar mensajes entrantes.
     * Implementa la lógica común de validación, procesamiento y acknowledgment.
     */
    protected void handleMessage(T event, Channel channel, long deliveryTag) {
        try {
            log.info("Received {} message from queue", getEntityType());

            validateEvent(event);

            processEvent(event);
            acknowledgeMessage(channel, deliveryTag);
            log.info("{} message processed successfully", getEntityType());

        } catch (ValidationException ve) { 
            log.warn("Invalid {} event received: {}. Saving error to database", getEntityType(), ve.getMessage());
            handleValidationError(event, ve); 
            acknowledgeMessage(channel, deliveryTag);

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
    protected abstract void validateEvent(T event) throws ValidationException;

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

            // Intentar ejecutar acción de recuperación si está disponible
            boolean recoveryExecuted = attemptRecovery(event);

            // Guardar error en base de datos
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String errorDescription = String.format("Processing error: %s%s",
                        e.getMessage(),
                        recoveryExecuted ? " (Recovery action executed)" : "");

                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, getEntityType());
            }

            acknowledgeMessage(channel, deliveryTag); // ACK para evitar reenvío
        } catch (Exception ackException) {
            log.error("Error acknowledging message: {}", ackException.getMessage());
        }
    }

    /**
     * Maneja errores de validación de eventos.
     */
    private void handleValidationError(T event, ValidationException e) {
        try {
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String errorDescription = e.getMessage();

                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, getEntityType());
            }
        } catch (Exception ex) {
            log.error("Error saving validation error to database: {}", ex.getMessage());
        }
    }

    /**
     * Intenta ejecutar una acción de recuperación cuando falla el procesamiento del
     * evento.
     * 
     * @param event El evento que falló al procesarse
     * @return true si se ejecutó una acción de recuperación, false en caso
     *         contrario
     */
    private boolean attemptRecovery(T event) {
        if (eventRecoveryActionPort == null) {
            log.debug("No recovery action port configured for {}", getEntityType());
            return false;
        }

        try {
            if (eventRecoveryActionPort.canHandle(event)) {
                log.info("Attempting recovery action for failed {} event", getEntityType());
                boolean success = eventRecoveryActionPort.executeRecoveryAction(event);

                if (success) {
                    log.info("Recovery action executed successfully for {} event", getEntityType());
                } else {
                    log.warn("Recovery action failed for {} event", getEntityType());
                }

                return success;
            } else {
                log.debug("Recovery action cannot handle this {} event", getEntityType());
                return false;
            }
        } catch (Exception recoveryException) {
            log.error("Error executing recovery action for {} event: {}",
                    getEntityType(), recoveryException.getMessage(), recoveryException);
            return false;
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
}
