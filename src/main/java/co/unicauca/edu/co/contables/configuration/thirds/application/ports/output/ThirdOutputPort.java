package co.unicauca.edu.co.contables.configuration.thirds.application.ports.output;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @brief Puerto de salida para operaciones de persistencia de terceros
 *
 * Define el contrato para todas las operaciones de acceso a datos
 * relacionadas con la entidad Third, incluyendo CRUD, consultas y operaciones masivas.
 */
public interface ThirdOutputPort {
    /**
     * @brief Guarda un tercero
     * @param third El tercero a guardar
     * @return El tercero guardado
     */
    Third saveThird(Third third);

    /**
     * @brief Guarda múltiples terceros en lote (optimizado)
     * @param thirds Lista de terceros a guardar
     * @return Lista de terceros guardados
     */
    List<Third> saveAllThirds(List<Third> thirds);

    /**
     * @brief Actualiza un tercero
     * @param third El tercero a actualizar
     * @return El tercero actualizado
     */
    Third updateThird(Third third);

    /**
     * @brief Obtiene un tercero por id y empresa
     * @param id El id del tercero
     * @param entId El id de la empresa
     * @return El tercero si existe, vacío en caso contrario
     */
    Optional<Third> getThirdById(Long id, String entId);

    /**
     * @brief Encuentra un tercero por su ID (sin filtrar por empresa)
     * @param id El id del tercero
     * @return El tercero si existe, null en caso contrario
     */
    Third findById(Long id);

    /**
     * @brief Verifica existencia de tercero por id y empresa
     * @param id El id del tercero
     * @param entId El id de la empresa
     * @return True si existe, false en caso contrario
     */
    boolean existThirdById(long id, String entId);

    /**
     * @brief Encuentra números de identificación existentes
     *
     * Verifica cuáles números de identificación ya existen en la base de datos
     * para evitar duplicados durante operaciones de importación.
     *
     * @param idNumbers conjunto de números de identificación a verificar
     * @param entId el id de la empresa
     * @return conjunto de números de identificación que ya existen
     */
    Set<Long> findExistingIdNumbers(Set<Long> idNumbers, String entId);

    /**
     * @brief Cambia el estado de un tercero
     * @param thId El id del tercero
     * @param entId El id de la empresa
     * @return True si se cambió el estado, false en caso contrario
     */
    boolean changeThirdState(Long thId, String entId);

    /**
     * @brief Obtiene todos los terceros con paginación
     * @param entId El id de la empresa
     * @param page El objeto pageable para paginación
     * @return La página de terceros
     */
    Page<Third> getAllThirdsBy(String entId, Pageable page);
    
    /**
     * @brief Obtiene terceros filtrados por estado
     *
     * Optimizado para exportación con filtro de estado directamente en BD
     * para mejorar rendimiento en operaciones de exportación masiva.
     *
     * @param entId El id de la empresa
     * @param state Estado de los terceros (true=activos, false=inactivos)
     * @param page El objeto pageable para paginación
     * @return La página de terceros filtrados por estado
     */
    Page<Third> getAllThirdsByState(String entId, Boolean state, Pageable page);

    /**
     * @brief Obtiene terceros con carga optimizada para exportación (elimina N+1 queries)
     * 
     * Método específico para exportaciones masivas que carga todas las relaciones
     * en batch usando JOIN FETCH, reduciendo drasticamente el número de queries a la BD.
     * Recomendado exclusivamente para operaciones de exportación.
     *
     * @param entId El id de la empresa
     * @param page El objeto pageable para paginación
     * @return La página de terceros con todas sus relaciones pre-cargadas
     */
    Page<Third> getAllThirdsForExport(String entId, Pageable page);

    /**
     * @brief Obtiene terceros filtrados por estado con carga optimizada para exportación
     * 
     * Similar a getAllThirdsForExport pero con filtro de estado.
     * Optimizado para eliminar el problema N+1 queries en exportaciones masivas.
     *
     * @param entId El id de la empresa
     * @param state Estado de los terceros (true=activos, false=inactivos)
     * @param page El objeto pageable para paginación
     * @return La página de terceros filtrados con todas sus relaciones pre-cargadas
     */
    Page<Third> getAllThirdsByStateForExport(String entId, Boolean state, Pageable page);

    /**
     * @brief Elimina un tercero del sistema
     *
     * Elimina un tercero junto con todas sus asociaciones y dependencias
     * del sistema de forma permanente.
     *
     * @param thirdId El ID del tercero a eliminar
     * @param entId El ID de la empresa
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean deleteThird(Long thirdId, String entId);

    /**
     * @brief Cuenta el total de terceros por empresa
     * @param entId El id de la empresa
     * @return El número total de terceros
     */
    long countAllThirdsByEntId(String entId);

    /**
     * @brief Busca terceros por empresa y término de búsqueda
     * @param entId El id de la empresa
     * @param search Término de búsqueda
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de terceros que coinciden con la búsqueda
     */
    Page<Third> findByEntIdAndSearch(String entId, String search, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta terceros por empresa y término de búsqueda
     * @param entId El id de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de terceros que coinciden
     */
    long countByEntIdAndSearch(String entId, String search);

    /**
     * @brief Obtiene todos los terceros con ordenamiento
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de terceros ordenados
     */
    Page<Third> getAllThirdsByWithSort(String entId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Actualiza estado de terceros de forma masiva
     *
     * Cambia el estado de todos los terceros pertenecientes a una empresa
     * en una sola operación optimizada.
     *
     * @param entId El id de la empresa
     * @param newState El nuevo estado (true para activo, false para inactivo)
     * @return La cantidad de terceros actualizados
     */
    int bulkUpdateThirdState(String entId, Boolean newState);

    /**
     * @brief Obtiene terceros activos con ordenamiento
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de terceros activos ordenados
     */
    Page<Third> getAllActiveThirdsByWithSort(String entId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta el total de terceros activos por empresa
     * @param entId El id de la empresa
     * @return El número total de terceros activos
     */
    long countActiveThirdsByEntId(String entId);

    /**
     * @brief Obtiene terceros filtrados por empresa y nombre de tipo de tercero activo con paginación
     * @param entId El id de la empresa
     * @param thirdTypeName El nombre del tipo de tercero activo (case insensitive)
     * @param page El objeto pageable para paginación con ordenamiento ASC por defecto
     * @return La página de terceros filtrados por tipo activo
     */
    Page<Third> getThirdsByEntIdAndThirdTypeName(String entId, String thirdTypeName, Pageable page);

    /**
     * @brief Cuenta el total de terceros por empresa y nombre de tipo de tercero activo
     * @param entId El id de la empresa
     * @param thirdTypeName El nombre del tipo de tercero activo (case insensitive)
     * @return El número total de terceros del tipo activo especificado
     */
    long countThirdsByEntIdAndThirdTypeName(String entId, String thirdTypeName);

    /**
     * @brief Incrementa el contador de uso de un tercero
     * @details Incrementa el contador de uso del tercero sin pasar por las validaciones
     * de actualización completa. Optimizado para operaciones de eventos asíncronos.
     * @param thirdId El id del tercero
     * @return true si se incrementó correctamente, false si el tercero no existe
     */
    boolean incrementUsageCount(Long thirdId);

}
