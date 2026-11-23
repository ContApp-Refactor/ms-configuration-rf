package co.unicauca.edu.co.contables.inventory.products.application.ports.output;

import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @brief Interfaz que define los puertos de persistencia para el servicio de unidades de medida.
 * Los puertos de persistencia representan las operaciones de almacenamiento y recuperación
 * de datos relacionadas con las unidades de medida.
 */
public interface IUnitOfMeasurePersistencePort {

    /**
     * @brief Busca una unidad de medida por ID y empresa
     * @param id el ID de la unidad de medida a buscar
     * @param enterpriseId el ID de la empresa
     * @return Optional con la unidad de medida si existe
     */
    Optional<UnitOfMeasure> findByIdAndEnterpriseId(Long id, String enterpriseId);


    /**
     * @brief Obtiene unidades de medida por empresa y estado con paginación
     * @param enterpriseId identificador de la empresa
     * @param state estado de las unidades de medida (true=activas, false=inactivas)
     * @param pageable objeto Pageable con información de paginación
     * @return página de unidades de medida filtradas por estado
     */
    Page<UnitOfMeasure> getAllUnitOfMeasuresByState(String enterpriseId, Boolean state, Pageable pageable);

    /**
     * @brief Busca unidades de medida por empresa y término de búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (asc/desc)
     * @return página de unidades de medida que coinciden con la búsqueda
     */
    Page<UnitOfMeasure> findByEnterpriseIdAndSearch(String enterpriseId, String search, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta unidades de medida por empresa y término de búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @return cantidad de unidades de medida que coinciden
     */
    long countByEnterpriseIdAndSearch(String enterpriseId, String search);

    /**
     * @brief Obtiene todas las unidades de medida con ordenamiento personalizado
     * @param enterpriseId el ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (asc/desc)
     * @return página de unidades de medida ordenadas
     */
    Page<UnitOfMeasure> getAllUnitOfMeasuresByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta el total de unidades de medida por empresa
     * @param enterpriseId el ID de la empresa
     * @return el número total de unidades de medida
     */
    long countByEnterpriseId(String enterpriseId);

    /**
     * @brief Obtiene unidades de medida activas de una empresa con ordenamiento
     * @param enterpriseId ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (asc/desc)
     * @return página de unidades de medida activas ordenadas
     */
    Page<UnitOfMeasure> getActiveUnitOfMeasuresBy(String enterpriseId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta el total de unidades de medida activas por empresa
     * @param enterpriseId ID de la empresa
     * @return cantidad total de unidades de medida activas
     */
    long countActiveByEnterpriseId(String enterpriseId);

    /**
     * @brief Crea una nueva unidad de medida
     * @param unitOfMeasure la unidad de medida a crear
     * @return la unidad de medida creada con ID asignado
     */
    UnitOfMeasure create(UnitOfMeasure unitOfMeasure);

    /**
     * @brief Elimina una unidad de medida por ID y empresa
     * @param id el ID de la unidad de medida a eliminar
     * @param enterpriseId el ID de la empresa
     */
    void deleteByIdAndEnterpriseId(Long id, String enterpriseId);

    /**
     * @brief Verifica existencia de unidad de medida por nombre y empresa
     * @param name el nombre de la unidad de medida
     * @param enterpriseId el ID de la empresa
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameAndEnterpriseId(String name, String enterpriseId);
    
    /**
     * @brief Verifica existencia de unidad de medida por abreviación y empresa
     * @param abbreviation la abreviación de la unidad de medida
     * @param enterpriseId el ID de la empresa
     * @return true si existe, false en caso contrario
     */
    boolean existsByAbbreviationAndEnterpriseId(String abbreviation, String enterpriseId);
    
    /**
     * @brief Verifica existencia de unidad de medida por nombre y empresa excluyendo ID
     * @param name el nombre de la unidad de medida
     * @param enterpriseId el ID de la empresa
     * @param id el ID a excluir de la búsqueda
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id);
    
    /**
     * @brief Verifica existencia de unidad de medida por abreviación y empresa excluyendo ID
     * @param abbreviation la abreviación de la unidad de medida
     * @param enterpriseId el ID de la empresa
     * @param id el ID a excluir de la búsqueda
     * @return true si existe, false en caso contrario
     */
    boolean existsByAbbreviationAndEnterpriseIdAndIdNot(String abbreviation, String enterpriseId, Long id);
}
