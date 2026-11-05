package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker;

import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogDocumentEventRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditDocumentEventCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.DocumentEventDto;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.mapper.DocumentEventMapper;
import co.unicauca.edu.co.contables.audit.infrastructure.config.rabbitConfig.RabbitDocumentConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

@Component
public class AuditDocumentEventDtoListener extends AbstractMessageListener<DocumentEventDto> {

    private final LogAuditDocumentEventCommand logDocumentEventPort;
    private final DocumentEventMapper documentEventMapper;

    public AuditDocumentEventDtoListener(LogAuditDocumentEventCommand logDocumentEventPort,
            DocumentEventMapper documentEventMapper,
            IMessageErrorHandlingPort messageErrorHandlingPort,
            ObjectMapper objectMapper) {
        super(messageErrorHandlingPort, objectMapper);
        this.logDocumentEventPort = logDocumentEventPort;
        this.documentEventMapper = documentEventMapper;
    }

    @RabbitListener(queues = RabbitDocumentConfig.DOCUMENT_AUDIT_QUEUE)
    public void handleDocumentEvent(
            DocumentEventDto eventDto,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        handleMessage(eventDto, channel, deliveryTag);
    }

    @Override
    protected void processEvent(DocumentEventDto event) {
        LogDocumentEventRequest request = documentEventMapper.toRequest(event);
        logDocumentEventPort.execute(request);
    }

    @Override
    protected Optional<String> validateEvent(DocumentEventDto event) {
        if (event == null)
            return Optional.of("event is null");
        if (event.getEnterpriseId() == null || event.getEnterpriseId().isBlank())
            return Optional.of("enterpriseId missing");
        if (event.getDocumentId() == null || event.getDocumentId().isBlank())
            return Optional.of("documentId missing");
        if (event.getDocumentCode() == null || event.getDocumentCode().isBlank())
            return Optional.of("documentCode missing");
        if (event.getDocumentType() == null || event.getDocumentType().isBlank())
            return Optional.of("documentType missing");
        if (event.getDocumentDate() == null)
            return Optional.of("documentDate missing");
        if (event.getUserId() == null || event.getUserId().isBlank())
            return Optional.of("userId missing");
        if (event.getUserName() == null || event.getUserName().isBlank())
            return Optional.of("userName missing");
        if (event.getUserRoles() == null || event.getUserRoles().isEmpty())
            return Optional.of("userRoles missing");
        if (event.getOperationType() == null)
            return Optional.of("operationType missing");
        if (event.getModuleName() == null || event.getModuleName().isBlank())
            return Optional.of("moduleName missing");
        if (event.getOperationAt() == null)
            return Optional.of("operationAt missing");
        if (event.getDocumentData() == null || event.getDocumentData().isEmpty())
            return Optional.of("documentData missing");
        if (!isValidDocumentDataSize(event.getDocumentData()))
            return Optional.of("documentData exceeds max size");
        return Optional.empty();
    }

    private boolean isValidDocumentDataSize(Map<String, Object> dataObject) {
        return isValidJsonSize(dataObject, 1_000_000);
    }

    @Override
    protected String getEntityType() {
        return "Document_event";
    }

    @Override
    protected String extractEventType(DocumentEventDto event) {
        return event != null && event.getOperationType() != null ? event.getOperationType() : null;
    }

    @Override
    protected String convertEventToJson(DocumentEventDto event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to convert\"}";
        }
    }

}
