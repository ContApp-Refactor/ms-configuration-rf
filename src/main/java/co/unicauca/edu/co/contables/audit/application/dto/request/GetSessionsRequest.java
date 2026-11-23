package co.unicauca.edu.co.contables.audit.application.dto.request;

import java.time.Instant;

import co.unicauca.edu.co.contables.audit.domain.enums.UserAction;

import lombok.Builder;
import lombok.Getter;

/**
 * @brief DTO for session event queries with multiple filters
 */
@Getter
@Builder
public class GetSessionsRequest {

    private Instant dateFrom;
    private Instant dateTo;
    private String userName;
    private String userRole;
    private UserAction action;
    private String requestingUserRole;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortField = "actionAt";

    @Builder.Default
    private String sortDirection = "DESC";

}
