package co.unicauca.edu.co.contables.inventory.products.application.ports.input;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @brief Puerto de entrada para operaciones CRUD de categorías de productos
 *
 * Define contrato de operaciones disponibles para gestión de categorías:
 * - Consultas paginadas y filtradas
 * - Operaciones básicas de búsqueda por criterios
 * - Conteo y estadísticas de categorías
 */
public interface ICategoryServicePort {

    /**
     * @brief Busca una categoría por ID y empresa
     * @param enterpriseId ID de la empresa
     * @param id ID de la categoría a buscar
     * @return categoría encontrada o null si no existe
     */
    Category findById(String enterpriseId, Long id);

    /**
     * @brief Obtiene todas las categorías de una empresa con paginación
     * @param enterpriseId ID de la empresa
     * @param pageable objeto de paginación con ordenamiento
     * @return página de categorías de la empresa especificada
     */
    Page<Category> getAllCategoriesBy(String enterpriseId, Pageable pageable);

    /**
     * @brief Cuenta el total de categorías por empresa
     * @param enterpriseId ID de la empresa
     * @return número total de categorías registradas para la empresa
     */
    long countAllCategoriesByEntId(String enterpriseId);

    /**
     * @brief Busca categorías por empresa y término de búsqueda con ordenamiento
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda (coincide con código o descripción)
     * @param page número de página (base 0)
     * @param size tamaño de página
     * @param sortField campo para ordenar resultados
     * @param sortOrder dirección del ordenamiento (asc/desc)
     * @return página de categorías que coinciden con el criterio de búsqueda
     */
    Page<Category> findByEntIdAndSearch(String enterpriseId, String search, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta categorías por empresa y término de búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda para filtrar
     * @return cantidad de categorías que coinciden con el criterio de búsqueda
     */
    long countByEntIdAndSearch(String enterpriseId, String search);

    /**
     * @brief Obtiene todas las categorías con ordenamiento personalizado
     * @param enterpriseId ID de la empresa
     * @param page número de página (base 0)
     * @param size tamaño de página
     * @param sortField campo para ordenar resultados
     * @param sortOrder dirección del ordenamiento (asc/desc)
     * @return página de todas las categorías con ordenamiento personalizado
     */
    Page<Category> getAllCategoriesByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Obtiene todas las categorías activas con ordenamiento personalizado
     * @param enterpriseId ID de la empresa
     * @param page número de página (base 0)
     * @param size tamaño de página
     * @param sortField campo para ordenar resultados
     * @param sortOrder dirección del ordenamiento (asc/desc)
     * @return página de categorías activas con ordenamiento personalizado
     */
    Page<Category> getAllActiveCategoriesByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta el total de categorías activas por empresa
     * @param enterpriseId ID de la empresa
     * @return número total de categorías activas registradas para la empresa
     */
    long countActiveCategoriesByEntId(String enterpriseId);


    /**
     * @brief Crea una nueva categoría
     * @param category datos de la categoría a crear
     * @return categoría creada con ID asignado
     */
    Category create(Category category);

    /**
     * @brief Actualiza una categoría existente
     * @param enterpriseId ID de la empresa
     * @param id ID de la categoría a actualizar
     * @param category datos actualizados de la categoría
     * @return categoría actualizada con los nuevos datos
     */
    Category update(String enterpriseId, Long id, Category category);

    /**
     * @brief Elimina una categoría por ID y empresa
     * @param enterpriseId ID de la empresa
     * @param id ID de la categoría a eliminar
     */
    void deleteById(String enterpriseId, Long id);

    /**
     * @brief Cambia el estado de una categoría (activar/desactivar)
     * @param enterpriseId ID de la empresa
     * @param id ID de la categoría cuyo estado se va a cambiar
     */
    void changeState(String enterpriseId, Long id);
}
