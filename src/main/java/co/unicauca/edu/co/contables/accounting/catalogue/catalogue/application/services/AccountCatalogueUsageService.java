package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueUsagePort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueUpdateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.dto.AccountUsedEventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Servicio para gestión de uso de cuentas contables
 *
 * Maneja la lógica de negocio relacionada con el contador de uso de cuentas contables
 * cuando son utilizados por otros servicios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountCatalogueUsageService implements IAccountCatalogueUsagePort {

    private final IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;
    private final IAccountCatalogueUpdateOutputPort accountCatalogueUpdateOutputPort;

    private static final String ACCOUNT_TYPE_ID = "ID";
    private static final String ACCOUNT_TYPE_CODE = "CODE";

    /**
     * @brief Incrementa el contador de uso de una cuenta contable
     * @details Busca la cuenta por ID o código según el tipo especificado en el evento,
     * y luego incrementa su contador de uso de forma atómica en la base de datos.
     * @param accountUsedEvent Evento con información de la cuenta usada
     * @throws AccountCatalogueNotFoundException si la cuenta no existe
     * @throws IllegalArgumentException si el tipo de cuenta es inválido
     */
    @Override
    public void incrementUsageCount(AccountUsedEventDto accountUsedEvent) {
        String sourceAccountType = accountUsedEvent.getSourceAccountType();
        Long account = accountUsedEvent.getAccount();
        String enterpriseId = accountUsedEvent.getEnterpriseId();

        log.info("Processing usage event for account: {} (type: {}) in enterprise: {}",
                 account, sourceAccountType, enterpriseId);

        AccountCatalogue accountCatalogue;

        // Buscar la cuenta por ID o código según el tipo
        if (ACCOUNT_TYPE_ID.equalsIgnoreCase(sourceAccountType)) {
            accountCatalogue = accountCatalogueSearchOutputPort.getAccountCatalogueById(account, enterpriseId);
        } else if (ACCOUNT_TYPE_CODE.equalsIgnoreCase(sourceAccountType)) {
            String codeString = account.toString();
            accountCatalogue = accountCatalogueSearchOutputPort.getAccountCatalogueByCode(codeString, enterpriseId);
        } else {
            throw new IllegalArgumentException("Tipo de fuente de cuenta inválido: " + sourceAccountType + 
                                             ". Debe ser '" + ACCOUNT_TYPE_ID + "' o '" + ACCOUNT_TYPE_CODE + "'");
        }

        if (accountCatalogue == null) {
            throw new AccountCatalogueNotFoundException("Cuenta contable no encontrada: " + account + 
                                                       " (tipo: " + sourceAccountType + ")");
        }

        // Incrementar el contador de uso de forma atómica
        AccountCatalogue updatedAccount = accountCatalogueUpdateOutputPort.incrementUsageCount(accountCatalogue.getId());

        if (updatedAccount != null) {
            log.info("Usage count incremented successfully for account ID: {}. New count: {}", 
                     accountCatalogue.getId(), updatedAccount.getUsageCount());
        } else {
            log.error("Failed to increment usage count for account ID: {}", accountCatalogue.getId());
        }
    }
}

