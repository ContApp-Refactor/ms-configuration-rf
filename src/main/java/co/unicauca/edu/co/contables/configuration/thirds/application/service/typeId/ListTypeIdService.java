package co.unicauca.edu.co.contables.configuration.thirds.application.service.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.ListTypeIdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListTypeIdService implements ListTypeIdUseCase {

    private final IdOutputPort idOutputPort;

    /**
     * @brief Obtiene todos los tipos de identificación para una empresa específica
     * @param entId identificador de la empresa
     * @return lista de tipos de identificación disponibles
     */
    @Override
    public List<TypeId> getAllTypeId(String entId) {
        return idOutputPort.getAllTypeIds(entId);
    }

    /**
     * @brief Obtiene todos los tipos de identificación con paginación y ordenamiento personalizado
     * @param entId identificador de la empresa
     * @param page número de página (0-based)
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (ascendente/descendente)
     * @return página de tipos de identificación
     */
    @Override
    public Page<TypeId> getAllTypeIdsWithSort(String entId, int page, int size, String sortField, String sortOrder) {
        return idOutputPort.getAllTypeIdsWithSort(entId, page, size, sortField, sortOrder);
    }

    /**
     * @brief Obtiene todos los tipos de identificación activos con paginación
     * @param entId identificador de la empresa
     * @param page número de página (0-based)
     * @param size tamaño de página
     * @return página de tipos de identificación activos ordenados por nombre
     */
    @Override
    public Page<TypeId> getAllActiveTypeIds(String entId, int page, int size) {
        return idOutputPort.getAllActiveTypeIds(entId, page, size);
    }

    /**
     * @brief Busca tipos de identificación por empresa y término de búsqueda
     * @param entId identificador de la empresa
     * @param search término de búsqueda
     * @param page número de página (0-based)
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (ascendente/descendente)
     * @return página de tipos de identificación que coinciden con la búsqueda
     */
    @Override
    public Page<TypeId> findByEntIdAndSearch(String entId, String search, int page, int size, String sortField, String sortOrder) {
        return idOutputPort.findByEntIdAndSearch(entId, search, page, size, sortField, sortOrder);
    }
    
    /**
     * @brief Cuenta tipos de identificación por empresa
     * @param entId identificador de la empresa
     * @return cantidad total de tipos de identificación
     */
    @Override
    public long countByEntId(String entId) {
        return idOutputPort.countByEntId(entId);
    }
    
    /**
     * @brief Cuenta tipos de identificación activos por empresa
     * @param entId identificador de la empresa
     * @return cantidad de tipos de identificación activos
     */
    @Override
    public long countActiveByEntId(String entId) {
        return idOutputPort.countActiveByEntId(entId);
    }
    
    /**
     * @brief Cuenta tipos de identificación por empresa y término de búsqueda
     * @param entId identificador de la empresa
     * @param search término de búsqueda
     * @return cantidad de tipos de identificación que coinciden con la búsqueda
     */
    @Override
    public long countByEntIdAndSearch(String entId, String search) {
        return idOutputPort.countByEntIdAndSearch(entId, search);
    }}
