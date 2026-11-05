package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.StringJoiner;

/**
 * @brief Domain model for message processing errors
 * 
 *        Represents errors that occur during message processing operations,
 *        providing audit trail and debugging information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageProcessingError {
    private Long id;
    private String eventType;
    private String errorDescription;
    private String messageData;
    private Instant errorTimestamp;
    private String entityType;

    // --------- Domain behaviors to avoid anemic model ---------

    /**
     * Ensures invariants and returns this instance for fluent usage.
     * Throws IllegalArgumentException when a required field is missing/blank.
     */
    public MessageProcessingError requireValid() {
        if (isBlank(eventType)) {
            throw new IllegalArgumentException("eventType is required");
        }
        if (isBlank(errorDescription)) {
            throw new IllegalArgumentException("errorDescription is required");
        }
        if (isBlank(entityType)) {
            throw new IllegalArgumentException("entityType is required");
        }
        ensureTimestamp();
        return this;
    }

    /**
     * Ensures errorTimestamp is set; if null, sets to Instant.now().
     */
    private MessageProcessingError ensureTimestamp() {
        if (this.errorTimestamp == null) {
            this.errorTimestamp = Instant.now();
        }
        return this;
    }

    /**
     * Returns a concise, single-line summary, truncating details to the given
     * length.
     */
    public String summary(int maxDetailLength) {
        StringJoiner sj = new StringJoiner(" | ");
        if (!isBlank(eventType))
            sj.add("event=" + eventType);
        if (!isBlank(entityType))
            sj.add("entity=" + entityType);
        sj.add("at=" + (errorTimestamp != null ? errorTimestamp.toString() : "null"));
        String detail = !isBlank(errorDescription) ? errorDescription : String.valueOf(messageData);
        sj.add("detail=" + truncate(detail, Math.max(32, maxDetailLength)));
        return sj.toString();
    }

    // --------------------- helpers ---------------------

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String truncate(String s, int maxLen) {
        if (s == null)
            return null;
        if (maxLen <= 0)
            return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
