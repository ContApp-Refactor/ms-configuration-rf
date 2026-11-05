package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueUpdateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueInUseException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @brief Servicio para operaciones de actualización de cuentas contables
 *
 *        Maneja la actualización de cuentas existentes del catálogo con
 *        validaciones
 *        de integridad y reglas de negocio específicas para modificaciones.
 */
@Service
@AllArgsConstructor
@Transactional
public class AccountCatalogueUpdateService implements IAccountCatalogueUpdateInputPort {

    private final IAccountCatalogueUpdateOutputPort accountCatalogueUpdateOutputport;
    private final AccountCatalogueValidationService validationService;

    /**
     * @brief Actualiza cuenta contable con validaciones de integridad completas
     * @param id               ID de la cuenta a actualizar
     * @param accountCatalogue datos actualizados con todas las validaciones
     *                         aplicadas
     * @return cuenta actualizada después de validaciones y persistencia
     */
    @Override
    @Auditable(operationType = OperationType.UPDATE, affectedTable = "ACCOUNT_CATALOGUE", moduleName = "ACCOUNTING", idArgIndex = 0)
    public AccountCatalogue updateAccountCatalogue(long id, AccountCatalogue accountCatalogue) {
        // Validar que la cuenta a actualizar existe
        AccountCatalogue existingAccount = validationService.validateAccountExistsByIdAndEnterprise(id,
                accountCatalogue.getIdEnterprise());

        // Validar que la cuenta no tenga movimientos contables registrados
        if (existingAccount.isInUse()) {
            throw new AccountCatalogueInUseException(existingAccount.getCode(), true); // true indica operación de
                                                                                       // edición
        }

        // Verificar que el idEnterprise esté establecido
        if (accountCatalogue.getIdEnterprise() == null || accountCatalogue.getIdEnterprise().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empresa es requerido para la actualización");
        }

        // Validar que el idEnterprise coincida con el de la cuenta existente
        // (seguridad)
        if (!accountCatalogue.getIdEnterprise().equals(existingAccount.getIdEnterprise())) {
            throw new IllegalArgumentException(
                    "El ID de empresa no coincide. Esperado: " + existingAccount.getIdEnterprise() +
                            ", Recibido: " + accountCatalogue.getIdEnterprise());
        }

        // Normalizar y validar el código de cuenta
        if (accountCatalogue.getCode() != null) {
            accountCatalogue.setCode(accountCatalogue.getCode().trim());
        }

        if (accountCatalogue.getCode() == null || accountCatalogue.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la cuenta es requerido para la actualización");
        }

        validationService.validateAccountCode(accountCatalogue.getCode());

        // Normalizar y validar la descripción de la cuenta
        if (accountCatalogue.getDescription() != null) {
            accountCatalogue.setDescription(accountCatalogue.getDescription().trim());
        }
        validationService.validateAccountDescription(accountCatalogue.getDescription());

        // Validar que no existe otra cuenta con el mismo código (excluyendo la actual)
        validationService.validateAccountDoesNotExistExcluding(
                accountCatalogue.getCode(),
                accountCatalogue.getIdEnterprise(),
                id);

        // Validar que no existe otra cuenta con la misma descripción (excluyendo la
        // actual) - case-insensitive
        validationService.validateAccountDescriptionDoesNotExistExcluding(
                accountCatalogue.getDescription(),
                accountCatalogue.getIdEnterprise(),
                id);

        // Validar que si el código cambió y la cuenta tiene padre, el nuevo código
        // mantenga el prefijo del padre
        boolean codeChanged = accountCatalogue.getCode() != null
                && !accountCatalogue.getCode().equals(existingAccount.getCode());
        if (codeChanged && existingAccount.getParent() != null && existingAccount.getParent().getCode() != null) {
            validationService.validateParentCodePrefix(accountCatalogue.getCode(),
                    existingAccount.getParent().getCode());
        }

        // Si tiene padre, validar que el padre existe
        if (accountCatalogue.getParent() != null && accountCatalogue.getParent().getId() != null) {
            validationService.validateAccountExistsByIdAndEnterprise(accountCatalogue.getParent().getId(),
                    accountCatalogue.getIdEnterprise());
        }

        // Validar que crossing y costCenter solo se puedan establecer en cuentas
        // auxiliares (8 dígitos)
        validationService.validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(accountCatalogue);

        // Validar que costCenter solo pueda ser true cuando financialStatus sea Estado
        // de Resultados
        validationService.validateCostCenterRequiresIncomeStatement(accountCatalogue);

        return accountCatalogueUpdateOutputport.updateAccountCatalogue(id, accountCatalogue);
    }
}
