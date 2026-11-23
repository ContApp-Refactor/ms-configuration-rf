package co.unicauca.edu.co.contables.inventory.products.application.ports.output;

import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import org.springframework.data.domain.Page;
import java.util.Optional;

/**
 * @brief Interfaz que define los puertos de persistencia para el servicio de tipos de producto.
 * Los puertos de persistencia representan las operaciones de almacenamiento y recuperación
 * de datos relacionadas con los tipos de producto.
 */
public interface  IProductTypePersistencePort {

    /**
     * @brief Guarda un tipo de producto
     * @param productType el tipo de producto a guardar
     * @return el tipo de producto guardado con ID asignado
     */
    ProductType save(ProductType productType);

    /**
     * @brief Busca un tipo de producto por ID y empresa
     * @param id el ID del tipo de producto a buscar
     * @param enterpriseId el ID de la empresa
     * @return Optional con el tipo de producto si existe
     */
    Optional<ProductType> findByIdAndEnterpriseId(Long id, String enterpriseId);
    
    /**
     * @brief Busca un tipo de producto por ID
     * @param id el ID del tipo de producto a buscar
     * @return Optional con el tipo de producto si existe
     */
    Optional<ProductType> findById(Long id);

    /**
     * @brief Actualiza un tipo de producto existente
     * @param id el ID del tipo de producto a actualizar
     * @param productType los datos del tipo de producto actualizado
     * @return el tipo de producto actualizado con los nuevos datos
     */
    ProductType update(Long id, ProductType productType);
    
    /**
     * @brief Elimina un tipo de producto por ID
     * @param id el ID del tipo de producto a eliminar
     */
    void delete(Long id);
    
    /**
     * @brief Verifica existencia de tipo de producto por nombre y empresa
     * @param name el nombre del tipo de producto
     * @param enterpriseId el ID de la empresa
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameAndEnterpriseId(String name, String enterpriseId);
    
    /**
     * @brief Verifica existencia de tipo de producto por nombre y empresa excluyendo ID
     * @param name el nombre del tipo de producto
     * @param enterpriseId el ID de la empresa
     * @param id el ID a excluir de la búsqueda
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id);

    /**
     * @brief Obtiene todos los tipos de producto con paginación y ordenamiento
     * @param enterpriseId ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo por el que ordenar
     * @param sortOrder orden (asc/desc)
     * @return página de tipos de producto
     */
    Page<ProductType> getAllProductTypesByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Busca tipos de producto por empresa y término de búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo por el que ordenar
     * @param sortOrder orden (asc/desc)
     * @return página de tipos de producto que coinciden con la búsqueda
     */
    Page<ProductType> findByEnterpriseIdAndSearch(String enterpriseId, String search, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta tipos de producto por empresa y término de búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @return cantidad de tipos de producto que coinciden
     */
    long countByEnterpriseIdAndSearch(String enterpriseId, String search);

    /**
     * @brief Cuenta todos los tipos de producto de una empresa
     * @param enterpriseId ID de la empresa
     * @return cantidad total de tipos de producto
     */
    long countByEnterpriseId(String enterpriseId);

    /**
     * @brief Busca tipos de producto activos por empresa con paginación
     * @param enterpriseId ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @return página de tipos de producto activos
     */
    Page<ProductType> findActivatedByEnterpriseId(String enterpriseId, int page, int size);

    /**
     * @brief Cuenta tipos de producto activos por empresa
     * @param enterpriseId ID de la empresa
     * @return cantidad de tipos de producto activos
     */
    long countActivatedByEnterpriseId(String enterpriseId);
}
