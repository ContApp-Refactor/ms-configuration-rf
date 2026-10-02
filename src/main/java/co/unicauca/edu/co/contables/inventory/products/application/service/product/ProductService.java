package co.unicauca.edu.co.contables.inventory.products.application.service.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductEventPort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.ICategoryPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductInUseException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNameAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductReferenceAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.utils.StringNormalizer;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.utils.PaginationHelper;

import lombok.RequiredArgsConstructor;

/**
 * Servicio que implementa la lógica de negocio para los productos.
 * Esta clase interactúa con los puertos de persistencia y realiza las
 * operaciones
 * necesarias para gestionar los productos.
 */
@Service
@RequiredArgsConstructor
public class ProductService implements IProductServicePort {

    private final IProductPersistencePort productPersistencePort;
    private final IProductEventPort productEventPort;
    private final IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort;
    private final ICategoryPersistencePort categoryPersistencePort;
    private final IProductTypePersistencePort productTypePersistencePort;

    @Override
    public Product findById(Long id, String enterpriseId) {
        return productPersistencePort.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(ProductNotFoundException::new);
    }

    @Override
    public Page<Product> findAllWithFilters(String enterpriseId, String search, int pageNumber, int pageSize,
            String sortField, String sortOrder) {
        return productPersistencePort.findByEnterpriseIdWithFilters(enterpriseId, search, pageNumber, pageSize,
                sortField, sortOrder);
    }

    @Override
    public Page<Product> findAllPaginated(String enterpriseId, Optional<Integer> numPage, Optional<Integer> size,
            String sortField, String sortOrder, Optional<String> search) {
        // Contar total de registros (con o sin filtro)
        long totalRecords = search.isPresent() && !search.get().trim().isEmpty()
                ? countByEnterpriseIdWithFilters(enterpriseId, search.get())
                : countByEnterpriseId(enterpriseId);

        // Crear Pageable flexible
        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Obtener página de datos (con o sin filtro)
        return search.isPresent() && !search.get().trim().isEmpty()
                ? findAllWithFilters(enterpriseId, search.get(), pageable.getPageNumber(), pageable.getPageSize(),
                        sortField, sortOrder)
                : findAllWithFilters(enterpriseId, null, pageable.getPageNumber(), pageable.getPageSize(), sortField,
                        sortOrder);
    }

    @Override
    public long countByEnterpriseIdWithFilters(String enterpriseId, String search) {
        return productPersistencePort.countByEnterpriseIdWithFilters(enterpriseId, search);
    }

    @Override
    public long countByEnterpriseId(String enterpriseId) {
        return productPersistencePort.countByEnterpriseId(enterpriseId);
    }

    @Override
    public long countActivatedByEnterpriseId(String enterpriseId) {
        return productPersistencePort.countActivatedByEnterpriseId(enterpriseId);
    }

    @Override
    public Page<Product> findActivatedWithPagination(String enterpriseId, int pageNumber, int pageSize) {
        return productPersistencePort.findActivatedWithPagination(enterpriseId, pageNumber, pageSize);
    }

