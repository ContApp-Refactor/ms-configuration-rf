package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request;

import java.time.ZonedDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import co.unicauca.edu.co.contables.audit.domain.enums.ExportFormat;
import co.unicauca.edu.co.contables.audit.domain.enums.UserAction;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.validation.ValidateRange;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidateRange
public class ExportSessionsRestRequest
        implements DateRangeRequest {

    @NotNull(message = "Date from is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateFrom;

    @NotNull(message = "Date to is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateTo;

    @Size(max = 100, message = "User name must not exceed 100 characters")
    private String userName;

    @Size(max = 100, message = "User role must not exceed 100 characters")
    private String userRole;

    private UserAction action;

    @NotNull(message = "Export format is required")
    private ExportFormat exportFormat;
}
