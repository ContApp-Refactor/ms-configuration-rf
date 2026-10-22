package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxDeleteInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxDeleteOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

import jakarta.transaction.Transactional;

import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxInUseException;

/**
 * @brief Servicio de aplicación para eliminación de impuestos
 *
 *        Implementa la lógica de negocio para eliminar impuestos
 *        con validaciones de existencia previas.
 */
@Service
@AllArgsConstructor
public class TaxDeleteService implements ITaxDeleteInputPort {
    private final ITaxDeleteOutputPort taxDeleteOutputPort;
    private final ITaxSearchOutputPort taxSearchOutputPort;
    private final TaxValidationService taxValidationService;

    /**
     * @brief Elimina impuesto con validación previa
     * @param id           ID del impuesto a eliminar
     * @param idEnterprise ID de la empresa
     * @return true si se eliminó con éxito, false de lo contrario
     */
    @Override
    @Transactional
    @Auditable(operationType = OperationType.DELETE, affectedTable = "TAX", moduleName = "TAXES", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public boolean deleteByCode(long id, String idEnterprise) {
        // Validar que el impuesto existe para la empresa específica
        taxValidationService.validateTaxExists(Long.valueOf(id), idEnterprise);

        // Obtener el impuesto para validar uso
        Tax existingTax = taxSearchOutputPort.getTaxByIdAndEnterprise(id, idEnterprise);

        // Validar que el impuesto no tenga movimientos contables registrados
        if (existingTax.isInUse()) {
            throw new TaxInUseException(existingTax.getCode(), false); // false indica operación de eliminación
        }

        return taxDeleteOutputPort.deleteByCode(id, idEnterprise);
    }
}
