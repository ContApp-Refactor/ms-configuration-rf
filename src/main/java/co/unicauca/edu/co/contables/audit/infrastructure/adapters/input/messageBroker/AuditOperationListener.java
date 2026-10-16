package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker;

import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogOperationRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditOperationCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.OperationEventDto;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.mapper.OperationEventMapper;
import co.unicauca.edu.co.contables.audit.infrastructure.config.rabbitConfig.RabbitOperationConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

@Component
public class AuditOperationListener extends AbstractMessageListener<OperationEventDto> {

    private final LogAuditOperationCommand logOperationPort;
    private final OperationEventMapper operationEventMapper;

    public AuditOperationListener(LogAuditOperationCommand logOperationPort,
            OperationEventMapper operationEventMapper,
            IMessageErrorHandlingPort messageErrorHandlingPort,
            ObjectMapper objectMapper) {
        super(messageErrorHandlingPort, objectMapper);
        this.logOperationPort = logOperationPort;
        this.operationEventMapper = operationEventMapper;
    }

    @RabbitListener(queues = RabbitOperationConfig.OPERATION_AUDIT_QUEUE)
    public void handleOperationEvent(
            OperationEventDto eventDto,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        handleMessage(eventDto, channel, deliveryTag);
    }

    @Override
    protected void processEvent(OperationEventDto event) {
        LogOperationRequest request = operationEventMapper.toRequest(event);
        logOperationPort.execute(request);
    }

    @Override
    protected Optional<String> validateEvent(OperationEventDto event) {
        if (event == null)
            return Optional.of("event is null");
        if (event.getEnterpriseId() == null || event.getEnterpriseId().isBlank())
            return Optional.of("enterpriseId missing");
        if (event.getUserId() == null || event.getUserId().isBlank())
            return Optional.of("userId missing");
        if (event.getUserName() == null || event.getUserName().isBlank())
            return Optional.of("userName missing");
        if (event.getUserRole() == null || event.getUserRole().isEmpty())
            return Optional.of("userRole missing");
        if (event.getOperationType() == null || event.getOperationType().isBlank())
            return Optional.of("operationType missing");
        if (event.getOperationAt() == null)
            return Optional.of("operationAt missing");
        if (event.getModuleName() == null || event.getModuleName().isBlank())
            return Optional.of("moduleName missing");
        if (event.getAffectedTable() == null || event.getAffectedTable().isBlank())
            return Optional.of("affectedTable missing");
        if (event.getRegisterId() == null || event.getRegisterId().isBlank())
            return Optional.of("registerId missing");
        if (event.getDataObject() == null || event.getDataObject().isEmpty())
            return Optional.of("dataObject missing");
        if (!isValidDataObjectSize(event.getDataObject()))
            return Optional.of("dataObject exceeds max size");
        return Optional.empty();
    }

    private boolean isValidDataObjectSize(Map<String, Object> dataObject) {
        return isValidJsonSize(dataObject, 1_000_000);
    }

    @Override
    protected String getEntityType() {
        return "Operation";
    }

    @Override
    protected String extractEventType(OperationEventDto event) {
        return event != null && event.getOperationType() != null ? event.getOperationType() : null;
    }

    @Override
    protected String convertEventToJson(OperationEventDto event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to convert\"}";
        }
    }

}
