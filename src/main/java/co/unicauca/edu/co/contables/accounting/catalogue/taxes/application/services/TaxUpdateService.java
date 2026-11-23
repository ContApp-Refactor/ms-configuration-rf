package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxUpdateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxInUseException;

import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

/**
 * @brief Servicio de aplicación para actualización de impuestos
 *
 *        Implementa la lógica de negocio para actualizar impuestos existentes
 *        con validaciones de unicidad y cuentas contables.
 */
@Service
@AllArgsConstructor
@Data
public class TaxUpdateService implements ITaxUpdateInputPort {
    private final ITaxUpdateOutputPort taxUpdateOutputPort;
    private final ITaxSearchOutputPort taxSearchOutputPort;
    private final TaxValidationService taxValidationService;

    /**
     * @brief Actualiza impuesto existente con validaciones
     * @param taxDTO datos actualizados del impuesto
     * @param id     identificador del impuesto a actualizar
     * @return impuesto actualizado
     */
    @Override
    @Transactional
    @Auditable(operationType = OperationType.UPDATE, affectedTable = "TAX", moduleName = "TAXES")
    public Tax update(TaxDTO taxDTO, long id) {
        // Validar que el impuesto existe
        taxValidationService.validateTaxExists(id, taxDTO.getIdEnterprise());

        // Obtener el impuesto para validar uso
        Tax existingTax = taxSearchOutputPort.getTaxByIdAndEnterprise(id, taxDTO.getIdEnterprise());

        // Validar que el impuesto no tenga movimientos contables registrados
        if (existingTax.isInUse()) {
            throw new TaxInUseException(existingTax.getCode(), true); // true indica operación de edición
        }

        // Validar unicidad del código usando normalización, excluyendo el registro
        // actual
        taxValidationService.validateTaxCodeNotExistsExcludingId(taxDTO.getCode(), taxDTO.getIdEnterprise(), id);

        // Validar cuentas de impuesto
        taxValidationService.validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(),
                taxDTO.getIdEnterprise());
        taxValidationService.validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());

        return taxUpdateOutputPort.update(taxDTO, id);
    }
}
