package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.IInvoiceSynchronizationUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPayableWriteOffCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentVoucherCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentScheduleExecutionUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.config.TreasuryRabbitConfig;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryRabbitDtos.AccountingResultEnvelope;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryRabbitDtos.PurchaseInvoiceEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TreasuryEventListener {
    private final IInvoiceSynchronizationUseCase invoiceSynchronization;
    private final IPaymentVoucherCommandUseCase vouchers;
    private final IPayableWriteOffCommandUseCase writeOffs;
    private final IPaymentScheduleExecutionUseCase schedules;

    @RabbitListener(queues = TreasuryRabbitConfig.PURCHASE_QUEUE,
            containerFactory = "treasuryRabbitListenerContainerFactory")
    public void invoice(Message message, PurchaseInvoiceEnvelope envelope) {
        var event = envelope.payload();
        invoiceSynchronization.synchronize(new TreasuryCommands.PurchaseInvoiceEvent(
                envelope.eventId(), envelope.eventType(), event.invoiceId(), event.reference(),
                event.enterpriseId(), event.supplierId(), event.originalAmount(), event.paidAmount(),
                event.pendingAmount(), event.issueDate(), event.dueDate(), event.payableAccountId(),
                event.payableAccountCode(), event.active(), event.tenantId()));
    }

    @RabbitListener(queues = TreasuryRabbitConfig.RESULT_QUEUE,
            containerFactory = "treasuryRabbitListenerContainerFactory")
    public void accounting(Message message, AccountingResultEnvelope envelope) {
        var event = envelope.payload();
        var result = new TreasuryCommands.AccountingResult(
                event.eventId(), event.sourceEventId(), event.operation(), event.documentType(), event.documentId(), event.accepted(),
                event.accountingEntryId(), event.reason(), event.tenantId());
        vouchers.applyAccountingResult(result);
        writeOffs.applyAccountingResult(result);
        schedules.applyAccountingResult(result);
    }
}
