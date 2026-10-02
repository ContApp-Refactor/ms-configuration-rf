package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces.IProductPersistenceMapper;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * @brief Adaptador de persistencia para operaciones CRUD de productos
 *
 * Implementa IProductPersistencePort para gestionar persistencia de productos
 * con soporte para multitenancy por empresa y operaciones paginadas.
 */
@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements IProductPersistencePort {

    private final IProductRepository productRepository;
    private final IProductPersistenceMapper productPersistenceMapper;

    @Override
    public Optional<Product> findByIdAndEnterpriseId(Long id, String enterpriseId) {
        return productRepository.findByIdAndEnterpriseId(id, enterpriseId)
                .map(productPersistenceMapper::toProduct);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id)
                .map(productPersistenceMapper::toProduct);
    }

    @Override
    public Product create(Product product) {
        return productPersistenceMapper.toProduct(productRepository.save(productPersistenceMapper.toProductEntity(product)));
    }

    @Override
    public List<Product> saveAll(List<Product> products) {
        List<ProductEntity> entities = products.stream()
                .map(productPersistenceMapper::toProductEntity)
                .toList();
        
        List<ProductEntity> savedEntities = productRepository.saveAll(entities);
        
        return savedEntities.stream()
                .map(productPersistenceMapper::toProduct)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(Long.valueOf(id));
    }

    @Override
    public Page<Product> findActivatedWithPagination(String enterpriseId, int pageNumber, int pageSize) {
        Sort sort = Sort.by("name").ascending();
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        return productRepository.findByEnterpriseIdAndState(enterpriseId, true, pageRequest)
                .map(productPersistenceMapper::toProduct);
    }

    @Override
    public Page<Product> findByEnterpriseIdAndState(String enterpriseId, boolean state, int pageNumber, int pageSize) {
        Sort sort = Sort.by("name").ascending();
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        return productRepository.findByEnterpriseIdAndState(enterpriseId, state, pageRequest)
                .map(productPersistenceMapper::toProduct);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return productPersistenceMapper.toProductList(
                productRepository.findByCategoryId(categoryId));
    }

    @Override
    public List<Product> findByUnitOfMeasureId(Long unitOfMeasureId) {
        return productPersistenceMapper.toProductList(
                productRepository.findByUnitOfMeasureId(unitOfMeasureId));
    }

    @Override
    public List<Product> findByProductTypeId(Long productTypeId) {
        return productPersistenceMapper.toProductList(
                productRepository.findByProductTypeId(productTypeId));
    }

    @Override
    public boolean existsByNameAndEnterpriseId(String name, String enterpriseId) {
        return productRepository.existsByNameAndEnterpriseId(name, enterpriseId);
    }

    @Override
    public boolean existsByReferenceAndEnterpriseId(String reference, String enterpriseId) {
        return productRepository.existsByReferenceAndEnterpriseId(reference, enterpriseId);
    }

    @Override
    public boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id) {
        return productRepository.existsByNameAndEnterpriseIdAndIdNot(name, enterpriseId, id);
    }

    @Override
    public boolean existsByReferenceAndEnterpriseIdAndIdNot(String reference, String enterpriseId, Long id) {
        return productRepository.existsByReferenceAndEnterpriseIdAndIdNot(reference, enterpriseId, id);
    }

    @Override
    public Page<Product> findByEnterpriseIdWithFilters(String enterpriseId, String search, int pageNumber, int pageSize, String sortField, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc") ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        return productRepository.findByEnterpriseIdWithFilters(enterpriseId, search, pageRequest)
                .map(productPersistenceMapper::toProduct);
    }

    @Override
    public long countByEnterpriseIdWithFilters(String enterpriseId, String search) {
        return productRepository.countByEnterpriseIdWithFilters(enterpriseId, search);
    }

    @Override
    public long countByEnterpriseId(String enterpriseId) {
        return productRepository.countByEnterpriseId(enterpriseId);
    }

    @Override
    public long countActivatedByEnterpriseId(String enterpriseId) {
        return productRepository.countByEnterpriseIdAndState(enterpriseId, true);
    }
}
