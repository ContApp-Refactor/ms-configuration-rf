package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker;

import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogOperationRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditOperationCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.base.AbstractSpringEventListener;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.mapper.OperationEventMapper;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
public class SpringEventsAuditOperationListener extends AbstractSpringEventListener<OperationEventDto> {

    private final LogAuditOperationCommand logOperationPort;
    private final OperationEventMapper operationEventMapper;

    public SpringEventsAuditOperationListener(
            LogAuditOperationCommand logOperationPort,
            OperationEventMapper operationEventMapper,
            IMessageErrorHandlingPort messageErrorHandlingPort,
            ObjectMapper objectMapper) {
        super(messageErrorHandlingPort, objectMapper);
        this.logOperationPort = logOperationPort;
        this.operationEventMapper = operationEventMapper;
    }

    @EventListener
    public void onOperationEvent(OperationEventDto eventDto) {
        handleEvent(eventDto);
    }

    @Override
    protected void processEvent(OperationEventDto event) {
        LogOperationRequest request = operationEventMapper.toRequest(toAuditEvent(event));
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

        return event != null && event.getOperationType() != null ? event.getOperationType() : "UNKNOWN";
    }

    @Override
    protected String convertEventToJson(OperationEventDto event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to convert\"}";
        }
    }

    private co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.OperationEventDto toAuditEvent(
            OperationEventDto event) {
        return co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.OperationEventDto
                .builder()
                .enterpriseId(event.getEnterpriseId())
                .userId(event.getUserId())
                .userName(event.getUserName())
                .userRole(event.getUserRole())
                .operationType(event.getOperationType())
                .operationAt(event.getOperationAt())
                .moduleName(event.getModuleName())
                .affectedTable(event.getAffectedTable())
                .registerId(event.getRegisterId())
                .dataObject(event.getDataObject())
                .build();
    }
}
