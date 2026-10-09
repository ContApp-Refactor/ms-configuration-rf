package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request;

import java.time.ZonedDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.validation.ValidateRange;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
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
public class GetOperationsRestRequest implements DateRangeRequest {

    @NotNull(message = "Date from is required")
    @PastOrPresent(message = "Date from cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateFrom;

    @NotNull(message = "Date to is required")
    @PastOrPresent(message = "Date to cannot be in the future")
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

    @NotNull(message = "Enterprise ID is required")
    @Size(max = 100)
    private String enterpriseId;

    @Size(max = 100, message = "Register ID must not exceed 100 characters")
    private String registerId;

    @Builder.Default
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must not exceed 100")
    private Integer size = 20;

    @Builder.Default
    @Pattern(regexp = "^(operationAt|userName|userRole|operationType|moduleName|affectedTable)$", message = "Sort field must be one of: operationAt, userName, userRole, operationType, moduleName, affectedTable")
    private String sortField = "operationAt";

    @Builder.Default
    @Pattern(regexp = "^(ASC|DESC)$", message = "Sort direction must be ASC or DESC")
    private String sortDirection = "DESC";
}
