package co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.config.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.config.RabbitTaxUsedConfig;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.dto.TaxUsageDto;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.enums.EventUsageType;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxUsageInputPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Listener para eventos de uso de impuestos
 *
 * Escucha eventos de RabbitMQ cuando se notifica el uso de un impuesto,
 * actualizando el contador de uso correspondiente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaxUsageListener extends AbstractMessageListener<EventDto<TaxUsageDto, EventUsageType>> {

    private final ITaxUsageInputPort taxUsageInputPort;

    /**
     * @brief Maneja eventos de uso de impuestos desde la cola
     * @param event Evento con información del impuesto usado
     * @param message El mensaje RabbitMQ raw
     * @param channel El canal RabbitMQ
     * @param deliveryTag El tag de entrega del mensaje
     */
    @RabbitListener(queues = RabbitTaxUsedConfig.TAX_USED_QUEUE)
    public void handleTaxEvent(
        EventDto<TaxUsageDto, EventUsageType> event,
        Message message,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        handleMessage(event, channel, deliveryTag);
    }

    /**
     * @brief Procesa el evento de impuesto basado en su tipo
     * @param event El evento de impuesto a procesar
     */
    @Override
    protected void processEvent(EventDto<TaxUsageDto, EventUsageType> event) {
        try {
            switch (event.getType()) {
                case USED:

                    if (!isValidEvent(event)) {
                        log.warn("Invalid tax usage event received");
                        return;
                    }

                    TaxUsageDto data = event.getData();
                    log.info("Registrando uso de impuesto ID: {}, empresa: {}, cantidad: {}",
                             data.getTaxId(), data.getEnterpriseId(), data.getQuantityUsed());
                    taxUsageInputPort.incrementUsageCount(data.getTaxId(), data.getEnterpriseId());
                    log.info("Uso registrado correctamente para impuesto ID: {}", data.getTaxId());
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de evento no soportado: " + event.getType());
            }
        } catch (Exception e) {
            log.error("Error procesando evento de impuesto: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * @brief Valida la integridad de los datos del evento de impuesto
     * @param event El evento de impuesto a validar
     * @return True si el evento es válido, false en caso contrario
     */
    @Override
    protected boolean isValidEvent(EventDto<TaxUsageDto, EventUsageType> event) {
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

        TaxUsageDto data = event.getData();

        if (data.getTaxId() == null) {
            log.warn("TaxId es null - campo obligatorio");
            return false;
        }

        if (data.getEnterpriseId() == null || data.getEnterpriseId().trim().isEmpty()) {
            log.warn("EnterpriseId es null o vacío - campo obligatorio");
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
        return "Tax";
    }

    @Override
    protected String extractEventType(EventDto<TaxUsageDto, EventUsageType> event) {
        // Este método se requiere por la interfaz AbstractMessageListener
        // pero no se utiliza en el contexto de TaxUsageListener
        // ya que no se implementa manejo de errores en base de datos
        return null;
    }

    @Override
    protected String convertEventToJson(EventDto<TaxUsageDto, EventUsageType> event) {
        // Este método se requiere por la interfaz AbstractMessageListener
        // pero no se utiliza en el contexto de TaxUsageListener
        // ya que no se implementa manejo de errores en base de datos
        return "{}";
    }
}
