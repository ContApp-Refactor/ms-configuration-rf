package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Response DTO for MessageProcessingError
 * 
 * Represents the data structure returned by REST endpoints
 * for message processing error operations.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MessageProcessingErrorDtoResponse {
    
    private Long id;
    private String eventType;
    private String errorDescription;
    private String messageData;
    private Instant errorTimestamp;
    private String entityType;
}