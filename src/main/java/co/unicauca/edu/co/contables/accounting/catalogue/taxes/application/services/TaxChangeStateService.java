package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxChangeStateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxChangeStateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * @brief Servicio de aplicación para cambio de estado de impuestos
 *
 *        Implementa la lógica de negocio para activar o desactivar impuestos
 *        con validaciones de existencia previas.
 */
@Service
@AllArgsConstructor
public class TaxChangeStateService implements ITaxChangeStateInputPort {

    private final ITaxChangeStateOutputPort taxChangeStateOutputPort;
    private final TaxValidationService validationService;

    /**
     * @brief Cambia estado de impuesto con validación previa
     * @param id           ID del impuesto
     * @param idEnterprise ID de la empresa
     * @param status       nuevo estado (true=activo, false=inactivo)
     * @return impuesto actualizado
     */
    @Transactional
    @Override
    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "TAX", moduleName = "TAXES", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public Tax changeState(Long id, String idEnterprise, Boolean status) {
        // Validar que el impuesto existe para la empresa específica
        validationService.validateTaxExists(id, idEnterprise);

        // Cambiar el estado
        return taxChangeStateOutputPort.changeState(id, idEnterprise, status);
    }
}
