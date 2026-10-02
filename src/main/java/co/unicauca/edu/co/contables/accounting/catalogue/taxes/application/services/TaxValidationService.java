package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services;

import java.util.List;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueInactiveException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.DuplicateTaxAccountsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.InvalidAccountDigitsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxNotFoundException;
import co.unicauca.edu.co.contables.commons.utils.StringStandardizationUtils;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

import lombok.AllArgsConstructor;

/**
 * @brief Servicio de validaciones para operaciones con impuestos
 *
 * Proporciona métodos de validación de negocio para impuestos,
 * incluyendo cuentas contables, códigos únicos y existencia de registros.
 */
@Service
@AllArgsConstructor
public class TaxValidationService {

    private final ITaxSearchOutputPort taxSearchOutputPort;
    private final IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;

    /**
     * @brief Valida cuentas de impuestos de venta y compra
     * @param salesTaxId ID de cuenta de impuesto de venta
     * @param purchaseTaxId ID de cuenta de impuesto de compra
     * @param idEnterprise ID de la empresa
     */
    public void validateAccountDigits(Long salesTaxId, Long purchaseTaxId, String idEnterprise) {
        if (salesTaxId != null) {
            AccountCatalogue salesTax = accountCatalogueSearchOutputPort
                    .getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise);
            if (salesTax == null) {
                throw new AccountCatalogueNotFoundException("La cuenta especificada no existe");
            }
            if (!Boolean.TRUE.equals(salesTax.getStatus())) {
                throw new AccountCatalogueInactiveException(
                        "La cuenta '" + salesTax.getCode() + "' está inactiva");
            }
            if (salesTax.getCode() == null || salesTax.getCode().trim().length() != 8) {
                throw new InvalidAccountDigitsException();
            }
        }

        if (purchaseTaxId != null) {
            AccountCatalogue purchaseTax = accountCatalogueSearchOutputPort
                    .getAccountCatalogueByIdAndIdEnterprise(purchaseTaxId, idEnterprise);
            if (purchaseTax == null) {
                throw new AccountCatalogueNotFoundException("La cuenta especificada no existe");
            }
            if (!Boolean.TRUE.equals(purchaseTax.getStatus())) {
                throw new AccountCatalogueInactiveException(
                        "La cuenta '" + purchaseTax.getCode() + "' está inactiva");
            }
            if (purchaseTax.getCode() == null || purchaseTax.getCode().trim().length() != 8) {
                throw new InvalidAccountDigitsException();
            }
        }
    }

    /**
     * @brief Valida que una cuenta contable esté activa
     * @param accountId ID de la cuenta
     * @param idEnterprise ID de la empresa
     */
    public void validateAccountActive(Long accountId, String idEnterprise) {
        if (accountId != null) {
            AccountCatalogue account = accountCatalogueSearchOutputPort
                    .getAccountCatalogueByIdAndIdEnterprise(accountId, idEnterprise);
            if (account == null) {
                throw new AccountCatalogueNotFoundException("La cuenta especificada no existe");
            }
            if (!Boolean.TRUE.equals(account.getStatus())) {
                throw new AccountCatalogueInactiveException("La cuenta '" + account.getCode() + "' está inactiva");
            }
        }
    }

    /**
     * @brief Valida unicidad de código de impuesto por empresa
     * @param code código del impuesto
     * @param idEnterprise ID de la empresa
     */
    public void validateTaxCodeNotExists(String code, String idEnterprise) {
        if (code == null || code.trim().isEmpty()) {
            return; // No validar códigos vacíos o nulos
        }

        String normalizedCode = StringStandardizationUtils.standardizeCode(code);

        // Buscar todos los impuestos de la empresa y comparar códigos normalizados
        List<Tax> allTaxes = taxSearchOutputPort.getTaxesByEnterprise(idEnterprise);

        for (Tax tax : allTaxes) {
            if (tax.getCode() != null) {
                String existingNormalizedCode = StringStandardizationUtils.standardizeCode(tax.getCode());
                if (normalizedCode.equals(existingNormalizedCode)) {
                    throw new TaxAlreadyExistsException(
                            "Ya existe un impuesto con código '" + code + "'");
                }
            }
        }
    }

    /**
     * @brief Valida unicidad de código excluyendo un ID específico
     * @param code código del impuesto
     * @param idEnterprise ID de la empresa
     * @param excludeId ID del impuesto a excluir de la validación
     */
    public void validateTaxCodeNotExistsExcludingId(String code, String idEnterprise, Long excludeId) {
        if (code == null || code.trim().isEmpty()) {
            return; // No validar códigos vacíos o nulos
        }

        String normalizedCode = StringStandardizationUtils.standardizeCode(code);

        // Buscar todos los impuestos de la empresa y comparar códigos normalizados
        List<Tax> allTaxes = taxSearchOutputPort.getTaxesByEnterprise(idEnterprise);

        for (Tax tax : allTaxes) {
            // Excluir el impuesto que se está editando
            if (!tax.getId().equals(excludeId) && tax.getCode() != null) {
                String existingNormalizedCode = StringStandardizationUtils.standardizeCode(tax.getCode());
                if (normalizedCode.equals(existingNormalizedCode)) {
                    throw new TaxAlreadyExistsException(
                            "Ya existe un impuesto con código '" + code + "'");
                }
            }
        }
    }

    /**
     * @brief Valida existencia de impuesto por código
     * @param code código del impuesto
     * @param idEnterprise ID de la empresa
     */
    public void validateTaxExists(String code, String idEnterprise) {
        Tax tax = taxSearchOutputPort.getTax(code, idEnterprise);
        if (tax == null) {
            throw new TaxNotFoundException(
                    "No se encontró un impuesto con código '" + code + "'");
        }
    }

    /**
     * @brief Valida existencia de impuesto por ID
     * @param id ID del impuesto
     * @param idEnterprise ID de la empresa
     */
    public void validateTaxExists(Long id, String idEnterprise) {
        Tax tax = taxSearchOutputPort.getTaxByIdAndEnterprise(id, idEnterprise);
        if (tax == null) {
            throw new TaxNotFoundException(
                    "No se encontró un impuesto con ID '" + id + "'");
        }
    }

    /**
     * @brief Valida que las cuentas de impuesto de venta e impuesto de compra sean diferentes.
     *
     * @param salesTaxId ID de la cuenta de impuesto de venta
     * @param purchaseTaxId ID de la cuenta de impuesto de compra
     */
    public void validateDifferentTaxAccounts(Long salesTaxId, Long purchaseTaxId) {
        if (salesTaxId != null && purchaseTaxId != null && salesTaxId.equals(purchaseTaxId)) {
            throw new DuplicateTaxAccountsException(
                    "Las cuentas de impuesto de venta e impuesto de compra deben ser diferentes");
        }
    }
}
