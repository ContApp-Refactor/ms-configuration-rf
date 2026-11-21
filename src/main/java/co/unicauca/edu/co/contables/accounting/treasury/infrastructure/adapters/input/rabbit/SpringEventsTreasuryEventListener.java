package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IInvoiceSynchronizationUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPayableWriteOffCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentVoucherCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentScheduleExecutionUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryRabbitDtos.AccountingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsTreasuryEventListener {

    private final IInvoiceSynchronizationUseCase invoiceSynchronization;
    private final IPaymentVoucherCommandUseCase vouchers;
    private final IPayableWriteOffCommandUseCase writeOffs;
    private final IPaymentScheduleExecutionUseCase schedules;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void invoice(PurchaseInvoiceEventDto event) {
        try {
            invoiceSynchronization.synchronize(new TreasuryCommands.PurchaseInvoiceEvent(
                    event.getEventId(), event.getEventType(), event.getInvoiceId(), event.getReference(),
                    event.getEnterpriseId(), event.getSupplierId(), event.getOriginalAmount(), event.getPaidAmount(),
                    event.getPendingAmount(), event.getIssueDate(), event.getDueDate(), event.getPayableAccountId(),
                    event.getPayableAccountCode(), event.isActive(), event.getTenantId()));
        } catch (Exception ex) {
            log.error("Error procesando evento de compra en treasury: {}", event.getReference(), ex);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void accounting(AccountingResult event) {
        try {
            var result = new TreasuryCommands.AccountingResult(
                    event.eventId(), event.sourceEventId(), event.operation(), event.documentType(),
                    event.documentId(), event.accepted(), event.accountingEntryId(), event.reason(),
                    event.tenantId());
            vouchers.applyAccountingResult(result);
            writeOffs.applyAccountingResult(result);
            schedules.applyAccountingResult(result);
        } catch (Exception ex) {
            log.error("Error aplicando resultado contable en treasury: {} {} {}",
                    event.operation(), event.documentType(), event.documentId(), ex);
        }
    }
}
