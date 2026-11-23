package co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Contexto compartido entre los pasos ({@link IJobCommand}) de un job programado.
 *
 * Transporta los identificadores del job y de la ejecución, el instante
 * programado, el id de correlación y un mapa tipado de atributos que
 * los pasos consumen y enriquecen (job, ejecución, configuración de
 * entrega, bytes del reporte, banderas de descarga/correo).
 */
@Data
@NoArgsConstructor
public class JobCommandContext {
    public static final String ATTRIBUTE_JOB = "scheduledReportJob";
    public static final String ATTRIBUTE_EXECUTION = "scheduledReportExecution";
    public static final String ATTRIBUTE_DELIVERY_CONFIG = "deliveryConfig";
    public static final String ATTRIBUTE_REPORT_FORMAT = "reportFormat";
    public static final String ATTRIBUTE_REPORT_BYTES = "reportBytes";
    public static final String ATTRIBUTE_DOWNLOAD_ENABLED = "downloadEnabled";
    public static final String ATTRIBUTE_EMAIL_ENABLED = "emailEnabled";
    public static final String ATTRIBUTE_REGISTERED_BOOK = "registeredBook";
    public static final String ATTRIBUTE_REPORT_DATA = "reportData";

    private UUID jobId;
    private UUID executionId;
    private Instant scheduledAt;
    private String correlationId;
    private String triggeredBy;
    private Map<String,Object> attributes = new HashMap<>();

    public JobCommandContext(
            UUID jobId,
            UUID executionId,
            Instant scheduledAt,
            String correlationId,
            String triggeredBy,
            Map<String, Object> attributes
    ) {
        this.jobId = jobId;
        this.executionId = executionId;
        this.scheduledAt = scheduledAt;
        this.correlationId = correlationId;
        this.triggeredBy = triggeredBy;
        this.attributes = copyAttributes(attributes);
    }

    public void putAttribute(String key, Object value) {
        ensureAttributes().put(key, value);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = copyAttributes(attributes);
    }

    public <T> T getAttribute(String key, Class<T> type) {
        Object value = ensureAttributes().get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new IllegalStateException("Context attribute '" + key + "' is not of type " + type.getSimpleName());
        }
        return type.cast(value);
    }

    public <T> T getRequiredAttribute(String key, Class<T> type) {
        T value = getAttribute(key, type);
        if (value == null) {
            throw new IllegalStateException("Missing required context attribute '" + key + "'.");
        }
        return value;
    }

    private Map<String, Object> ensureAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    private Map<String, Object> copyAttributes(Map<String, Object> attributes) {
        if (attributes == null) {
            return new HashMap<>();
        }
        return new HashMap<>(attributes);
    }
}
