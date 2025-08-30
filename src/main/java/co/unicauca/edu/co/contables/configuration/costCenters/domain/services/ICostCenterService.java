package co.unicauca.edu.co.contables.configuration.costCenters.domain.services;

import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;
import org.springframework.data.domain.Page;

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
}

