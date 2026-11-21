package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.base;

import java.util.Optional;

import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSpringEventListener<T> {

    protected final ObjectMapper objectMapper;

    protected IMessageErrorHandlingPort messageErrorHandlingPort;

    protected AbstractSpringEventListener(
            IMessageErrorHandlingPort messageErrorHandlingPort,
            ObjectMapper objectMapper) {
        this.messageErrorHandlingPort = messageErrorHandlingPort;
        this.objectMapper = objectMapper;
    }

    protected void handleEvent(T event) {
        try {
            log.info("Received {} message from queue", getEntityType());
            Optional<String> validationError = validateEvent(event);
            if (validationError.isPresent()) {
                log.warn("Invalid {} event: {}", getEntityType(), validationError.get());
                handleValidationError(event, validationError.get());
                return;
            }
            processEvent(event);
            log.info("{} message processed successfully", getEntityType());
        } catch (Exception e) {
            handleProcessingError(e, event);
        }
    }

    protected abstract void processEvent(T event);

    protected abstract Optional<String> validateEvent(T event);

    protected abstract String getEntityType();

    private void handleProcessingError(Exception e, T event) {
        try {
            log.error("Error processing {} message: {}", getEntityType(), e.getMessage(), e);

            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String errorDescription = buildErrorDescription(e);
                String errorStage = resolveErrorStage(e);
                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, getEntityType(),
                        errorStage);
            }
        } catch (Exception persistenceException) {
            log.error("Error persisting processing error: {}", persistenceException.getMessage());
        }
    }

    private String buildErrorDescription(Exception e) {
        StringBuilder sb = new StringBuilder();

        sb.append("[").append(e.getClass().getSimpleName()).append("] ");

        sb.append(e.getMessage() != null ? e.getMessage() : "No message");

        Throwable cause = getRootCause(e);
        if (cause != null && cause != e) {
            sb.append(" | Caused by: [")
                    .append(cause.getClass().getSimpleName())
                    .append("] ")
                    .append(cause.getMessage() != null ? cause.getMessage() : "No message");
        }

        StackTraceElement[] stack = e.getStackTrace();
        for (StackTraceElement frame : stack) {
            if (frame.getClassName().startsWith("co.unicauca.edu.co.contables.audit")) {
                sb.append(" | at ").append(frame.getClassName())
                        .append(".").append(frame.getMethodName())
                        .append(":").append(frame.getLineNumber());
                break;
            }
        }

        return sb.toString();
    }

    private Throwable getRootCause(Throwable t) {
        Throwable cause = t.getCause();
        while (cause != null && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private String resolveErrorStage(Exception e) {
        String exName = e.getClass().getSimpleName();
        if (e instanceof RuntimeException || exName.contains("Validation"))
            return "VALIDATION";
        if (exName.contains("Mapping") || exName.contains("JsonMapping")
                || exName.contains("JsonParse") || e instanceof IllegalArgumentException)
            return "MAPPING";
        if (exName.contains("DataAccess") || exName.contains("Persistence")
                || exName.contains("Jpa") || exName.contains("Sql"))
            return "PERSISTENCE";
        return "UNKNOWN";
    }

    private void handleValidationError(T event, String reason) {
        if (messageErrorHandlingPort != null) {
            messageErrorHandlingPort.saveProcessingError(
                    extractEventType(event),
                    "Validation failed: " + reason,
                    convertEventToJson(event),
                    getEntityType(),
                    "VALIDATION");
        }
    }

    protected boolean isValidJsonSize(Object data, int maxBytes) {
        try {
            String json = objectMapper.writeValueAsString(data);
            return json.length() <= maxBytes;
        } catch (Exception e) {
            return false;
        }
    }

    protected abstract String extractEventType(T event);

    protected abstract String convertEventToJson(T event);
}
