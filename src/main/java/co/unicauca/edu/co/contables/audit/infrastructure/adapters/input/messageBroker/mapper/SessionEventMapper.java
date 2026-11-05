package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.mapper;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogSessionRequest;
import co.unicauca.edu.co.contables.audit.domain.enums.UserAction;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;

@Component
public class SessionEventMapper {

    public LogSessionRequest toRequest(SessionEventDto eventDto) {
        return LogSessionRequest.builder()
                .sessionId(sanitizeText(eventDto.getSessionId()))
                .userId(sanitizeText(eventDto.getUserId()))
                .userName(sanitizeText(eventDto.getUserName()))
                .userRole(eventDto.getUserRole())
                .action(parseUserAction(eventDto.getAction()))
                .actionAt(eventDto.getActionAt())
                .ipAddress(eventDto.getIpAddress())
                .build();
    }

    private UserAction parseUserAction(String action) {
        if (action == null) {
            throw new RuntimeException("User action cannot be null");
        }
        try {
            return UserAction.valueOf(action.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid user action received: " + action);
        }
    }

    private String sanitizeText(String input) {
        if (input == null)
            return null;
        String sanitized = input.trim()
                .replaceAll("[\\n\\r\\t]", " ")
                .replaceAll("[<>]", "");
        return sanitized;
    }

}
