package co.unicauca.edu.co.contables.audit.application.dto.request;

import java.time.Instant;

import co.unicauca.edu.co.contables.audit.domain.enums.ExportFormat;
import co.unicauca.edu.co.contables.audit.domain.enums.UserAction;

import lombok.Builder;
import lombok.Getter;

/**
 * @brief DTO for exporting session events
 */
@Getter
@Builder
public class ExportSessionsRequest {

    private String requestedBy;
    private Instant dateFrom;
    private Instant dateTo;
    private String userName;
    private String userRole;
    private UserAction action;
    private ExportFormat exportFormat;
}
