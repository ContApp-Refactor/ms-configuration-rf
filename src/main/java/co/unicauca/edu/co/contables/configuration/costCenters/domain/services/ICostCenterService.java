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

	CostCenter softDelete(Long id, String idEnterprise);

	/**
	 * Obtiene los centros de costo activos de último nivel (código con 5 o más caracteres)
	 * @param idEnterprise ID de la empresa
	 * @return Lista de centros de costo de último nivel activos
	 */
	List<CostCenter> findActiveLastLevelCostCenters(String idEnterprise);
}

