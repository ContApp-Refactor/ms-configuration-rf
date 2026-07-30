package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import co.unicauca.edu.co.contables.configuration.commons.config.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.config.RabbitDocumentTypeUsedConfig;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.dto.DocumentTypeUsageDto;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.enums.EventUsageType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeUsage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Listener para eventos de uso de tipos de documento
 *
 * Escucha eventos de RabbitMQ cuando se notifica el uso de un tipo de documento,
 * actualizando el contador de uso correspondiente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentTypeUsageListener extends AbstractMessageListener<EventDto<DocumentTypeUsageDto, EventUsageType>> {

    private final IDocumentTypeUsage documentTypeUsage;

    /**
     * @brief Maneja eventos de uso de tipos de documento desde la cola
     * @param event Evento con información del tipo de documento usado
     * @param message El mensaje RabbitMQ raw
     * @param channel El canal RabbitMQ
     * @param deliveryTag El tag de entrega del mensaje
     */
    @RabbitListener(queues = RabbitDocumentTypeUsedConfig.DOCUMENT_TYPE_USED_QUEUE)
    public void handleDocumentTypeEvent(
        EventDto<DocumentTypeUsageDto, EventUsageType> event,
        Message message,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        handleMessage(event, channel, deliveryTag);
    }

    /**
     * @brief Procesa el evento de tipo de documento basado en su tipo
     * @param event El evento de tipo de documento a procesar
     */
    @Override
    protected void processEvent(EventDto<DocumentTypeUsageDto, EventUsageType> event) {
        try {
            switch (event.getType()) {
                case USED:

                    if (!isValidEvent(event)) {
                        log.warn("Invalid document type usage event received");
                        return;
                    }

                    DocumentTypeUsageDto data = event.getData();
                    log.info("Registrando uso de tipo de documento ID: {}, empresa: {}, cantidad: {}",
                             data.getDocumentTypeId(), data.getEnterpriseId(), data.getQuantityUsed());
                    documentTypeUsage.incrementUsageCount(data.getDocumentTypeId(), data.getEnterpriseId());
                    log.info("Uso registrado correctamente para tipo de documento ID: {}", data.getDocumentTypeId());
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de evento no soportado: " + event.getType());
            }
        } catch (Exception e) {
            log.error("Error procesando evento de tipo de documento: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * @brief Valida la integridad de los datos del evento de tipo de documento
     * @param event El evento de tipo de documento a validar
     * @return True si el evento es válido, false en caso contrario
     */
    @Override
    protected boolean isValidEvent(EventDto<DocumentTypeUsageDto, EventUsageType> event) {
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

        DocumentTypeUsageDto data = event.getData();

        if (data.getDocumentTypeId() == null) {
            log.warn("DocumentTypeId es null - campo obligatorio");
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
        return "DocumentType";
    }

    @Override
    protected String extractEventType(EventDto<DocumentTypeUsageDto, EventUsageType> event) {
        if (event == null || event.getType() == null) {
            return null;
        }
        return event.getType().toString();
    }

}
