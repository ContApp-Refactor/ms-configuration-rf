package co.unicauca.edu.co.contables.inventory.products.application.ports.output;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

/**
 * @brief Interfaz que define los puertos de persistencia para el servicio de productos.
 * Los puertos de persistencia representan las operaciones de almacenamiento y recuperación
 * de datos relacionadas con los productos.
 */
public interface IProductPersistencePort {

    /**
     * @brief Busca un producto por ID y empresa
     * @param id el ID del producto a buscar
     * @param enterpriseId el ID de la empresa
     * @return Optional con el producto si existe
     */
    Optional<Product> findByIdAndEnterpriseId(Long id, String enterpriseId);

    /**
     * @brief Busca un producto por ID
     * @param id el ID del producto a buscar
     * @return Optional con el producto si existe
     */
    Optional<Product> findById(Long id);

    /**
     * @brief Crea un nuevo producto
     * @param product el producto a crear
     * @return el producto creado con ID asignado
     */
    Product create(Product product);

    /**
     * @brief Crea múltiples productos en lote
     * @details Usa saveAll de JPA para inserción batch, reduciendo queries a BD
     * @param products lista de productos a crear
     * @return lista de productos creados con IDs asignados
     */
    List<Product> saveAll(List<Product> products);

    /**
     * @brief Elimina un producto por ID
     * @param id el ID del producto a eliminar
     */
    void deleteById(Long id);

    /**
     * @brief Busca productos activos por empresa con paginación
     * @param enterpriseId el ID de la empresa
     * @param pageNumber el número de página
     * @param pageSize el tamaño de página
     * @return página de productos activos
     */
    Page<Product> findActivatedWithPagination(String enterpriseId, int pageNumber, int pageSize);
    
    /**
     * @brief Busca productos por empresa y estado con paginación
     * @param enterpriseId el ID de la empresa
     * @param state el estado del producto (true=activo, false=inactivo)
     * @param pageNumber el número de página
     * @param pageSize el tamaño de página
     * @return página de productos filtrados por estado
     */
    Page<Product> findByEnterpriseIdAndState(String enterpriseId, boolean state, int pageNumber, int pageSize);
    
    /**
     * @brief Busca productos por ID de categoría
     * @param categoryId el ID de la categoría
     * @return lista de productos de la categoría
     */
    List<Product> findByCategoryId(Long categoryId);
    
    /**
     * @brief Busca productos por ID de unidad de medida
     * @param unitOfMeasureId el ID de la unidad de medida
     * @return lista de productos con esa unidad de medida
     */
    List<Product> findByUnitOfMeasureId(Long unitOfMeasureId);
    
    /**
     * @brief Busca productos por ID de tipo de producto
     * @param productTypeId el ID del tipo de producto
     * @return lista de productos de ese tipo de producto
     */
    List<Product> findByProductTypeId(Long productTypeId);
    
    /**
     * @brief Verifica existencia de producto por nombre y empresa
     * @param name el nombre del producto
     * @param enterpriseId el ID de la empresa
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameAndEnterpriseId(String name, String enterpriseId);
    
    /**
     * @brief Verifica existencia de producto por referencia y empresa
     * @param reference la referencia del producto
     * @param enterpriseId el ID de la empresa
     * @return true si existe, false en caso contrario
     */
    boolean existsByReferenceAndEnterpriseId(String reference, String enterpriseId);
    
    /**
     * @brief Verifica existencia de producto por nombre y empresa excluyendo ID
     * @param name el nombre del producto
     * @param enterpriseId el ID de la empresa
     * @param id el ID a excluir de la búsqueda
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id);
    
    /**
     * @brief Verifica existencia de producto por referencia y empresa excluyendo ID
     * @param reference la referencia del producto
     * @param enterpriseId el ID de la empresa
     * @param id el ID a excluir de la búsqueda
     * @return true si existe, false en caso contrario
     */
    boolean existsByReferenceAndEnterpriseIdAndIdNot(String reference, String enterpriseId, Long id);
    
    /**
     * @brief Busca productos por empresa con filtros de búsqueda y paginación
     * @param enterpriseId el ID de la empresa
     * @param search el término de búsqueda (opcional)
     * @param pageNumber el número de página
     * @param pageSize el tamaño de página
     * @param sortField el campo de ordenamiento
     * @param sortOrder el orden (asc/desc)
     * @return página de productos
     */
    Page<Product> findByEnterpriseIdWithFilters(String enterpriseId, String search, int pageNumber, int pageSize, String sortField, String sortOrder);
    
   
    long countByEnterpriseIdWithFilters(String enterpriseId, String search);
    
    
    long countByEnterpriseId(String enterpriseId);
    
    long countActivatedByEnterpriseId(String enterpriseId);
}
