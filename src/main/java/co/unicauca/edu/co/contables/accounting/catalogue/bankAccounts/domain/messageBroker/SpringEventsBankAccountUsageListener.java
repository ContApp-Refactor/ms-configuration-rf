package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.dto.BankAccountUsageDto;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountUsage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsBankAccountUsageListener {

    private final IBankAccountUsage bankAccountUsage;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onBankAccountUsed(BankAccountUsageDto event) {
        try {
            if (!isValidEvent(event)) {
                log.warn("Invalid bank account usage event received");
                return;
            }

            log.info("Registrando uso de cuenta bancaria ID: {}, empresa: {}, cantidad: {}",
                    event.getBankAccountId(), event.getEnterpriseId(), event.getQuantityUsed());
            bankAccountUsage.incrementUsageCount(event.getBankAccountId(), event.getEnterpriseId());
            log.info("Uso registrado correctamente para cuenta bancaria ID: {}", event.getBankAccountId());
        } catch (Exception e) {
            log.error("Error procesando evento de cuenta bancaria: {}", e.getMessage());
        }
    }

    private boolean isValidEvent(BankAccountUsageDto data) {
        if (data == null) {
            log.warn("Evento es null");
            return false;
        }

        if (data.getBankAccountId() == null) {
            log.warn("BankAccountId es null - campo obligatorio");
            return false;
        }

        if (data.getEnterpriseId() == null || data.getEnterpriseId().trim().isEmpty()) {
            log.warn("EnterpriseId es null o vacío - campo obligatorio");
            return false;
        }

        if (data.getQuantityUsed() == null || data.getQuantityUsed() <= 0) {
            log.warn("QuantityUsed es null o inválido - campo obligatorio");
            return false;
        }

        return true;
    }
}
