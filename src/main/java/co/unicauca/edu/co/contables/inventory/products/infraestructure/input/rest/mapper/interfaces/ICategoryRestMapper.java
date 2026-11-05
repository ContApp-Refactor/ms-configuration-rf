package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.CategoryCreateRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.CategoryResponse;

import java.util.List;

import org.mapstruct.Mapper;

/**
 * @brief Mapper para transformación de entidades de categoría
 *
 * Define contratos de mapeo entre entidades de dominio Category
 * y DTOs de request/response para operaciones REST.
 */
@Mapper(componentModel = "spring")
public interface ICategoryRestMapper {

    /**
     * @brief Convierte DTO de creación a entidad de categoría
     * @param categoryCreateRequest solicitud de creación de categoría
     * @return entidad de categoría convertida
     */
    Category toCategory(CategoryCreateRequest categoryCreateRequest);

    /**
     * @brief Convierte entidad de categoría a DTO de respuesta
     * @param category entidad de categoría
     * @return respuesta de categoría convertida
     */
    default CategoryResponse toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .enterpriseId(category.getEnterpriseId())
                .inventoryId(category.getInventoryId())
                .costId(category.getCostId())
                .saleId(category.getSaleId())
                .returnId(category.getReturnId())
                .taxes(category.getTaxes())
                .state(category.isState())
                .build();
    }

    /**
     * @brief Convierte lista de entidades a lista de DTOs de respuesta
     * @param categoryList lista de entidades de categoría
     * @return lista de respuestas de categoría convertidas
     */
    default List<CategoryResponse> toCategoryResponseList(List<Category> categoryList) {
        if (categoryList == null) {
            return null;
        }

        return categoryList.stream()
                .map(this::toCategoryResponse)
                .toList();
    }
}
