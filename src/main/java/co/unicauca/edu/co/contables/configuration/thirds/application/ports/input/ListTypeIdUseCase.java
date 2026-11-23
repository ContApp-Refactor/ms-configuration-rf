package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @brief Caso de uso para consulta y listado de tipos de identificación
 *
 * Proporciona operaciones de consulta, búsqueda, paginación y conteo
 * para tipos de identificación con múltiples criterios de filtrado.
 */
public interface ListTypeIdUseCase {
    /**
     * @brief Obtiene todos los tipos de identificación
     * @param entId Identificador de la empresa
     * @return Lista de tipos de identificación
     */
    List<TypeId> getAllTypeId(String entId);

    /**
     * @brief Obtiene tipos de identificación con paginación y ordenamiento
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de tipos de identificación
     */
    Page<TypeId> getAllTypeIdsWithSort(String entId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Obtiene tipos de identificación activos con paginación
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de tipos de identificación activos ordenados por nombre
     */
    Page<TypeId> getAllActiveTypeIds(String entId, int page, int size);

    /**
     * @brief Busca tipos de identificación por empresa y término de búsqueda
     * @param entId El id de la empresa
     * @param search Término de búsqueda
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de tipos de identificación que coinciden
     */
    Page<TypeId> findByEntIdAndSearch(String entId, String search, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta tipos de identificación por empresa
     * @param entId El id de la empresa
     * @return Cantidad de tipos de identificación
     */
    long countByEntId(String entId);

    /**
     * @brief Cuenta tipos de identificación activos por empresa
     * @param entId El id de la empresa
     * @return Cantidad de tipos de identificación activos
     */
    long countActiveByEntId(String entId);

    /**
     * @brief Cuenta tipos de identificación por empresa y término de búsqueda
     * @param entId El id de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de tipos de identificación que coinciden
     */
    long countByEntIdAndSearch(String entId, String search);
}
