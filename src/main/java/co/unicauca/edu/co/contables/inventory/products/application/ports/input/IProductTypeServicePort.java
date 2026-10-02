package co.unicauca.edu.co.contables.inventory.products.application.ports.input;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import org.springframework.data.domain.Page;
import java.util.Optional;

/**
 * @brief Puerto de entrada para operaciones CRUD de tipos de producto
 *
 * Define contrato completo de operaciones para gestión de tipos de producto:
 * - Operaciones CRUD básicas
 * - Gestión de estados (activar/desactivar)
 * - Consultas paginadas y filtradas por empresa
 */
public interface IProductTypeServicePort {
    /**
     * @brief Crea un nuevo tipo de producto
     * @param productType el tipo de producto a crear
     * @return el tipo de producto creado con ID asignado
     */
    ProductType createProductType(ProductType productType);

    /**
     * @brief Busca un tipo de producto por ID
     * @param id el ID del tipo de producto a buscar
     * @return Optional con el tipo de producto si existe
     */
    Optional<ProductType> findById(Long id);
    
    /**
     * @brief Busca un tipo de producto por ID y empresa
     * @param id el ID del tipo de producto a buscar
     * @param enterpriseId el ID de la empresa
     * @return el tipo de producto encontrado
     * @throws ProductTypeNotFoundException si no se encuentra el tipo de producto
     */
    ProductType getProductTypeByIdAndEnterpriseId(Long id, String enterpriseId);

    /**
     * @brief Actualiza un tipo de producto existente
     * @param id el ID del tipo de producto a actualizar
     * @param enterpriseId el ID de la empresa
     * @param productType los datos del tipo de producto actualizado
     * @return el tipo de producto actualizado con los nuevos datos
     */
    ProductType updateProductType(Long id, String enterpriseId, ProductType productType);

    /**
     * @brief Elimina un tipo de producto por ID y empresa
     * @param id el ID del tipo de producto a eliminar
     * @param enterpriseId el ID de la empresa
     */
    void deleteProductType(Long id, String enterpriseId);
    
    /**
     * @brief Cambia el estado de un tipo de producto (activar/desactivar)
     * @param id el ID del tipo de producto cuyo estado se va a cambiar
     * @param enterpriseId el ID de la empresa
     */
    void changeState(Long id, String enterpriseId);
    
    /**
     * @brief Obtiene todos los tipos de producto con paginación y ordenamiento
     * @param enterpriseId ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo por el que ordenar (solo "name")
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
     * @param sortField campo por el que ordenar (solo "name")
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
     * @brief Obtiene tipos de producto activos con paginación
     * @param enterpriseId ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @return página de tipos de producto activos
     */
    Page<ProductType> findActivatedWithPagination(String enterpriseId, int page, int size);

    /**
     * @brief Cuenta tipos de producto activos por empresa
     * @param enterpriseId ID de la empresa
     * @return cantidad de tipos de producto activos
     */
    long countActivatedByEnterpriseId(String enterpriseId);
}
