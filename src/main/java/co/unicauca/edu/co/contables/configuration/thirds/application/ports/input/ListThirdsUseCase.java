package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @brief Caso de uso para consulta y listado de terceros
 *
 * Proporciona operaciones de consulta, búsqueda, paginación y conteo
 * para terceros con múltiples criterios de filtrado y ordenamiento.
 */
public interface ListThirdsUseCase {
    /**
     * @brief Obtiene todos los terceros con paginación
     * @param entId El id de la empresa
     * @param pageable El objeto pageable
     * @return La página de terceros
     */
    Page<Third> getAllThirdsBy(String entId, Pageable pageable);

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
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de terceros filtrados por tipo de tercero activo ordenados por defecto ASC
     */
    Page<Third> getThirdsByEntIdAndThirdTypeName(String entId, String thirdTypeName, int page, int size);

    /**
     * @brief Cuenta terceros por empresa y nombre de tipo de tercero activo
     * @param entId El id de la empresa
     * @param thirdTypeName El nombre del tipo de tercero activo (case insensitive)
     * @return Cantidad de terceros que tienen el tipo activo especificado
     */
    long countThirdsByEntIdAndThirdTypeName(String entId, String thirdTypeName);

}
