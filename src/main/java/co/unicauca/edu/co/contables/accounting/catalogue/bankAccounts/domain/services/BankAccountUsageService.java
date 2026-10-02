package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * @brief Servicio para gestión de uso de cuentas bancarias
 *
 * Implementa el caso de uso de incremento de contador de uso.
 * Mantiene la separación arquitectónica entre adaptadores de entrada 
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountUsageService implements IBankAccountUsage {

    private final IBankAccountService bankAccountService;

    @Override
    public void incrementUsageCount(Long bankAccountId, String enterpriseId) {
        log.info("Incrementing usage count for bankAccountId: {} in enterprise: {}", bankAccountId, enterpriseId);

        // Obtener la cuenta bancaria actual para incrementar el contador
        BankAccount currentBankAccount = bankAccountService.findById(bankAccountId, enterpriseId);
        Integer newUsageCount = currentBankAccount.getUsageCount() + 1;

        bankAccountService.updateUsageCount(bankAccountId, enterpriseId, newUsageCount);

        log.info("Usage count incremented successfully for bankAccountId: {}", bankAccountId);
    }
}
