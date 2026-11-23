package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation;

import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.repository.BankAccountRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.repository.PaymentMethodRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueAssociatedWithBankAccountException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueAssociatedWithPaymentMethodException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueAssociatedWithTaxException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueDescriptionAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.InvalidAccountCodeException;

import lombok.AllArgsConstructor;

/**
 * @brief Servicio de validaciones para cuentas contables
 *
 * Centraliza todas las reglas de validación de negocio para cuentas contables,
 * incluyendo códigos, descripciones, jerarquías y asociaciones.
 */
@Service
@AllArgsConstructor
public class AccountCatalogueValidationService {

    private final IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;
    private final BankAccountRepository bankAccountRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ITaxRepository taxRepository;

    /**
     * @brief Valida código de cuenta según reglas de negocio (longitud específica)     *
     * @param code código de cuenta a validar
     * @throws InvalidAccountCodeException si el código no cumple con las reglas
     */
    public void validateAccountCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidAccountCodeException("El código de cuenta no puede estar vacío");
        }

        String trimmedCode = code.trim();

        // Verificar que solo contenga dígitos
        if (!Pattern.matches("^\\d+$", trimmedCode)) {
            throw new InvalidAccountCodeException("El código de cuenta debe contener solo dígitos");
        }

        int length = trimmedCode.length();
        if (length != 1 && length != 2 && length != 4 && length != 6 && length != 8) {
            throw new InvalidAccountCodeException(
                    "El código de cuenta debe tener exactamente 1, 2, 4, 6 u 8 dígitos. Longitud actual: " + length);
        }

        // Validar que códigos de 1 dígito no sean "0" (solo permitidos 1-9)
        if (length == 1 && trimmedCode.equals("0")) {
            throw new InvalidAccountCodeException(
                    "El código de cuenta no puede ser 0. Los valores permitidos son del 1 al 9");
        }
    }

    /**
     * @brief Valida unicidad de código de cuenta por empresa     *
     * @param code código de cuenta a verificar
     * @param idEnterprise ID de empresa para aislamiento de datos
     * @throws AccountCatalogueAlreadyExistsException si ya existe cuenta con ese código
     */
    public void validateAccountDoesNotExist(String code, String idEnterprise) {
        AccountCatalogue existingAccount = accountCatalogueSearchOutputPort.getAccountCatalogueByCode(code,
                idEnterprise);
        if (existingAccount != null) {
            throw new AccountCatalogueAlreadyExistsException(
                    "Ya existe una cuenta con el código '" + code + "'");
        }
    }

    /**
     * @brief Valida unicidad de código excluyendo cuenta específica (para actualizaciones)
     * @param code código de cuenta a verificar
     * @param idEnterprise ID de empresa para aislamiento de datos
     * @param excludeId ID de cuenta a excluir de la validación
     * @throws AccountCatalogueAlreadyExistsException si ya existe otra cuenta con ese código
     */
    public void validateAccountDoesNotExistExcluding(String code, String idEnterprise, Long excludeId) {
        if (code == null || code.trim().isEmpty()) {
            return; // La validación de código vacío se maneja en validateAccountCode
        }

        if (idEnterprise == null || idEnterprise.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empresa no puede estar vacío para validación de duplicados");
        }

        if (excludeId == null) {
            throw new IllegalArgumentException("El ID a excluir no puede ser null para validación de duplicados");
        }

        AccountCatalogue existingAccount = accountCatalogueSearchOutputPort.getAccountCatalogueByCode(code.trim(),
                idEnterprise);
        if (existingAccount != null && !existingAccount.getId().equals(excludeId)) {
            throw new AccountCatalogueAlreadyExistsException(
                    "Ya existe otra cuenta con el código '" + code.trim() + "'");
        }
    }

    /**
     * @brief Valida existencia de cuenta por código y retorna instancia     *
     * @param code código de cuenta a buscar
     * @param idEnterprise ID de empresa para aislamiento de datos
     * @return instancia de cuenta encontrada
     * @throws AccountCatalogueNotFoundException si no existe cuenta con ese código
     */
    public AccountCatalogue validateAccountExists(String code, String idEnterprise) {
        AccountCatalogue account = accountCatalogueSearchOutputPort.getAccountCatalogueByCode(code, idEnterprise);
        if (account == null) {
            throw new AccountCatalogueNotFoundException(
                    "No se encontró una cuenta con el código '" + code + "'");
        }
        return account;
    }

    /**
     * @brief Valida existencia de cuenta por ID único y retorna instancia
     * @param id ID único de la cuenta en base de datos
     * @param idEnterprise ID de empresa para validación de pertenencia
     * @return instancia de cuenta encontrada
     * @throws AccountCatalogueNotFoundException si no existe cuenta con ese ID
     */
    public AccountCatalogue validateAccountExistsByIdAndEnterprise(Long id, String idEnterprise) {
        AccountCatalogue account = accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(id,
                idEnterprise);
        if (account == null) {
            throw new AccountCatalogueNotFoundException(
                    "No se encontró una cuenta con el ID '" + id + "'");
        }
        return account;
    }

    /**
     * @brief Valida que cuenta no tenga asociaciones con impuestos
     * @param account instancia de cuenta a validar
     * @throws AccountCatalogueAssociatedWithTaxException si tiene asociaciones con impuestos
     */
    public void validateAccountNotAssociatedWithTaxes(AccountCatalogue account) {
        // Consultar directamente la base de datos para verificar si hay impuestos asociados
        boolean hasAssociatedTaxes = taxRepository.existsBySalesTaxCodeOrPurchaseTaxCode(account.getCode(), account.getIdEnterprise());

        if (hasAssociatedTaxes) {
            throw new AccountCatalogueAssociatedWithTaxException(
                    "No se puede eliminar la cuenta " + account.getCode()
                            + " porque está asociada a uno o más impuestos.");
        }
    }

    /**
     * @brief Valida recursivamente jerarquía completa contra asociaciones con impuestos
     * @param account cuenta padre cuya jerarquía completa debe validarse
     * @throws AccountCatalogueAssociatedWithTaxException si cualquier cuenta en jerarquía tiene asociaciones
     */
    public void validateAccountAndChildrenNotAssociatedWithTaxes(AccountCatalogue account) {
        // Validar la cuenta principal
        validateAccountNotAssociatedWithTaxes(account);

        // Validar recursivamente todas las cuentas hijas
        if (account.getChildren() != null && !account.getChildren().isEmpty()) {
            for (AccountCatalogue child : account.getChildren()) {
                validateAccountAndChildrenNotAssociatedWithTaxes(child);
            }
        }
    }

    /**
     * @brief Valida que cuenta no tenga asociaciones con cuentas bancarias
     * @param account instancia de cuenta a validar
     * @throws AccountCatalogueAssociatedWithBankAccountException si tiene asociaciones con cuentas bancarias
     */
    public void validateAccountNotAssociatedWithBankAccounts(AccountCatalogue account) {
        boolean hasBankAccounts = bankAccountRepository.existsByAccountingAccountIdAndIdEnterprise(
                account.getId(), account.getIdEnterprise());

        if (hasBankAccounts) {
            throw new AccountCatalogueAssociatedWithBankAccountException(
                    "No se puede eliminar la cuenta '" + account.getCode()
                            + "' porque está asociada a una o más cuentas bancarias.");
        }
    }

    /**
     * @brief Valida recursivamente jerarquía completa contra asociaciones con cuentas bancarias
     * @param account cuenta padre cuya jerarquía completa debe validarse
     * @throws AccountCatalogueAssociatedWithBankAccountException si cualquier cuenta en jerarquía tiene asociaciones
     */
    public void validateAccountAndChildrenNotAssociatedWithBankAccounts(AccountCatalogue account) {
        // Validar la cuenta principal
        validateAccountNotAssociatedWithBankAccounts(account);

        // Validar recursivamente todas las cuentas hijas
        if (account.getChildren() != null && !account.getChildren().isEmpty()) {
            for (AccountCatalogue child : account.getChildren()) {
                validateAccountAndChildrenNotAssociatedWithBankAccounts(child);
            }
        }
    }

    /**
     * @brief Valida que cuenta no tenga asociaciones con métodos de pago
     * @param account instancia de cuenta a validar
     * @throws AccountCatalogueAssociatedWithPaymentMethodException si tiene asociaciones con métodos de pago
     */
    public void validateAccountNotAssociatedWithPaymentMethods(AccountCatalogue account) {
        boolean hasPaymentMethods = paymentMethodRepository.existsByAccountingAccountIdAndIdEnterprise(
                account.getId(), account.getIdEnterprise());

        if (hasPaymentMethods) {
            throw new AccountCatalogueAssociatedWithPaymentMethodException(
                    "No se puede eliminar la cuenta '" + account.getCode()
                            + "' porque está asociada a uno o más métodos de pago.");
        }
    }

    /**
     * @brief Valida recursivamente jerarquía completa contra asociaciones con métodos de pago
     * @param account cuenta padre cuya jerarquía completa debe validarse
     * @throws AccountCatalogueAssociatedWithPaymentMethodException si cualquier cuenta en jerarquía tiene asociaciones
     */
    public void validateAccountAndChildrenNotAssociatedWithPaymentMethods(AccountCatalogue account) {
        // Validar la cuenta principal
        validateAccountNotAssociatedWithPaymentMethods(account);

        // Validar recursivamente todas las cuentas hijas
        if (account.getChildren() != null && !account.getChildren().isEmpty()) {
            for (AccountCatalogue child : account.getChildren()) {
                validateAccountAndChildrenNotAssociatedWithPaymentMethods(child);
            }
        }
    }


    /**
     * @brief Valida formato y contenido de descripción de cuenta contable
     * @param description descripción de cuenta a validar
     * @throws InvalidAccountCodeException si la descripción no cumple criterios de formato
     */
    public void validateAccountDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidAccountCodeException("La descripción de la cuenta no puede estar vacía");
        }

        String trimmedDescription = description.trim();

        // Validar que no tenga múltiples espacios consecutivos
        if (trimmedDescription.contains("  ")) {
            throw new InvalidAccountCodeException(
                    "La descripción de la cuenta no puede contener múltiples espacios consecutivos");
        }

        // Permitir alfanuméricos, espacios, guiones, puntos, comas, paréntesis
        if (!Pattern.matches("^[a-zA-Z0-9\\s\\-.,()áéíóúÁÉÍÓÚñÑ]+$", trimmedDescription)) {
            throw new InvalidAccountCodeException(
                    "La descripción de la cuenta contiene caracteres no válidos. " +
                            "Solo se permiten letras, números, espacios y los siguientes caracteres especiales: - . , ( )");
        }

        // Validar longitud mínima y máxima
        if (trimmedDescription.length() < 2) {
            throw new InvalidAccountCodeException("La descripción de la cuenta debe tener al menos 2 caracteres");
        }

        if (trimmedDescription.length() > 100) {
            throw new InvalidAccountCodeException("La descripción de la cuenta no puede exceder 100 caracteres");
        }
    }

    /**
     * @brief Valida unicidad de descripción de cuenta por empresa (case-insensitive)
     * @param description descripción de cuenta a verificar
     * @param idEnterprise ID de empresa para aislamiento de datos
     * @throws AccountCatalogueDescriptionAlreadyExistsException si ya existe cuenta con esa descripción
     */
    public void validateAccountDescriptionDoesNotExist(String description, String idEnterprise) {
        if (description == null || description.trim().isEmpty()) {
            return; // La validación de descripción vacía se maneja en validateAccountDescription
        }

        AccountCatalogue existingAccount = accountCatalogueSearchOutputPort
                .getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise(description.trim(), idEnterprise);
        if (existingAccount != null) {
            throw new AccountCatalogueDescriptionAlreadyExistsException(
                    "Ya existe una cuenta con la descripción '" + description.trim() + "'");
        }
    }

    /**
     * @brief Valida unicidad de descripción excluyendo cuenta específica (para actualizaciones)
     * @param description descripción de cuenta a verificar
     * @param idEnterprise ID de empresa para aislamiento de datos
     * @param excludeId ID de cuenta a excluir de la validación
     * @throws AccountCatalogueDescriptionAlreadyExistsException si ya existe otra cuenta con esa descripción
     */
    public void validateAccountDescriptionDoesNotExistExcluding(String description, String idEnterprise,
            Long excludeId) {
        if (description == null || description.trim().isEmpty()) {
            return; // La validación de descripción vacía se maneja en validateAccountDescription
        }

        if (idEnterprise == null || idEnterprise.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El ID de empresa no puede estar vacío para validación de descripción duplicada");
        }

        if (excludeId == null) {
            throw new IllegalArgumentException(
                    "El ID a excluir no puede ser null para validación de descripción duplicada");
        }

        AccountCatalogue existingAccount = accountCatalogueSearchOutputPort
                .getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise(description.trim(), idEnterprise);
        if (existingAccount != null && !existingAccount.getId().equals(excludeId)) {
            throw new AccountCatalogueDescriptionAlreadyExistsException(
                    "Ya existe otra cuenta con la descripción '" + description.trim() + "'");
        }
    }

    /**
     * @brief Valida restricciones de campos opcionales (crossing/costCenter) a cuentas auxiliares
     * @param accountCatalogue cuenta con campos opcionales a validar
     * @throws InvalidAccountCodeException si campos opcionales se usan en cuentas no auxiliares
     */
    public void validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(AccountCatalogue accountCatalogue) {
        boolean hasCrossing = accountCatalogue.getCrossing() != null && accountCatalogue.getCrossing();
        boolean hasCostCenter = accountCatalogue.getCostCenter() != null && accountCatalogue.getCostCenter();

        if (hasCrossing || hasCostCenter) {
            String code = accountCatalogue.getCode();
            if (code == null || code.trim().length() != 8) {
                String invalidFields = "";
                if (hasCrossing && hasCostCenter) {
                    invalidFields = "crossing y costCenter";
                } else if (hasCrossing) {
                    invalidFields = "crossing";
                } else {
                    invalidFields = "costCenter";
                }

                throw new InvalidAccountCodeException(
                        "Los campos " + invalidFields
                                + " solo pueden ser establecidos en cuentas auxiliares (8 dígitos exactamente). " +
                                "Código actual: '" + code + "' tiene " + (code != null ? code.trim().length() : 0)
                                + " dígitos.");
            }
        }
    }

    /**
     * @brief Valida dependencia de costCenter con estado financiero Estado de Resultados
     * @param accountCatalogue cuenta con costCenter y estado financiero a validar
     * @throws InvalidAccountCodeException si costCenter=true pero estado financiero incorrecto
     */
    public void validateCostCenterRequiresIncomeStatement(AccountCatalogue accountCatalogue) {
        boolean hasCostCenter = accountCatalogue.getCostCenter() != null && accountCatalogue.getCostCenter();

        if (hasCostCenter) {
            String code = accountCatalogue.getCode();
            // Solo aplicar esta validación a cuentas auxiliares (8 dígitos)
            if (code != null && code.trim().length() == 8) {
                FinancialStatusEnum financialStatus = accountCatalogue.getFinancialStatus();

                if (financialStatus == null || financialStatus != FinancialStatusEnum.INCOMESTATEMENT) {
                    throw new InvalidAccountCodeException(
                            "El centro de costo solo puede ser establecido cuando el estado financiero " +
                                    "sea 'Estado de Resultados'. Estado financiero actual: " +
                                    (financialStatus != null ? financialStatus.getState() : "No definido"));
                }
            }
        }
    }

    /**
     * @brief Valida integridad jerárquica de códigos padre-hijo
     * @param accountCode código de cuenta hija a validar
     * @param parentCode código de cuenta padre como referencia
     * @throws InvalidAccountCodeException si el código no respeta jerarquía padre-hijo
     */
    public void validateParentCodePrefix(String accountCode, String parentCode) {
        if (accountCode == null) {
            throw new InvalidAccountCodeException("El código de la cuenta no puede ser nulo");
        }
        if (parentCode == null) {
            throw new InvalidAccountCodeException("El código del padre no puede ser nulo");
        }

        if (!accountCode.startsWith(parentCode)) {
            throw new InvalidAccountCodeException(
                    "El código de la cuenta debe comenzar con el código del padre. " +
                            "Padre: '" + parentCode + "', Cuenta: '" + accountCode + "'");
        }
    }
}
