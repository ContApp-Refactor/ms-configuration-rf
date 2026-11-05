package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request;

import java.time.ZonedDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import co.unicauca.edu.co.contables.audit.domain.enums.UserAction;
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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidateRange
public class GetSessionsRestRequest implements DateRangeRequest {

    @NotNull(message = "Date from is required")
    @PastOrPresent(message = "Date from cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateFrom;

    @NotNull(message = "Date to is required")
    @PastOrPresent(message = "Date to cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateTo;

    @Size(max = 100, message = "User name must not exceed 100 characters")
    private String userName;

    @Size(max = 100, message = "User role must not exceed 100 characters")
    private String userRole;

    private UserAction action;

    @Builder.Default
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must not exceed 100")
    private Integer size = 20;

    @Builder.Default
    @Pattern(regexp = "^(loginTime|logoutTime|userName|userRole)$", message = "Sort field must be one of: loginTime, logoutTime, userName, userRole")
    private String sortField = "loginTime";

    @Builder.Default
    @Pattern(regexp = "^(ASC|DESC)$", message = "Sort direction must be ASC or DESC")
    private String sortDirection = "DESC";
}
