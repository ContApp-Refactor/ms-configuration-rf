package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.CategoryEntity;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapper para transformación entre dominio y persistencia de categorías
 *
 * Define contratos de mapeo bidireccional entre entidades JPA CategoryEntity
 * y objetos de dominio Category para operaciones de persistencia.
 */
@Mapper(componentModel = "spring")
public interface ICategoryPersistenceMapper {

    /**
     * @brief Convierte objeto de dominio a entidad JPA
     * @param category objeto Category del dominio
     * @return CategoryEntity correspondiente para persistencia
     */
    @Mapping(target = "tenantId", ignore = true)
    CategoryEntity toCategoryEntity(Category category);

    /**
     * @brief Convierte entidad JPA a objeto de dominio
     * @param categoryEntity CategoryEntity de persistencia
     * @return objeto Category del dominio
     */
    default Category toCategory(CategoryEntity categoryEntity) {
        if (categoryEntity == null) {
            return null;
        }

        return Category.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .description(categoryEntity.getDescription())
                .enterpriseId(categoryEntity.getEnterpriseId())
                .inventoryId(categoryEntity.getInventoryId())
                .costId(categoryEntity.getCostId())
                .saleId(categoryEntity.getSaleId())
                .returnId(categoryEntity.getReturnId())
                .taxes(categoryEntity.getTaxes())
                .state(categoryEntity.isState())
                .build();
    }

    /**
     * @brief Convierte lista de entidades JPA a lista de objetos de dominio
     * @param categoryEntityList lista de CategoryEntity de persistencia
     * @return lista de objetos Category del dominio
     */
    List<Category> toCategoryList(List<CategoryEntity> categoryEntityList);
}
