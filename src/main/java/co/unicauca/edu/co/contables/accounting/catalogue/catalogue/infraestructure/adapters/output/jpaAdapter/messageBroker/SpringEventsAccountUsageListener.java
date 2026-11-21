package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueUsagePort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.dto.AccountUsedEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsAccountUsageListener {

    private final IAccountCatalogueUsagePort accountCatalogueUsagePort;

    private static final String ACCOUNT_TYPE_ID = "ID";
    private static final String ACCOUNT_TYPE_CODE = "CODE";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAccountUsed(AccountUsedEventDto event) {
        try {
            if (!isValidEvent(event)) {
                log.warn("Invalid account usage event received");
                return;
            }

            log.info("Processing usage for account: {} (type: '{}') in enterprise: '{}'",
                    event.getAccount(), event.getSourceAccountType(), event.getEnterpriseId());
            accountCatalogueUsagePort.incrementUsageCount(event);
            log.info("Account usage event processed successfully for account: {} (type: '{}') in enterprise: '{}'",
                    event.getAccount(), event.getSourceAccountType(), event.getEnterpriseId());
        } catch (Exception e) {
            log.error("Error processing account usage event: {}", e.getMessage(), e);
        }
    }

    private boolean isValidEvent(AccountUsedEventDto data) {
        if (data == null) {
            log.warn("Event is null");
            return false;
        }

        if (data.getAccount() == null) {
            log.warn("Account is null - required field");
            return false;
        }

        if (data.getEnterpriseId() == null || data.getEnterpriseId().trim().isEmpty()) {
            log.warn("EnterpriseId is null or empty - required field");
            return false;
        }

        if (data.getSourceAccountType() == null || data.getSourceAccountType().trim().isEmpty()) {
            log.warn("SourceAccountType is null or empty - required field");
            return false;
        }

        if (!ACCOUNT_TYPE_ID.equalsIgnoreCase(data.getSourceAccountType())
                && !ACCOUNT_TYPE_CODE.equalsIgnoreCase(data.getSourceAccountType())) {
            log.warn("SourceAccountType must be '{}' or '{}', but was: {}",
                    ACCOUNT_TYPE_ID, ACCOUNT_TYPE_CODE, data.getSourceAccountType());
            return false;
        }

        return true;
    }
}
