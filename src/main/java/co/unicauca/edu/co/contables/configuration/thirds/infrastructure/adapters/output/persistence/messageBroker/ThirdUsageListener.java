package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.IThirdUsagePort;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUsageEventDto;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.enums.EventUsageType;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.rabbitConfig.RabbitThirdUsedConfig;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @brief Listener para eventos de uso de productos desde PEPS
 *
 * Escucha eventos de RabbitMQ cuando PEPS notifica que ha utilizado un producto,
 * actualizando el contador de uso correspondiente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ThirdUsageListener extends AbstractMessageListener<EventDto<ThirdUsageEventDto, EventUsageType>> {

    private final IThirdUsagePort thirdUsagePort;

    /**
     * @brief Maneja eventos de uso de terceros desde la cola
     * @param event Evento con información del tercero usado
     */
    @RabbitListener(queues = RabbitThirdUsedConfig.THIRD_USED_QUEUE)
    public void handleThirdEvent(
            EventDto<ThirdUsageEventDto, EventUsageType> event,
            Message message, 
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        handleMessage(event, channel, deliveryTag);
    }

    /**
     * @brief Valida la integridad del evento recibido
     * @param event Evento a validar
     * @return true si el evento es válido
     */
    protected boolean isValidEvent(EventDto<ThirdUsageEventDto, EventUsageType> event) {
        if (event == null) {
            log.warn("Event is null");
            return false;
        }
        
        if (event.getData() == null) {
            log.warn("Event data is null");
            return false;
        }
        
        ThirdUsageEventDto data = event.getData();

        if (data.getThirdId() == null) {
            log.warn("ThirdId is null - required field");
            return false;
        }
        
        // if (data.getEnterpriseId() == null || data.getEnterpriseId().trim().isEmpty()) {
        //     log.warn("EnterpriseId is null or empty - required field");
        //     return false;
        // }
        
        if (data.getQuantityUsed() == null || data.getQuantityUsed() <= 0) {
            log.warn("QuantityUsed is null or invalid - required field");
            return false;
        }
        
        return true;
    }

    /**
     * @brief Procesa el evento de tercero basado en su tipo
     * @param event El evento de tercero a procesar
     */
    @Transactional
    @Override
    protected void processEvent(EventDto<ThirdUsageEventDto, EventUsageType> event) {
        try {
            switch (event.getType()) {
                case USED:
                
                    if (!isValidEvent(event)) {
                        log.warn("Invalid third usage event received");
                        return;
                    }

                    ThirdUsageEventDto data = event.getData();
                    log.info("Registrando uso de tercero ID: {}, cantidad: {}",
                             data.getThirdId(), data.getQuantityUsed());
                    thirdUsagePort.incrementUsageCount(data.getThirdId());
                    log.info("Uso registrado correctamente para tercero ID: {}", data.getThirdId());
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de evento no soportado: " + event.getType());
            }
        } catch (Exception e) {
            log.error("Error procesando evento de tercero: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    protected String getEntityType() {
        return "ThirdUsage";
    }
}

