package co.unicauca.edu.co.contables.audit.application.internal.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageResult<T> {
    private final List<T> content;
    private final long totalElements;

    public PageResult(List<T> content, long totalElements) {
        this.content = Collections.unmodifiableList(new ArrayList<>(content));
        this.totalElements = totalElements;
    }

    public List<T> getContent() {
        return content;
    }

    public long getTotalElements() {
        return totalElements;
    }
}
