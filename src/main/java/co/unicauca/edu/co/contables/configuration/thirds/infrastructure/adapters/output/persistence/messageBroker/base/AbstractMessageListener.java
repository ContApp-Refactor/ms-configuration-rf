package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.base;


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

    /**
     * Método principal para manejar mensajes entrantes.
     * Implementa la lógica común de validación, procesamiento y acknowledgment.
     */
    protected void handleMessage(T event, Channel channel, long deliveryTag) {
        try {
            log.info("Received {} message from queue", getEntityType());
            
            if (!isValidEvent(event)) {
                log.warn("Invalid {} event received, saving error to database", getEntityType());
                return;
            }
            
            processEvent(event);
            acknowledgeMessage(channel, deliveryTag);
            log.info("{} message processed successfully", getEntityType());
            
        } catch (Exception e) {
            acknowledgeMessage(channel, deliveryTag);
            log.error("Error :c ");
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
     * Procesa el evento específico. Debe ser implementado por cada listener.
     */
    protected abstract void processEvent(T event);

    /**
     * Valida si el evento es válido para procesamiento.
     */
    protected abstract boolean isValidEvent(T event);

    /**
     * Retorna el tipo de entidad que maneja este listener (para logging).
     */
    protected abstract String getEntityType();

}
