package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.format.annotation.DateTimeFormat;

import co.unicauca.edu.co.contables.audit.domain.enums.ExportFormat;
import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.validation.ValidateRange;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidateRange
public class ExportOperationsRestRequest
        implements DateRangeRequest {

    @NotNull(message = "Date from is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateFrom;

    @NotNull(message = "Date to is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateTo;

    @Size(max = 100, message = "Module name must not exceed 100 characters")
    private String moduleName;

    @Size(max = 100, message = "Table name must not exceed 100 characters")
    private String affectedTable;

    @Size(max = 100, message = "User name must not exceed 100 characters")
    private String userName;

    @Size(max = 100, message = "User role must not exceed 100 characters")
    private String userRole;

    private OperationType operationType;

    @Size(max = 100, message = "Register ID must not exceed 100 characters")
    private String registerId;

    @NotNull(message = "Enterprise ID is required")
    @Size(max = 100)
    private String enterpriseId;

    @NotNull(message = "Enterprise name is required")
    @Size(max = 150)
    private String enterpriseName;

    @Size(max = 100)
    private String requestedBy;

    @Builder.Default
    private ExportFormat exportFormat = ExportFormat.EXCEL;
}