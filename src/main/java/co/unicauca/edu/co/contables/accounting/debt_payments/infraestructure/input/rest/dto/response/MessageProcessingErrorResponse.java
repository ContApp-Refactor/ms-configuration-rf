package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageProcessingErrorResponse {
    private Long id;
    private String eventType;
    private String errorDescription;
    private String messageData;
    private Instant errorTimestamp;
    private String entityType;
}
