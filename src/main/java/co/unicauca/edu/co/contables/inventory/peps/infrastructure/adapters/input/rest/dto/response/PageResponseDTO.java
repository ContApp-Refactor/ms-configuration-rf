package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Generic DTO for paginated responses
 * 
 * Provides a stable JSON structure for paginated data, wrapping Spring Data's Page object.
 * @param <T> Type of content in the page
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponseDTO<T> {
    
    /**
     * @brief Content of the current page
     */
    private List<T> content;
    
    /**
     * @brief Current page number (0-based)
     */
    private int pageNumber;
    
    /**
     * @brief Page size (elements per page)
     */
    private int pageSize;
    
    /**
     * @brief Total number of elements across all pages
     */
    private long totalElements;
    
    /**
     * @brief Total number of pages
     */
    private int totalPages;
    
    /**
     * @brief Indicates if this is the last page
     */
    private boolean last;
    
    /**
     * @brief Indicates if this is the first page
     */
    private boolean first;
    
    /**
     * @brief Indicates if the page is empty
     */
    private boolean empty;
    
    /**
     * @brief Number of elements in the current page
     */
    private int numberOfElements;
    
    /**
     * @brief Factory method to create from Spring Data Page
     * @param page The Spring Data Page object
     * @return A new PageResponseDTO instance
     */
    public static <T> PageResponseDTO<T> fromPage(Page<T> page) {
        return PageResponseDTO.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .empty(page.isEmpty())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }
}
