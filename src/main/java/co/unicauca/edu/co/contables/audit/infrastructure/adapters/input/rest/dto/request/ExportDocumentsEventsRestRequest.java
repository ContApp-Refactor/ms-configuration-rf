package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request;

import java.time.ZonedDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import co.unicauca.edu.co.contables.audit.domain.enums.AuditDateType;
import co.unicauca.edu.co.contables.audit.domain.enums.DocumentOperationType;
import co.unicauca.edu.co.contables.audit.domain.enums.ExportFormat;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.validation.ValidateRange;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidateRange
public class ExportDocumentsEventsRestRequest
        implements DateRangeRequest {

    @NotNull(message = "Date from is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateFrom;

    @NotNull(message = "Date to is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateTo;

    private AuditDateType dateType;

    @NotNull(message = "Enterprise ID is required")
    @Size(max = 100)
    private String enterpriseId;

    @NotNull(message = "Enterprise name is required")
    @Size(max = 100, message = "Enterprise name must not exceed 100 characters")
    private String enterpriseName;

    @Size(max = 50, message = "Document code must not exceed 50 characters")
    private String documentCode;

    @Size(max = 50, message = "Document type must not exceed 50 characters")
    private String documentType;

    @Size(max = 100, message = "Third party name must not exceed 100 characters")
    private String thirdPartyName;

    private DocumentOperationType operationType;

    @Size(max = 100, message = "User name must not exceed 100 characters")
    private String userName;

    @Builder.Default
    private ExportFormat exportFormat = ExportFormat.EXCEL;
}
