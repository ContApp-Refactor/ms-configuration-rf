package co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import co.unicauca.edu.co.contables.configuration.commons.config.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.config.RabbitCostCenterUsedConfig;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.dto.CostCenterUsageDto;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.enums.EventUsageType;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.IUsageCostCenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Listener para eventos de uso de centros de costo
 *
 * Escucha eventos de RabbitMQ cuando se notifica el uso de un centro de costo,
 * actualizando el contador de uso correspondiente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CostCenterUsageListener extends AbstractMessageListener<EventDto<CostCenterUsageDto, EventUsageType>> {

    private final IUsageCostCenter usageCostCenter;
    
    /**
     * @brief Maneja eventos de uso de centros de costo desde la cola
     * @param event Evento con información del centro de costo usado
     * @param message El mensaje RabbitMQ raw
     * @param channel El canal RabbitMQ
     * @param deliveryTag El tag de entrega del mensaje
     */
    @RabbitListener(queues = RabbitCostCenterUsedConfig.COST_CENTER_USED_QUEUE)
    public void handleCostCenterEvent(
        EventDto<CostCenterUsageDto, EventUsageType> event,
        Message message,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        handleMessage(event, channel, deliveryTag);
    }

    /**
     * @brief Procesa el evento de centro de costo basado en su tipo
     * @param event El evento de centro de costo a procesar
     */
    @Override
    protected void processEvent(EventDto<CostCenterUsageDto, EventUsageType> event) {
        try {
            switch (event.getType()) {
                case USED:
                
                    if (!isValidEvent(event)) {
                        log.warn("Invalid cost center usage event received");
                        return;
                    }

                    CostCenterUsageDto data = event.getData();
                    log.info("Registrando uso de centro de costo ID: {}, cantidad: {}",
                             data.getCostCenterId(), data.getQuantityUsed());
                    usageCostCenter.incrementUsageCount(data.getCostCenterId());
                    log.info("Uso registrado correctamente para centro de costo ID: {}", data.getCostCenterId());
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de evento no soportado: " + event.getType());
            }
        } catch (Exception e) {
            log.error("Error procesando evento de centro de costo: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * @brief Valida la integridad de los datos del evento de centro de costo
     * @param event El evento de centro de costo a validar
     * @return True si el evento es válido, false en caso contrario
     */
    @Override
    protected boolean isValidEvent(EventDto<CostCenterUsageDto, EventUsageType> event) {
        if (event == null) {
            log.warn("Evento es null");
            return false;
        }

        if (event.getType() == null) {
            log.warn("Tipo de evento es null");
            return false;
        }

        if (event.getData() == null) {
            log.warn("Datos del evento son null");
            return false;
        }

        CostCenterUsageDto data = event.getData();

        if (data.getCostCenterId() == null) {
            log.warn("CostCenterId es null - campo obligatorio");
            return false;
        }

        if (data.getQuantityUsed() == null || data.getQuantityUsed() <= 0) {
            log.warn("QuantityUsed es null o inválido - campo obligatorio");
            return false;
        }

        return true;
    }

    @Override
    protected String getEntityType() {
        return "CostCenter";
    }

    @Override
    protected String extractEventType(EventDto<CostCenterUsageDto, EventUsageType> event) {
        if (event == null || event.getType() == null) {
            return null;
        }
        return event.getType().toString();
    }

}
