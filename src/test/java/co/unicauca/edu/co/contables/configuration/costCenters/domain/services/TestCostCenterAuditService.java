package co.unicauca.edu.co.contables.configuration.costCenters.domain.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;

@Service
public class TestCostCenterAuditService {

    @Auditable(operationType = OperationType.CREATE, affectedTable = "COST_CENTER", moduleName = "COST_CENTERS")
    public CostCenter create() {

        return CostCenter.builder()
                .id(1L)
                .idEnterprise("ENT-1")
                .code("CC01")
                .name("Administracion")
                .status(true)
                .build();
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "COST_CENTER", moduleName = "COST_CENTERS")
    public CostCenter update(CostCenterUpdateReq req) {

        return CostCenter.builder()
                .id(req.getId())
                .idEnterprise(req.getIdEnterprise())
                .code(req.getCode())
                .name(req.getName())
                .status(true)
                .build();
    }

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "COST_CENTER", moduleName = "COST_CENTERS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public CostCenter changeState(Long id, String enterpriseId, Boolean status) {

        return CostCenter.builder()
                .id(id)
                .idEnterprise(enterpriseId)
                .status(status)
                .build();
    }
}
