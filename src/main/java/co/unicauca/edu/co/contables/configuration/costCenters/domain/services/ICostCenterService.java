package co.unicauca.edu.co.contables.configuration.costCenters.domain.services;

import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ICostCenterService {

	CostCenter create(CostCenterCreateReq request);

	CostCenter update(CostCenterUpdateReq request);

	Page<CostCenter> findAllByEnterpriseAndStatus(String idEnterprise, Boolean status, int page, int size);

	/**
	 * Obtiene centros de costo con paginación jerárquica
	 * Mantiene las familias completas juntas
	 */
	Page<CostCenter> findAllByEnterpriseHierarchical(String idEnterprise, int page, int size);

	CostCenter findById(Long id, String idEnterprise);

	CostCenter changeState(Long id, String idEnterprise, Boolean status);

	/**
	 * Elimina físicamente un centro de costo del sistema
	 * Valida que no tenga centros de costo hijos antes de eliminar
	 * @param id ID del centro de costo
	 * @param idEnterprise ID de la empresa
	 * @return Centro de costo eliminado
	 * @throws CostCentersNotFoundException si el centro de costo no existe
	 * @throws CostCenterHasChildrenException si el centro de costo tiene hijos
	 */
	CostCenter delete(Long id, String idEnterprise);

	/**
	 * Obtiene los centros de costo activos de último nivel (código con 5 o más caracteres)
	 * @param idEnterprise ID de la empresa
	 * @return Lista de centros de costo de último nivel activos
	 */
	List<CostCenter> findActiveLastLevelCostCenters(String idEnterprise);

	/**
	 * Cuenta el total de centros de costo por empresa
	 * @param idEnterprise ID de la empresa
	 * @return Número total de centros de costo
	 */
	long countAllByEnterprise(String idEnterprise);

	/**
	 * Cuenta el total de centros de costo filtrados por estado
	 * @param idEnterprise ID de la empresa
	 * @param status Estado del centro de costo
	 * @return Número total de centros de costo con el estado especificado
	 */
	long countAllByEnterpriseAndStatus(String idEnterprise, Boolean status);
}

