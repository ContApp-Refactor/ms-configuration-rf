package co.unicauca.edu.co.contables.audit.application.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponse<T> {

    private final List<T> data;
    private final Long totalElements;
    private final int totalPages;
    private final int currentPage;
    private final int pageSize;
    private final boolean hasNext;
    private final boolean hasPrevious;
    
}
