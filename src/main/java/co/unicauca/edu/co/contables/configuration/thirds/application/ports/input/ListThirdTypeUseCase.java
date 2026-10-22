package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @brief Caso de uso para consulta y listado de tipos de tercero
 *
 * Proporciona operaciones de consulta, búsqueda, paginación y conteo
 * para tipos de tercero con múltiples criterios de filtrado.
 */
public interface ListThirdTypeUseCase {
    /**
     * @brief Obtiene todos los tipos de terceros
     * @param entId Identificador de la empresa
     * @return Lista de tipos de terceros
     */
    List<ThirdType> getAllThirdTypes(String entId);

    /**
     * @brief Obtiene tipos de tercero con paginación y ordenamiento
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de tipos de tercero
     */
    Page<ThirdType> getAllThirdTypesWithSort(String entId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Busca tipos de tercero por empresa y término de búsqueda
     * @param entId El id de la empresa
     * @param search Término de búsqueda
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de tipos de tercero que coinciden
     */
    Page<ThirdType> findThirdTypesByEntIdAndSearch(String entId, String search, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta tipos de tercero por empresa
     * @param entId El id de la empresa
     * @return Cantidad de tipos de tercero
     */
    long countThirdTypesByEntId(String entId);

    /**
     * @brief Cuenta tipos de tercero por empresa y término de búsqueda
     * @param entId El id de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de tipos de tercero que coinciden
     */
    long countThirdTypesByEntIdAndSearch(String entId, String search);

    /**
     * @brief Cuenta tipos de tercero activos por empresa
     * @param entId El id de la empresa
     * @return Cantidad de tipos de tercero activos
     */
    long countActiveThirdTypesByEntId(String entId);

    /**
     * @brief Obtiene tipos de tercero activos con paginación
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de tipos de tercero activos ordenados por nombre
     */
    Page<ThirdType> getAllActiveThirdTypes(String entId, int page, int size);
}
