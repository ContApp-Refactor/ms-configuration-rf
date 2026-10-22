package co.unicauca.edu.co.contables.inventory.products.application.ports.input;

import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import org.springframework.data.domain.Page;

/**
 * @brief Puerto de entrada para operaciones CRUD de unidades de medida
 *
 * Define contrato de operaciones disponibles para gestión de unidades de medida:
 * - Consultas paginadas con filtros de búsqueda
 * - Operaciones básicas de búsqueda y conteo
 * - Gestión de unidades por empresa
 */
public interface IUnitOfMeasureServicePort {

    /**
     * @brief Busca una unidad de medida por ID y empresa
     * @param id el ID de la unidad de medida a buscar
     * @param enterpriseId el ID de la empresa
     * @return la unidad de medida encontrada
     */
    UnitOfMeasure findByIdAndEnterpriseId(Long id, String enterpriseId);

    /**
     * @brief Cuenta el total de unidades de medida por empresa
     * @param enterpriseId el ID de la empresa
     * @return el número total de unidades de medida
     */
    long countAllUnitOfMeasuresByEntId(String enterpriseId);

    /**
     * @brief Busca unidades de medida por empresa y término de búsqueda
     * @param enterpriseId el ID de la empresa
     * @param search término de búsqueda
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (asc/desc)
     * @return página de unidades de medida que coinciden con la búsqueda
     */
    Page<UnitOfMeasure> findByEntIdAndSearch(String enterpriseId, String search, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta unidades de medida por empresa y término de búsqueda
     * @param enterpriseId el ID de la empresa
     * @param search término de búsqueda
     * @return cantidad de unidades de medida que coinciden
     */
    long countByEntIdAndSearch(String enterpriseId, String search);

    /**
     * @brief Obtiene todas las unidades de medida con ordenamiento
     * @param enterpriseId el ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (asc/desc)
     * @return página de unidades de medida ordenadas
     */
    Page<UnitOfMeasure> getAllUnitOfMeasuresByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Obtiene unidades de medida activas ordenadas por nombre
     * @param enterpriseId el ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @return página de unidades de medida activas ordenadas por nombre ascendente
     */
    Page<UnitOfMeasure> getAllActiveUnitOfMeasuresBy(String enterpriseId, int page, int size);

    /**
     * @brief Cuenta el total de unidades de medida activas por empresa
     * @param enterpriseId el ID de la empresa
     * @return el número total de unidades de medida activas
     */
    long countActiveUnitOfMeasuresByEntId(String enterpriseId);


    /**
     * @brief Crea una nueva unidad de medida
     * @param unitOfMeasure la unidad de medida a crear
     * @return la unidad de medida creada con ID asignado
     */
    UnitOfMeasure create(UnitOfMeasure unitOfMeasure);

    /**
     * @brief Actualiza una unidad de medida existente
     * @param id el ID de la unidad de medida a actualizar
     * @param enterpriseId el ID de la empresa
     * @param unitOfMeasure los datos de la unidad de medida actualizada
     * @return la unidad de medida actualizada con los nuevos datos
     */
    UnitOfMeasure update(Long id, String enterpriseId, UnitOfMeasure unitOfMeasure);

    /**
     * @brief Elimina una unidad de medida por su ID
     * @param id el ID de la unidad de medida a eliminar
     * @param enterpriseId el ID de la empresa
     */
    void deleteById(Long id, String enterpriseId);

    /**
     * @brief Cambia el estado de una unidad de medida (activar/desactivar)
     * @param id el ID de la unidad de medida cuyo estado se va a cambiar
     * @param enterpriseId el ID de la empresa
     */
    void changeState(Long id, String enterpriseId);
}
