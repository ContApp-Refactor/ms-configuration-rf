package co.unicauca.edu.co.contables.inventory.products.application.service.productType;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductTypeServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeAssociatedException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeInUseException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeNameAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.utils.StringNormalizer;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.OperationType;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductTypeService implements IProductTypeServicePort {

    private final IProductTypePersistencePort productTypeOutputPort;
    private final IProductServicePort productServicePort;

    public ProductTypeService(IProductTypePersistencePort productTypeOutputPort,
            IProductServicePort productServicePort) {
        this.productTypeOutputPort = productTypeOutputPort;
        this.productServicePort = productServicePort;
    }

    @Auditable(operationType = OperationType.CREATE, affectedTable = "PRODUCT_TYPE")
    @Override
    public ProductType createProductType(ProductType productType) {
        // Normalizar el nombre de manera consistente (para validación y almacenamiento)
        productType.setName(StringNormalizer.normalize(productType.getName()));
        validateProductTypeUniqueness(productType);
        return productTypeOutputPort.save(productType);
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "PRODUCT_TYPE")
    @Override
    public ProductType updateProductType(Long id, String enterpriseId, ProductType productType) {
        // Verificar que el tipo de producto existe y pertenece a la empresa antes de actualizar
        Optional<ProductType> existingProductType = productTypeOutputPort.findByIdAndEnterpriseId(id, enterpriseId);
        if (existingProductType.isEmpty()) {
            throw new ProductTypeNotFoundException();
        }

        // Verificar que el tipo de producto no contenga productos en uso antes de permitir la edición
        validateProductTypeNotInUse(id);

        // Normalizar el nombre de manera consistente (para validación y almacenamiento)
        productType.setName(StringNormalizer.normalize(productType.getName()));
        validateProductTypeUniquenessForUpdate(id, productType);

        return productTypeOutputPort.update(id, productType);
    }

    @Auditable(operationType = OperationType.DELETE, affectedTable = "PRODUCT_TYPE")
    @Override
    public void deleteProductType(Long id, String enterpriseId) {
        // Verificar que el tipo de producto existe y pertenece a la empresa antes de eliminar
        Optional<ProductType> existingProductType = productTypeOutputPort.findByIdAndEnterpriseId(id, enterpriseId);
        if (existingProductType.isEmpty()) {
            throw new ProductTypeNotFoundException();
        }
        // Verificar que el tipo de producto no esté asociado a productos
        List<Product> products = productServicePort.findAllByProductType(id);
        if (!products.isEmpty()) {
            throw new ProductTypeAssociatedException();
        }
        productTypeOutputPort.delete(id);
    }

    @Override
    public Optional<ProductType> findById(Long id) {
        return productTypeOutputPort.findById(id);
    }
    
    /**
     * Busca un tipo de producto por ID y empresa, lanza excepción si no se encuentra.
     * 
     * @param id el ID del tipo de producto a buscar
     * @param enterpriseId el ID de la empresa
     * @return el tipo de producto encontrado
     * @throws ProductTypeNotFoundException si no se encuentra el tipo de producto
     */
    public ProductType getProductTypeByIdAndEnterpriseId(Long id, String enterpriseId) {
        return productTypeOutputPort.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(() -> new ProductTypeNotFoundException());
    }
    
    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "PRODUCT_TYPE")
    @Override
    public void changeState(Long id, String enterpriseId) {
        ProductType productType = productTypeOutputPort.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(() -> new ProductTypeNotFoundException());
        productType.setState(!productType.isState());
        productTypeOutputPort.save(productType);
    }
    
    @Override
    public Page<ProductType> getAllProductTypesByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder) {
        return productTypeOutputPort.getAllProductTypesByWithSort(enterpriseId, page, size, sortField, sortOrder);
    }

    @Override
    public Page<ProductType> findByEnterpriseIdAndSearch(String enterpriseId, String search, int page, int size, String sortField, String sortOrder) {
        return productTypeOutputPort.findByEnterpriseIdAndSearch(enterpriseId, search, page, size, sortField, sortOrder);
    }

    @Override
    public long countByEnterpriseIdAndSearch(String enterpriseId, String search) {
        return productTypeOutputPort.countByEnterpriseIdAndSearch(enterpriseId, search);
    }

    @Override
    public long countByEnterpriseId(String enterpriseId) {
        return productTypeOutputPort.countByEnterpriseId(enterpriseId);
    }

    @Override
    public Page<ProductType> findActivatedWithPagination(String enterpriseId, int page, int size) {
        return productTypeOutputPort.findActivatedByEnterpriseId(enterpriseId, page, size);
    }

    @Override
    public long countActivatedByEnterpriseId(String enterpriseId) {
        return productTypeOutputPort.countActivatedByEnterpriseId(enterpriseId);
    }
    
    /**
     * @brief Valida unicidad del nombre de tipo de producto en la empresa
     * @param productType tipo de producto a validar
     * @throws ProductTypeNameAlreadyExistsException si nombre ya existe
     */
    private void validateProductTypeUniqueness(ProductType productType) {
        // El nombre ya está normalizado, se usa directamente para validación
        if (productTypeOutputPort.existsByNameAndEnterpriseId(
                productType.getName(), productType.getEnterpriseId())) {
            throw new ProductTypeNameAlreadyExistsException(productType.getName());
        }
    }
    
    /**
     * @brief Valida unicidad del nombre durante actualización excluyendo registro actual
     * @param id ID del tipo de producto que se está actualizando
     * @param productType tipo de producto a validar
     * @throws ProductTypeNameAlreadyExistsException si nombre ya existe en otro registro
     */
    private void validateProductTypeUniquenessForUpdate(Long id, ProductType productType) {
        // El nombre ya está normalizado, se usa directamente para validación
        if (productTypeOutputPort.existsByNameAndEnterpriseIdAndIdNot(
                productType.getName(), productType.getEnterpriseId(), id)) {
            throw new ProductTypeNameAlreadyExistsException(productType.getName());
        }
    }

    /**
     * @brief Valida que el tipo de producto no contenga productos que ya han sido usados
     * @param productTypeId ID del tipo de producto a validar
     * @throws ProductTypeInUseException si el tipo de producto contiene productos en uso
     */
    private void validateProductTypeNotInUse(Long productTypeId) {
        List<Product> products = productServicePort.findAllByProductType(productTypeId);
        boolean hasProductsInUse = products.stream().anyMatch(Product::isInUse);

        if (hasProductsInUse) {
            throw new ProductTypeInUseException();
        }
    }
}
