package co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.listener;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEvent;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.ThirdReplicaEntity;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository.IThirdReplicaRepository;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.EventDtoThird;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.ThirdUpdatedEventDto;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("notificationsSpringEventsThirdListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsThirdEventListener {

    private static final String ENTITY_TYPE = "ThirdParty.Sync";

    private final IThirdReplicaRepository thirdReplicaRepository;
    private final IMessageErrorHandlingPort messageErrorHandlingPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onThirdUpdated(ThirdUpdatedEvent event) {
        handleThirdEvent(toLocalEvent(event));
    }

    private EventDtoThird<ThirdUpdatedEventDto, String> toLocalEvent(ThirdUpdatedEvent event) {
        if (event == null) {
            return null;
        }

        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto source =
                event.getData();
        if (source == null) {
            return new EventDtoThird<>(null, event.getType());
        }

        ThirdUpdatedEventDto data = ThirdUpdatedEventDto.builder()
                .thirdId(source.getThirdId())
                .entId(source.getEntId())
                .fullName(source.getFullName())
                .email(source.getEmail())
                .state(source.getState())
                .build();

        return new EventDtoThird<>(data, event.getType());
    }

    private void handleThirdEvent(EventDtoThird<ThirdUpdatedEventDto, String> event) {
        try {
            log.info("Received {} message from in-process bus", ENTITY_TYPE);

            String validationError = validateEvent(event);
            if (validationError != null) {
                log.warn("Invalid {} event received: {}. Saving error to database.", ENTITY_TYPE, validationError);
                handleValidationError(event, validationError);
                return;
            }

            processEvent(event);
            log.info("{} message processed successfully", ENTITY_TYPE);
        } catch (Exception e) {
            handleProcessingError(e, event);
        }
    }

    private String validateEvent(EventDtoThird<ThirdUpdatedEventDto, String> event) {
        if (event == null) {
            return "Event is null";
        }
        if (event.getData() == null) {
            return "Event data is null";
        }
        if (event.getType() == null) {
            return "Event type is null";
        }

        ThirdUpdatedEventDto data = event.getData();
        if (data.getThirdId() == null) {
            return "thirdId is null";
        }
        if (data.getEntId() == null) {
            return "entId is null";
        }
        if (data.getFullName() == null || data.getFullName().isBlank()) {
            return "fullName is null or blank";
        }
        if (data.getEmail() == null || data.getEmail().isBlank()) {
            return "email is null or blank";
        }
        if (data.getState() == null) {
            return "state is null";
        }

        return null;
    }

    private void processEvent(EventDtoThird<ThirdUpdatedEventDto, String> event) {
        ThirdUpdatedEventDto thirdUpdatedDTO = event.getData();
        Long thirdId = thirdUpdatedDTO != null ? thirdUpdatedDTO.getThirdId() : null;

        try {
            switch (event.getType()) {
                case "THIRD_UPDATED":
                    ThirdReplicaEntity replica = thirdReplicaRepository.findByThirdId(thirdUpdatedDTO.getThirdId())
                            .orElse(new ThirdReplicaEntity());

                    replica.setThirdId(thirdUpdatedDTO.getThirdId());
                    replica.setEnterpriseId(thirdUpdatedDTO.getEntId());
                    replica.setFullName(thirdUpdatedDTO.getFullName());
                    replica.setEmail(thirdUpdatedDTO.getEmail());
                    replica.setActive(thirdUpdatedDTO.getState());

                    thirdReplicaRepository.save(replica);
                    log.info("Third replica for thirdId: {} synchronized successfully.", thirdUpdatedDTO.getThirdId());
                    break;
                default:
                    log.warn("Unhandled event type: {}. The message will be discarded.", event.getType());
                    break;
            }
            log.info("Event processing completed for thirdId: {}", thirdId);
        } catch (Exception e) {
            log.error("Database operation failed for thirdId: {}. Error: {}", thirdId, e.getMessage());
            throw e;
        }
    }

    private void handleValidationError(EventDtoThird<ThirdUpdatedEventDto, String> event, String validationError) {
        try {
            if (messageErrorHandlingPort != null) {
                messageErrorHandlingPort.saveProcessingError(
                        extractEventType(event),
                        "Validation failed: " + validationError,
                        convertEventToJson(event),
                        ENTITY_TYPE);
            }
        } catch (Exception e) {
            log.error("Error saving validation error to database: {}", e.getMessage());
        }
    }

    private void handleProcessingError(Exception e, EventDtoThird<ThirdUpdatedEventDto, String> event) {
        try {
            log.error("Error processing {} message: {}", ENTITY_TYPE, e.getMessage(), e);

            if (messageErrorHandlingPort != null) {
                messageErrorHandlingPort.saveProcessingError(
                        extractEventType(event),
                        String.format("Processing error: %s", e.getMessage()),
                        convertEventToJson(event),
                        ENTITY_TYPE);
            }
        } catch (Exception handlerException) {
            log.error("Error handling processing error: {}", handlerException.getMessage());
        }
    }

    private String extractEventType(EventDtoThird<ThirdUpdatedEventDto, String> event) {
        return event != null ? event.getType() : "unknown";
    }

    private String convertEventToJson(EventDtoThird<ThirdUpdatedEventDto, String> event) {
        if (event == null) {
            return "{\"error\": \"Event is null\"}";
        }

        if (event.getData() == null) {
            return "{\"error\": \"Event data is null\", \"eventType\": \"" +
                    (event.getType() != null ? event.getType() : "null") + "\"}";
        }

        return JsonUtils.thirdDtoToJsonWithNullHandling(event.getData());
    }
}
