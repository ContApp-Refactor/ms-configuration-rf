package co.unicauca.edu.co.contables.inventory.products.application.service.category;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.ICategoryServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.ICategoryPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.product.ProductService;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryAssociatedException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryInUseException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryNameAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.utils.StringNormalizer;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.OperationType;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;



/**
 * @brief Servicio que implementa lógica de negocio para gestión de categorías
 *
 * Maneja operaciones CRUD de categorías con validaciones de unicidad,
 * normalización de datos y verificación de asociaciones antes de eliminación.
 */
@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryServicePort {

    private final ICategoryPersistencePort categoryPersistencePort;
    private final ProductService productServicePort;

    @Override
    public Category findById(String enterpriseId, Long id) {
        return categoryPersistencePort.findByIdAndEnterpriseId(id, enterpriseId).orElseThrow(CategoryNotFoundException::new);
    }


    @Auditable(operationType = OperationType.CREATE, affectedTable = "CATEGORY")
    @Override
    public Category create(Category category) {
        // Normalizar el nombre de manera consistente (para validación y almacenamiento)
        category.setName(StringNormalizer.normalize(category.getName()));
        validateCategoryUniqueness(category);
        return categoryPersistencePort.create(category);
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "CATEGORY", idArgIndex = 1, enterpriseIdArgIndex = 0)
    @Override
    public Category update(String enterpriseId, Long id, Category category) {
        return categoryPersistencePort.findByIdAndEnterpriseId(id, enterpriseId)
                .map(existingCategory -> {
                    // Verificar que no haya productos en uso antes de permitir la edición
                    validateCategoryNotInUse(id);
                    // Normalizar el nombre de manera consistente (para validación y almacenamiento)
                    category.setName(StringNormalizer.normalize(category.getName()));
                    validateCategoryUniquenessForUpdate(id, category);
                    existingCategory.setName(category.getName());
                    existingCategory.setDescription(category.getDescription());
                    existingCategory.setEnterpriseId(category.getEnterpriseId());
                    existingCategory.setInventoryId(category.getInventoryId());
                    existingCategory.setCostId(category.getCostId());
                    existingCategory.setSaleId(category.getSaleId());
                    existingCategory.setReturnId(category.getReturnId());
                    existingCategory.setTaxes(category.getTaxes());
                    return categoryPersistencePort.create(existingCategory);
                })
                .orElseThrow(CategoryNotFoundException::new);
    }

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "CATEGORY", idArgIndex = 1, enterpriseIdArgIndex = 0)
    @Override
    public void changeState(String enterpriseId, Long id) {
        Category category = categoryPersistencePort.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(CategoryNotFoundException::new);
        category.setState(!category.isState());
        categoryPersistencePort.create(category);
    }

    @Auditable(operationType = OperationType.DELETE, affectedTable = "CATEGORY", idArgIndex = 1, enterpriseIdArgIndex = 0)
    @Override
    public void deleteById(String enterpriseId, Long id) {
        if (categoryPersistencePort.findByIdAndEnterpriseId(id, enterpriseId).isEmpty()) {
            throw new CategoryNotFoundException();
        }
        List<Product> products = productServicePort.findAllByCategory(id);
        if (!products.isEmpty()) {
            throw new CategoryAssociatedException();
        }
        categoryPersistencePort.deleteById(id);
    }

    /**
     * @brief Valida unicidad del nombre de categoría en la empresa
     * @param category categoría a validar
     * @throws CategoryNameAlreadyExistsException si nombre ya existe
     */
    private void validateCategoryUniqueness(Category category) {
        // El nombre ya está normalizado, se usa directamente para validación
        if (categoryPersistencePort.existsByNameAndEnterpriseId(
                category.getName(), category.getEnterpriseId())) {
            throw new CategoryNameAlreadyExistsException(category.getName());
        }
    }

    /**
     * @brief Valida unicidad del nombre durante actualización excluyendo registro actual
     * @param id ID de la categoría que se está actualizando
     * @param category categoría a validar
     * @throws CategoryNameAlreadyExistsException si nombre ya existe en otro registro
     */
    private void validateCategoryUniquenessForUpdate(Long id, Category category) {
        // El nombre ya está normalizado, se usa directamente para validación
        if (categoryPersistencePort.existsByNameAndEnterpriseIdAndIdNot(
                category.getName(), category.getEnterpriseId(), id)) {
            throw new CategoryNameAlreadyExistsException(category.getName());
        }
    }

    /**
     * @brief Valida que la categoría no contenga productos que ya han sido usados
     * @param categoryId ID de la categoría a validar
     * @throws CategoryInUseException si la categoría contiene productos en uso
     */
    private void validateCategoryNotInUse(Long categoryId) {
        List<Product> products = productServicePort.findAllByCategory(categoryId);
        boolean hasProductsInUse = products.stream().anyMatch(Product::isInUse);

        if (hasProductsInUse) {
            throw new CategoryInUseException();
        }
    }

    @Override
    public Page<Category> getAllCategoriesBy(String enterpriseId, Pageable pageable) {
        return categoryPersistencePort.getAllCategoriesBy(enterpriseId, pageable);
    }

    @Override
    public long countAllCategoriesByEntId(String enterpriseId) {
        return categoryPersistencePort.countByEnterpriseId(enterpriseId);
    }

    @Override
    public Page<Category> findByEntIdAndSearch(String enterpriseId, String search, int page, int size, String sortField, String sortOrder) {
        return categoryPersistencePort.findByEnterpriseIdAndSearch(enterpriseId, search, Pageable.ofSize(size).withPage(page));
    }

    @Override
    public long countByEntIdAndSearch(String enterpriseId, String search) {
        return categoryPersistencePort.countByEnterpriseIdAndSearch(enterpriseId, search);
    }

    @Override
    public Page<Category> getAllCategoriesByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder) {
        return categoryPersistencePort.getAllCategoriesByWithSort(enterpriseId, page, size, sortField, sortOrder);
    }

    @Override
    public Page<Category> getAllActiveCategoriesByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder) {
        return categoryPersistencePort.getActiveCategoriesBy(enterpriseId, page, size, sortField, sortOrder);
    }

    @Override
    public long countActiveCategoriesByEntId(String enterpriseId) {
        return categoryPersistencePort.countActiveByEnterpriseId(enterpriseId);
    }
}
