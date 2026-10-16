package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request;

import java.time.ZonedDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import co.unicauca.edu.co.contables.audit.domain.enums.AuditDateType;
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
public class GetDocumentsEventsRestRequest implements DateRangeRequest {

    @NotNull(message = "Date from is required")
    @PastOrPresent(message = "Date from cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateFrom;

    @NotNull(message = "Date to is required")
    @PastOrPresent(message = "Date to cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateTo;

    private AuditDateType dateType;

    @NotNull(message = "Enterprise ID is required")
    private String enterpriseId;

    @Size(max = 50, message = "Document code must not exceed 50 characters")
    private String documentCode;

    @Size(max = 50, message = "Document type must not exceed 50 characters")
    private String documentType;

    @Size(max = 50, message = "Created by must not exceed 100 characters")
    private String createdBy;

    @Size(max = 50, message = "Third party name must not exceed 100 characters")
    private String thirdPartyName;

    @Builder.Default
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must not exceed 100")
    private Integer size = 20;

    @Builder.Default
    @Pattern(regexp = "^(documentDate|lastModifiedAt|documentCode)$", message = "Sort field must be one of: documentDate, lastModifiedAt, documentCode")
    private String sortField = "lastModifiedAt";

    @Builder.Default
    @Pattern(regexp = "^(ASC|DESC)$", message = "Sort direction must be ASC or DESC")
    private String sortDirection = "DESC";

}
