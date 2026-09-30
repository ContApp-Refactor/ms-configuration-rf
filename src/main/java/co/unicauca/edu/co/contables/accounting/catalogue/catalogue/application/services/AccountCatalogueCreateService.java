package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueCreateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueCreateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @brief Servicio para operaciones de creación de cuentas contables
 *
 *        Maneja la creación de nuevas cuentas del catálogo con validaciones
 *        completas de negocio y reglas de integridad.
 */
@Service
@AllArgsConstructor
public class AccountCatalogueCreateService implements IAccountCatalogueCreateInputPort {

    private final IAccountCatalogueCreateOutputPort accountCatalogueCreateOutputPort;
    private final AccountCatalogueValidationService validationService;

    /**
     * @brief Crea cuenta contable con validaciones completas
     * @param accountCatalogue datos de la cuenta a crear
     * @return cuenta creada con ID generado
     */
    @Override
    @Auditable(operationType = OperationType.CREATE, affectedTable = "ACCOUNT_CATALOGUE", moduleName = "ACCOUNTING")
    public AccountCatalogue createAccountCatalogue(AccountCatalogue accountCatalogue) {

        if (accountCatalogue.getCode() != null) {
            accountCatalogue.setCode(accountCatalogue.getCode().trim());
        }
        validationService.validateAccountCode(accountCatalogue.getCode());

        if (accountCatalogue.getDescription() != null) {
            accountCatalogue.setDescription(accountCatalogue.getDescription().trim());
        }
        validationService.validateAccountDescription(accountCatalogue.getDescription());

        validationService.validateAccountDoesNotExist(
                accountCatalogue.getCode(),
                accountCatalogue.getIdEnterprise());

        validationService.validateAccountDescriptionDoesNotExist(
                accountCatalogue.getDescription(),
                accountCatalogue.getIdEnterprise());

        if (accountCatalogue.getParent() != null && accountCatalogue.getParent().getId() != null) {
            validationService.validateAccountExistsByIdAndEnterprise(accountCatalogue.getParent().getId(),
                    accountCatalogue.getIdEnterprise());

        }

        validationService.validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(accountCatalogue);

        validationService.validateCostCenterRequiresIncomeStatement(accountCatalogue);

        return accountCatalogueCreateOutputPort.createAccountCatalogue(accountCatalogue);
    }

    /**
     * @brief Crea múltiples cuentas en batch sin validaciones individuales
     * @details Usado durante importación masiva donde las validaciones ya se
     *          realizaron en fase previa.
     *          Delega directamente al output port para máximo rendimiento.
     * @param accountCatalogues lista de cuentas ya validadas
     * @return lista de cuentas creadas con IDs asignados
     */
    public List<AccountCatalogue> createAllAccountCatalogues(List<AccountCatalogue> accountCatalogues) {
        return accountCatalogueCreateOutputPort.createAllAccountCatalogues(accountCatalogues);
    }
}
