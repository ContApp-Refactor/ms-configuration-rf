package co.unicauca.edu.co.contables.configuration.helpCenter.domain.services;

import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterUpdateReq;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IHelpCenterService {

    HelpCenter create(HelpCenterCreateReq request);

    HelpCenter update(HelpCenterUpdateReq request);

    HelpCenter findById(Long id, String idEnterprise);

    Page<HelpCenter> findAllByEnterprise(String idEnterprise, int page, int size);

    Page<HelpCenter> findAllByEnterprise(String idEnterprise, int page, int size, String sortField, String sortOrder);

    /**
     * Obtiene todos los registros de ayuda por ID de módulo y empresa
     * @param moduleId ID del módulo
     * @param idEnterprise ID de la empresa
     * @return Lista de registros de ayuda del módulo
     */
    List<HelpCenter> findAllByModuleAndEnterprise(Integer moduleId, String idEnterprise);

    HelpCenter changeState(Long id, String idEnterprise, Boolean status);

    HelpCenter delete(Long id, String idEnterprise);

    /**
     * Cuenta el total de registros de ayuda por empresa
     * @param idEnterprise ID de la empresa
     * @return Número total de registros de ayuda
     */
    long countAllByEnterprise(String idEnterprise);

    /**
     * Busca registros de ayuda por empresa y nombre (búsqueda parcial)
     * @param idEnterprise ID de la empresa
     * @param search Término de búsqueda
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de registros de ayuda que coinciden con la búsqueda
     */
    Page<HelpCenter> findByEnterpriseAndNameContaining(String idEnterprise, String search, int page, int size, String sortField, String sortOrder);

    /**
     * Cuenta registros de ayuda por empresa y nombre (búsqueda parcial)
     * @param idEnterprise ID de la empresa
     * @param search Término de búsqueda
     * @return Número total de registros de ayuda que coinciden con la búsqueda
     */
    long countByEnterpriseAndNameContaining(String idEnterprise, String search);
}
