package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

@Service
public class TestTaxesAuditService {

    @Auditable(operationType = OperationType.CREATE, affectedTable = "TAX", moduleName = "TAXES")
    public Tax create() {
        Tax tax = new Tax();
        tax.setId(1L);
        tax.setIdEnterprise("ENT-1");
        tax.setCode("T-001");
        tax.setDescription("IVA 19%");
        tax.setStatus(true);
        return tax;
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "TAX", moduleName = "TAXES")
    public Tax update(TaxDTO taxDTO, long id) {
        Tax tax = new Tax();
        tax.setId(id);
        tax.setIdEnterprise(taxDTO.getIdEnterprise());
        tax.setCode("T-001");
        tax.setDescription(taxDTO.getDescription());
        tax.setStatus(true);
        return tax;
    }

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "TAX", moduleName = "TAXES", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public Tax changeState(Long id, String entId, Boolean status) {
        Tax tax = new Tax();
        tax.setId(id);
        tax.setIdEnterprise(entId);
        tax.setCode("T-001");
        tax.setDescription("IVA 19%");
        tax.setStatus(status);
        return tax;
    }

    @Auditable(operationType = OperationType.DELETE, affectedTable = "TAX", moduleName = "TAXES", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public boolean deleteByCode(long id, String entId) {
        // podría devolver boolean en tu servicio real, aquí solo interesa que el aspect
        // se dispare
        return true;
    }
}
