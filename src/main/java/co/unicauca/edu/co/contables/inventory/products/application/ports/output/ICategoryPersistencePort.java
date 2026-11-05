package co.unicauca.edu.co.contables.inventory.products.application.ports.output;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @brief Interfaz que define los puertos de persistencia para el servicio de categorías.
 * Los puertos de persistencia representan las operaciones de almacenamiento y recuperación
 * de datos relacionadas con las categorías.
 */
public interface ICategoryPersistencePort {

    /**
     * @brief Busca una categoría por ID y empresa
     * @param id el ID de la categoría a buscar
     * @param enterpriseId el ID de la empresa
     * @return Optional con la categoría si existe
     */
    Optional<Category> findByIdAndEnterpriseId(Long id, String enterpriseId);

    /**
     * @brief Crea una nueva categoría
     * @param category la categoría a crear
     * @return la categoría creada con ID asignado
     */
    Category create(Category category);

    /**
     * @brief Elimina una categoría por ID
     * @param id el ID de la categoría a eliminar
     */
    void deleteById(Long id);



    /**
     * @brief Obtiene todas las categorías de una empresa con paginación
     * @param enterpriseId el ID de la empresa
     * @param pageable objeto de paginación con ordenamiento
     * @return página de categorías de la empresa especificada
     */
    Page<Category> getAllCategoriesBy(String enterpriseId, Pageable pageable);

    /**
     * @brief Obtiene categorías por empresa y estado con paginación
     * @param enterpriseId identificador de la empresa
     * @param state estado de las categorías (true=activas, false=inactivas)
     * @param pageable objeto Pageable con información de paginación
     * @return página de categorías filtradas por estado
     */
    Page<Category> getAllCategoriesByState(String enterpriseId, Boolean state, Pageable pageable);

    /**
     * @brief Busca categorías por empresa y término de búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @param pageable paginación con ordenamiento
     * @return página de categorías que coinciden con la búsqueda
     */
    Page<Category> findByEnterpriseIdAndSearch(String enterpriseId, String search, Pageable pageable);

    /**
     * @brief Cuenta categorías por empresa y término de búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @return cantidad de categorías que coinciden
     */
    long countByEnterpriseIdAndSearch(String enterpriseId, String search);

    /**
     * @brief Obtiene todas las categorías con ordenamiento personalizado
     * @param enterpriseId el ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (asc/desc)
     * @return página de categorías ordenadas
     */
    Page<Category> getAllCategoriesByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta el total de categorías por empresa
     * @param enterpriseId el ID de la empresa
     * @return el número total de categorías
     */
    long countByEnterpriseId(String enterpriseId);

    /**
     * @brief Obtiene categorías activas de una empresa con ordenamiento
     * @param enterpriseId ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (asc/desc)
     * @return página de categorías activas
     */
    Page<Category> getActiveCategoriesBy(String enterpriseId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta el total de categorías activas por empresa
     * @param enterpriseId ID de la empresa
     * @return cantidad total de categorías activas
     */
    long countActiveByEnterpriseId(String enterpriseId);
    
    /**
     * @brief Verifica existencia de categoría por nombre y empresa
     * @param name el nombre de la categoría
     * @param enterpriseId el ID de la empresa
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameAndEnterpriseId(String name, String enterpriseId);
    
    /**
     * @brief Verifica existencia de categoría por nombre y empresa excluyendo ID
     * @param name el nombre de la categoría
     * @param enterpriseId el ID de la empresa
     * @param id el ID a excluir de la búsqueda
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id);
}
