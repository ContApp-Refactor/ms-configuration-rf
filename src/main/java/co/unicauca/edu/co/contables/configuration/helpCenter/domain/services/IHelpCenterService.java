package co.unicauca.edu.co.contables.configuration.helpCenter.domain.services;

import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterUpdateReq;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @brief Interfaz del servicio de centro de ayuda
 *
 * Define las operaciones disponibles para la gestión de registros
 * del centro de ayuda en la capa de servicios.
 */
public interface IHelpCenterService {

    HelpCenter create(HelpCenterCreateReq request);

    HelpCenter update(HelpCenterUpdateReq request);

    HelpCenter findById(Long id);

    Page<HelpCenter> findAll(int page, int size);

    Page<HelpCenter> findAll(int page, int size, String sortField, String sortOrder);

    List<HelpCenter> findAllByModule(Integer moduleId);

    HelpCenter changeState(Long id, Boolean status);

    HelpCenter delete(Long id);

    long countAll();

    /**
     * @brief Busca registros por nombre con paginación y ordenamiento
     *
     * Busca registros del centro de ayuda que contengan el término de búsqueda
     * en nombre, módulo o descripción, con paginación y opciones de ordenamiento.
     * @param search término de búsqueda
     * @param page número de página (0-based)
     * @param size tamaño de página
     * @param sortField campo para ordenar
     * @param sortOrder dirección del ordenamiento (asc/desc)
     * @return página de registros encontrados
     */
    Page<HelpCenter> findByNameContaining(String search, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta registros por término de búsqueda
     *
     * Cuenta el total de registros del centro de ayuda que contengan
     * el término de búsqueda en nombre, módulo o descripción.
     * @param search término de búsqueda
     * @return número total de registros encontrados
     */
    long countByNameContaining(String search);
}
