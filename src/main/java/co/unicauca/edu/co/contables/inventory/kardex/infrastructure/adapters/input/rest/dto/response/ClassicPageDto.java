package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Paginacion en el formato clasico de Spring Data
 *
 * El monolito activa {@code PageSerializationMode.VIA_DTO} a nivel de aplicacion, lo que
 * serializa cualquier {@link Page} como {@code {content, page:{...}}}. Este DTO reproduce a
 * mano la serializacion clasica de {@code PageImpl}
 * ({@code content, pageable, totalPages, ...}) para los endpoints que la exigen, sin tocar
 * el modo global y sin afectar al resto de los paginados del monolito.
 *
 * @param <T> Tipo de los elementos paginados
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassicPageDto<T> {

    private List<T> content;
    private PageableDto pageable;
    private Integer totalPages;
    private Long totalElements;
    private Boolean last;
    private Boolean first;
    private Integer size;
    private Integer number;
    private SortDto sort;
    private Integer numberOfElements;
    private Boolean empty;

    public static <T> ClassicPageDto<T> from(Page<T> page) {
        return ClassicPageDto.<T>builder()
                .content(page.getContent())
                .pageable(PageableDto.from(page.getPageable()))
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .last(page.isLast())
                .first(page.isFirst())
                .size(page.getSize())
                .number(page.getNumber())
                .sort(SortDto.from(page.getSort()))
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageableDto {
        private Integer pageNumber;
        private Integer pageSize;
        private Long offset;
        private Boolean paged;
        private Boolean unpaged;
        private SortDto sort;

        public static PageableDto from(org.springframework.data.domain.Pageable pageable) {
            return PageableDto.builder()
                    .pageNumber(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .paged(pageable.isPaged())
                    .unpaged(pageable.isUnpaged())
                    .sort(SortDto.from(pageable.getSort()))
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortDto {
        private Boolean empty;
        private Boolean sorted;
        private Boolean unsorted;

        public static SortDto from(Sort sort) {
            return SortDto.builder()
                    .empty(sort.isEmpty())
                    .sorted(sort.isSorted())
                    .unsorted(sort.isUnsorted())
                    .build();
        }
    }
}
