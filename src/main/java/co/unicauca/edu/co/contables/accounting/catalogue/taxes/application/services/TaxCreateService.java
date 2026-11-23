package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services;

import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxCreateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxCreateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @brief Servicio de aplicación para creación de impuestos
 *
 *        Implementa la lógica de negocio para crear nuevos impuestos
 *        con validaciones de unicidad y cuentas contables.
 */
@Service
@AllArgsConstructor
@Data
public class TaxCreateService implements ITaxCreateInputPort {
    private final ITaxCreateOutputPort taxCreateOutputPort;
    private final TaxValidationService taxValidationService;

    /**
     * @brief Crea nueva entrada de impuesto con validaciones
     * @param tax datos del impuesto a crear
     * @return impuesto creado
     */
    @Override
    @Transactional
    @Auditable(operationType = OperationType.CREATE, affectedTable = "TAX", moduleName = "TAXES")
    public Tax createTax(TaxDTO tax) {
        // Validar unicidad del código usando normalización
        taxValidationService.validateTaxCodeNotExists(tax.getCode(), tax.getIdEnterprise());

        // Validar cuentas de impuesto
        taxValidationService.validateAccountDigits(tax.getSalesTaxId(), tax.getPurchaseTaxId(), tax.getIdEnterprise());
        taxValidationService.validateDifferentTaxAccounts(tax.getSalesTaxId(), tax.getPurchaseTaxId());

        return taxCreateOutputPort.createTax(tax);
    }
}
