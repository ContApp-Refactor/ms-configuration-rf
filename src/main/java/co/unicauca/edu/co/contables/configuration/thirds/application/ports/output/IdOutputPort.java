package co.unicauca.edu.co.contables.configuration.thirds.application.ports.output;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @brief Puerto de salida para gestión de tipos de tercero e identificación
 *
 * Define el contrato para todas las operaciones de persistencia
 * relacionadas con tipos de tercero y tipos de identificación.
 */
public interface IdOutputPort {
    /**
     * @brief Guarda un tipo de tercero
     * @param thirdType El tipo de tercero a guardar
     * @return El tipo de tercero guardado
     */
    ThirdType saveThirdType(ThirdType thirdType);

    /**
     * @brief Obtiene todos los tipos de tercero
     * @param entId El id de la empresa
     * @return La lista de tipos de tercero
     */
    List<ThirdType> getALLThirdTypes(String entId);

    /**
     * @brief Guarda un tipo de identificación
     * @param typeId El tipo de identificación a guardar
     * @return El tipo de identificación guardado
     */
    TypeId saveTypeId(TypeId typeId);

    /**
     * @brief Obtiene todos los tipos de identificación
     * @param entId El id de la empresa
     * @return La lista de tipos de identificación
     */
    List<TypeId> getAllTypeIds(String entId);

    /**
     * @brief Actualiza un tipo de identificación
     * @param typeId El tipo de identificación a actualizar
     * @return El tipo de identificación actualizado
     */
    TypeId updateTypeId(TypeId typeId);

    /**
     * @brief Actualiza un tipo de tercero
     * @param thirdType El tipo de tercero a actualizar
     * @return El tipo de tercero actualizado
     */
    ThirdType updateThirdType(ThirdType thirdType);
    
    /**
     * @brief Verifica existencia de tipo de identificación por ID
     * @param typeIdId El ID del tipo de identificación
     * @return true si existe, false en caso contrario
     */
    boolean existsTypeIdById(Long typeIdId);

    /**
     * @brief Verifica existencia de tipo de tercero por ID
     * @param thirdTypeId El ID del tipo de tercero
     * @return true si existe, false en caso contrario
     */
    boolean existsThirdTypeById(Long thirdTypeId);

    /**
     * @brief Obtiene tipo de identificación completo por ID
     * @param typeIdId El ID del tipo de identificación
     * @return El tipo de identificación completo o null si no existe
     */
    TypeId getTypeIdById(Long typeIdId);

    /**
     * @brief Obtiene tipo de tercero completo por ID
     * @param thirdTypeId El ID del tipo de tercero
     * @return El tipo de tercero completo o null si no existe
     */
    ThirdType getThirdTypeById(Long thirdTypeId);

    /**
     * @brief Elimina un tipo de tercero del sistema
     * @param thirdTypeId El ID del tipo de tercero a eliminar
     * @param entId El ID de la empresa
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean deleteThirdType(Long thirdTypeId, String entId);

    /**
     * @brief Verifica si tipo de tercero está en uso
     * @param thirdTypeId El ID del tipo de tercero
     * @param entId El ID de la empresa
     * @return true si está en uso, false en caso contrario
     */
    boolean isThirdTypeInUse(Long thirdTypeId, String entId);

    /**
     * @brief Verifica si tipo de tercero tiene terceros con movimientos contables
     * @param thirdTypeId El ID del tipo de tercero
     * @param entId El ID de la empresa
     * @return true si tiene terceros con movimientos contables, false en caso contrario
     */
    boolean hasThirdTypeThirdsWithMovements(Long thirdTypeId, String entId);

    /**
     * @brief Elimina un tipo de identificación del sistema
     * @param typeIdId El ID del tipo de identificación a eliminar
     * @param entId El ID de la empresa
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean deleteTypeId(Long typeIdId, String entId);

    /**
     * @brief Verifica si tipo de identificación está en uso
     * @param typeIdId El ID del tipo de identificación
     * @param entId El ID de la empresa
     * @return true si está en uso, false en caso contrario
     */
    boolean isTypeIdInUse(Long typeIdId, String entId);

    /**
     * @brief Verifica si tipo de identificación tiene terceros con movimientos contables
     * @param typeIdId El ID del tipo de identificación
     * @param entId El ID de la empresa
     * @return true si tiene terceros con movimientos contables, false en caso contrario
     */
    boolean hasTypeIdThirdsWithMovements(Long typeIdId, String entId);
    
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
     * @brief Cuenta tipos de identificación por empresa y término de búsqueda
     * @param entId El id de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de tipos de identificación que coinciden
     */
    long countByEntIdAndSearch(String entId, String search);

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
     * @brief Obtiene tipos de identificación activos con paginación
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de tipos de identificación activos ordenados por nombre
     */
    Page<TypeId> getAllActiveTypeIds(String entId, int page, int size);

    /**
     * @brief Cuenta tipos de identificación activos por empresa
     * @param entId El id de la empresa
     * @return Cantidad de tipos de identificación activos
     */
    long countActiveByEntId(String entId);

    /**
     * @brief Obtiene tipos de tercero activos con paginación
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de tipos de tercero activos ordenados por nombre
     */
    Page<ThirdType> getAllActiveThirdTypes(String entId, int page, int size);

    /**
     * @brief Cuenta tipos de tercero activos por empresa
     * @param entId El id de la empresa
     * @return Cantidad de tipos de tercero activos
     */
    long countActiveThirdTypesByEntId(String entId);
}
