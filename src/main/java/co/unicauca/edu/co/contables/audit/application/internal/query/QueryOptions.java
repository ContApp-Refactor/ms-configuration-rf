package co.unicauca.edu.co.contables.audit.application.internal.query;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QueryOptions {
    private final int page;
    private final int size;
    private final String sortField;
    private final String sortDirection;
}
