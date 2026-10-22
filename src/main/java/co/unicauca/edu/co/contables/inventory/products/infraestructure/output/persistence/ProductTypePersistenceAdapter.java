package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductTypeEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces.IProductTypePersistenceMapper;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IProductTypeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @brief Adaptador de persistencia para operaciones CRUD de tipos de producto
 *
 * Implementa IProductTypePersistencePort para gestionar persistencia de tipos de producto
 * con soporte para multitenancy por empresa y operaciones paginadas.
 */
@Component
@RequiredArgsConstructor
public class ProductTypePersistenceAdapter implements IProductTypePersistencePort {

    private final IProductTypeRepository productTypeRepository;
    private final IProductTypePersistenceMapper productTypePersistenceMapper;


    @Override
    public ProductType save(ProductType productType) {
        return productTypePersistenceMapper.toProductType(productTypeRepository.save(productTypePersistenceMapper.toProductTypeEntity(productType)));
    }

    @Override
    public ProductType update(Long id, ProductType productType) {
        productType.setId(id);
        return productTypePersistenceMapper.toProductType(productTypeRepository.save(productTypePersistenceMapper.toProductTypeEntity(productType)));
    }

    @Override
    public void delete(Long id) {
        productTypeRepository.deleteById(id);
    }

    @Override
    public Optional<ProductType> findById(Long id) {
        return productTypeRepository.findById(id).map(productTypePersistenceMapper::toProductType);
    }
    
    @Override
    public Optional<ProductType> findByIdAndEnterpriseId(Long id, String enterpriseId) {
        return productTypeRepository.findByIdAndEnterpriseId(id, enterpriseId).map(productTypePersistenceMapper::toProductType);
    }

    @Override
    public boolean existsByNameAndEnterpriseId(String name, String enterpriseId) {
        return productTypeRepository.existsByNameAndEnterpriseId(name, enterpriseId);
    }

    @Override
    public boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id) {
        return productTypeRepository.existsByNameAndEnterpriseIdAndIdNot(name, enterpriseId, id);
    }

    @Override
    public Page<ProductType> getAllProductTypesByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder) {
        String entitySortField = mapProductTypeSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder) 
            ? Sort.by(entitySortField).descending() 
            : Sort.by(entitySortField).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductTypeEntity> entityPage = productTypeRepository.getProductTypesBy(enterpriseId, pageable);
        return entityPage.map(productTypePersistenceMapper::toProductType);
    }

    @Override
    public Page<ProductType> findByEnterpriseIdAndSearch(String enterpriseId, String search, int page, int size, String sortField, String sortOrder) {
        String entitySortField = mapProductTypeSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder) 
            ? Sort.by(entitySortField).descending() 
            : Sort.by(entitySortField).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductTypeEntity> entityPage = productTypeRepository.findByEnterpriseIdAndSearch(enterpriseId, search, pageable);
        return entityPage.map(productTypePersistenceMapper::toProductType);
    }

    @Override
    public long countByEnterpriseIdAndSearch(String enterpriseId, String search) {
        return productTypeRepository.countByEnterpriseIdAndSearch(enterpriseId, search);
    }

    @Override
    public long countByEnterpriseId(String enterpriseId) {
        return productTypeRepository.countByEnterpriseId(enterpriseId);
    }

    @Override
    public Page<ProductType> findActivatedByEnterpriseId(String enterpriseId, int page, int size) {
        Sort sort = Sort.by("name").ascending(); // Sort por defecto por name asc
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductTypeEntity> entityPage = productTypeRepository.findActivatedByEnterpriseId(enterpriseId, pageable);
        return entityPage.map(productTypePersistenceMapper::toProductType);
    }

    @Override
    public long countActivatedByEnterpriseId(String enterpriseId) {
        return productTypeRepository.countActivatedByEnterpriseId(enterpriseId);
    }

    /**
     * @brief Mapea campo de ordenamiento del dominio a campo de entidad
     * @param sortField campo de ordenamiento del dominio
     * @return campo de ordenamiento de la entidad
     */
    private String mapProductTypeSortField(String sortField) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return "name"; // Default
        }
        if ("name".equalsIgnoreCase(sortField)) {
            return "name";
        }
        return "name"; // Default para cualquier otro campo
    }
}
