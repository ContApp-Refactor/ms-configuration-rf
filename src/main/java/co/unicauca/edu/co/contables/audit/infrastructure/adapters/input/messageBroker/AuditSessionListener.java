package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker;

import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogSessionRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditSessionCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.mapper.SessionEventMapper;
import co.unicauca.edu.co.contables.audit.infrastructure.config.rabbitConfig.RabbitSessionConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import org.springframework.messaging.handler.annotation.Header;

@Component
public class AuditSessionListener extends AbstractMessageListener<SessionEventDto> {

    private final LogAuditSessionCommand logAuditSessionPort;
    private final SessionEventMapper sessionEventMapper;

    public AuditSessionListener(LogAuditSessionCommand logAuditSessionPort,
            SessionEventMapper sessionEventMapper,
            IMessageErrorHandlingPort messageErrorHandlingPort,
            ObjectMapper objectMapper) {
        super(messageErrorHandlingPort, objectMapper);
        this.logAuditSessionPort = logAuditSessionPort;
        this.sessionEventMapper = sessionEventMapper;
    }

    @RabbitListener(queues = RabbitSessionConfig.SESSION_AUDIT_QUEUE)
    public void handleSessionEvent(
            SessionEventDto eventDto,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        handleMessage(eventDto, channel, deliveryTag);
    }

    @Override
    protected void processEvent(SessionEventDto event) {
        LogSessionRequest request = sessionEventMapper.toRequest(event);
        logAuditSessionPort.execute(request);
    }

    @Override
    protected Optional<String> validateEvent(SessionEventDto event) {
        if (event == null)
            return Optional.of("event is null");
        if (event.getSessionId() == null || event.getSessionId().isBlank())
            return Optional.of("sessionId missing");
        if (event.getUserId() == null || event.getUserId().isBlank())
            return Optional.of("userId missing");
        if (event.getUserName() == null || event.getUserName().isBlank())
            return Optional.of("userName missing");
        if (event.getUserRole() == null || event.getUserRole().isEmpty())
            return Optional.of("userRole missing");
        if (event.getAction() == null)
            return Optional.of("action missing");
        if (event.getActionAt() == null)
            return Optional.of("actionAt missing");
        return Optional.empty();
    }

    @Override
    protected String getEntityType() {
        return "Session";
    }

    @Override
    protected String extractEventType(SessionEventDto event) {
        return event != null && event.getAction() != null ? event.getAction() : null;
    }

    @Override
    protected String convertEventToJson(SessionEventDto event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to convert\"}";
        }
    }

}