    @Override
    public Page<Product> findActivatedPaginated(String enterpriseId, Optional<Integer> numPage,
            Optional<Integer> size) {
        long totalRecords = countActivatedByEnterpriseId(enterpriseId);
        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);
        return findActivatedWithPagination(enterpriseId, pageable.getPageNumber(), pageable.getPageSize());
    }

    @Auditable(operationType = OperationType.CREATE, affectedTable = "PRODUCT")
    @Override
    public Product create(Product product) {
        // Normalizar nombre y referencia de productos en mayúsculas (formato estándar
        // para productos)
        product.setName(StringNormalizer.normalizeCode(product.getName()));
        if (product.getReference() != null && !product.getReference().trim().isEmpty()) {
            product.setReference(StringNormalizer.normalizeCode(product.getReference()));
        }

        validateRelatedEntitiesExistence(product);

        validateProductUniqueness(product);

        Product createdProduct = productPersistencePort.create(product);

        createdProduct.generateCode();

        createdProduct = productPersistencePort.create(createdProduct);

        ProductSyncDto productSyncDto = new ProductSyncDto(
                createdProduct.getId(),
                createdProduct.getName(),
                createdProduct.getReference(),
                createdProduct.getEnterpriseId(),
                createdProduct.getPresentation(),
                createdProduct.getQuantity(),
                createdProduct.getCost(),
                createdProduct.isState());
        productEventPort.publishCreatedStockEvent(productSyncDto);
        return createdProduct;
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "PRODUCT", idArgIndex = 0, enterpriseIdArgIndex = 2)
    @Override
    public Product update(Long id, Product product, String enterpriseId) {

        Product objProduct = productPersistencePort.findByIdAndEnterpriseId(id, enterpriseId)
                .map(existingProduct -> {
                    // Validar que el producto no esté en uso
                    if (existingProduct.isInUse()) {
                        throw new ProductInUseException(
                            "No se puede editar el producto porque tiene movimientos contables"
                        );
                    }
                    
                    product.setName(StringNormalizer.normalizeCode(product.getName()));
                    if (product.getReference() != null && !product.getReference().trim().isEmpty()) {
                        product.setReference(StringNormalizer.normalizeCode(product.getReference()));
                    }

                    validateRelatedEntitiesExistence(product);
                    validateProductUniquenessForUpdate(id, product);

                    boolean shouldRegenerateCode = !existingProduct.getName().equals(product.getName()) ||
                            !existingProduct.getCategoryId().equals(product.getCategoryId()) ||
                            !existingProduct.getId().equals(product.getId());

                    existingProduct.setName(product.getName());
                    existingProduct.setDescription(product.getDescription());
                    existingProduct.setQuantity(product.getQuantity());
                    existingProduct.setUnitOfMeasureId(product.getUnitOfMeasureId());
                    existingProduct.setCategoryId(product.getCategoryId());
                    existingProduct.setCost(product.getCost());
                    existingProduct.setProductTypeId(product.getProductTypeId());
                    existingProduct.setReference(product.getReference());
                    existingProduct.setPresentation(product.getPresentation());

                    if (shouldRegenerateCode) {
                        existingProduct.generateCode();
                    }

                    return productPersistencePort.create(existingProduct);
                })
                .orElseThrow(ProductNotFoundException::new);

        ProductSyncDto productSyncDto = new ProductSyncDto(
                objProduct.getId(),
                objProduct.getName(),
                objProduct.getReference(),
                objProduct.getEnterpriseId(),
                objProduct.getPresentation(),
                objProduct.getQuantity(),
                objProduct.getCost(),
                objProduct.isState());
        productEventPort.publishUpdatedStockEvent(productSyncDto);
        return objProduct;
    }

   
    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "PRODUCT", idArgIndex = 0, enterpriseIdArgIndex = 1)
    @Override
    public void changeState(Long id, String enterpriseId) {
        Product product = productPersistencePort.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(ProductNotFoundException::new);
        product.setState(!product.isState());
        productPersistencePort.create(product);
    }

    @Auditable(operationType = OperationType.DELETE, affectedTable = "PRODUCT", idArgIndex = 0, enterpriseIdArgIndex = 1)
    @Override
    public void deleteById(Long id, String enterpriseId) {
        Product product = productPersistencePort.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(ProductNotFoundException::new);
        
        // Validar que el producto no esté en uso
        if (product.isInUse()) {
            throw new ProductInUseException(
                "No se puede eliminar el producto porque tiene movimientos contables"
            );
        }
        
        productPersistencePort.deleteById(id);

        ProductSyncDto productSyncDto = new ProductSyncDto(
            product.getId(),
            product.getName(),
            product.getReference(),
            product.getEnterpriseId(),
            product.getPresentation(),
            product.getQuantity(),
            product.getCost(),
            product.isState());

        productEventPort.publishDeletedStockEvent(productSyncDto);
    }


    @Override
    public List<Product> findAllByCategory(Long categoryId) {
        return productPersistencePort.findByCategoryId(categoryId);
    }

  

    @Override
    public List<Product> findAllByUnitOfMeasure(Long unitOfMeasureId) {
        return productPersistencePort.findByUnitOfMeasureId(unitOfMeasureId);
    }

   

    @Override
    public List<Product> findAllByProductType(Long productTypeId) {
        return productPersistencePort.findByProductTypeId(productTypeId);
    }

    /**
     * @brief Valida unicidad del nombre y referencia en la empresa
     * @param product producto a validar
     * @throws ProductNameAlreadyExistsException si nombre ya existe
     * @throws ProductReferenceAlreadyExistsException si referencia ya existe
     */
    private void validateProductUniqueness(Product product) {
        if (productPersistencePort.existsByNameAndEnterpriseId(
                product.getName(), product.getEnterpriseId())) {
            throw new ProductNameAlreadyExistsException(product.getName());
        }

        if (product.getReference() != null && !product.getReference().trim().isEmpty()) {
            if (productPersistencePort.existsByReferenceAndEnterpriseId(
                    product.getReference(), product.getEnterpriseId())) {
                throw new ProductReferenceAlreadyExistsException(product.getReference());
            }
        }
    }

    /**
     * @brief Valida unicidad durante actualización excluyendo registro actual
     * @param id ID del producto que se está actualizando
     * @param product producto a validar
     * @throws ProductNameAlreadyExistsException si nombre ya existe en otro registro
     * @throws ProductReferenceAlreadyExistsException si referencia ya existe en otro registro
     */
    private void validateProductUniquenessForUpdate(Long id, Product product) {
        if (productPersistencePort.existsByNameAndEnterpriseIdAndIdNot(
                product.getName(), product.getEnterpriseId(), id)) {
            throw new ProductNameAlreadyExistsException(product.getName());
        }

        if (product.getReference() != null && !product.getReference().trim().isEmpty()) {
            if (productPersistencePort.existsByReferenceAndEnterpriseIdAndIdNot(
                    product.getReference(), product.getEnterpriseId(), id)) {
                throw new ProductReferenceAlreadyExistsException(product.getReference());
            }
        }
    }

    /**
     * @brief Valida existencia y estado activo de entidades relacionadas
     * @param product producto cuyas entidades relacionadas se van a validar
     * @throws UnitOfMeasureNotFoundException si unidad de medida no existe o no está activa
     * @throws CategoryNotFoundException si categoría no existe o no está activa
     * @throws ProductTypeNotFoundException si tipo de producto no existe o no está activo
     */
    private void validateRelatedEntitiesExistence(Product product) {
        if (product.getUnitOfMeasureId() != null) {
            unitOfMeasurePersistencePort.findByIdAndEnterpriseId(
                    product.getUnitOfMeasureId(), product.getEnterpriseId())
                    .filter(UnitOfMeasure::isState) // Verificar que esté activa
                    .orElseThrow(() -> new UnitOfMeasureNotFoundException(
                            "Unidad de medida inactiva o no encontrada", true));
        }

        if (product.getCategoryId() != null) {
            categoryPersistencePort.findByIdAndEnterpriseId(
                    product.getCategoryId(), product.getEnterpriseId())
                    .filter(Category::isState) // Verificar que esté activa
                    .orElseThrow(() -> new CategoryNotFoundException(
                            "Categoría inactiva o no encontrada", true));
        }

        if (product.getProductTypeId() != null) {
            productTypePersistencePort.findByIdAndEnterpriseId(
                    product.getProductTypeId(), product.getEnterpriseId())
                    .filter(ProductType::isState) // Verificar que esté activo
                    .orElseThrow(() -> new ProductTypeNotFoundException(
                            "Tipo de producto inactivo o no encontrado", true));
        }
    }
}
