package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueChangeStateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueChangeStateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * @brief Servicio para cambio de estado de cuentas contables
 *
 *        Maneja la activación/desactivación de cuentas del catálogo,
 *        aplicando cambios a la cuenta y todos sus descendientes jerárquicos.
 */
@Service
@AllArgsConstructor
public class AccountCatalogueChangeStateService implements IAccountCatalogueChangeStateInputPort {

    private final IAccountCatalogueChangeStateOutputPort accountCatalogueChangeStateOutputPort;
    private final AccountCatalogueValidationService validationService;

    /**
     * @brief Cambia estado de cuenta y jerarquía completa
     * @param id           ID de la cuenta
     * @param idEnterprise ID de la empresa
     * @param status       nuevo estado (true=activo, false=inactivo)
     * @return cuenta actualizada
     */
    @Transactional
    @Override
    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "ACCOUNT_CATALOGUE", moduleName = "ACCOUNTING", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public AccountCatalogue changeState(Long id, String idEnterprise, Boolean status) {
        if (status == null) {
            throw new IllegalArgumentException("El parámetro 'status' es requerido");
        }

        validationService.validateAccountExistsByIdAndEnterprise(id, idEnterprise);

        return accountCatalogueChangeStateOutputPort.changeState(id, status);
    }
}
