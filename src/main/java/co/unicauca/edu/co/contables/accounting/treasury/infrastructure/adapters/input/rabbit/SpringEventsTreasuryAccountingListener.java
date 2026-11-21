package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.ITreasuryAccountingInputPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.ITreasuryAccountingInputPort.AccountingEventContext;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryAccountingEventDtos.TreasuryAccountingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("springEventsTreasuryAccountingListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsTreasuryAccountingListener {

    private static final String EVENT_PAYMENT_VOUCHER_CREATED = "PAYMENT_VOUCHER_CREATED";
    private static final String EVENT_PAYMENT_VOUCHER_VOID_REQUESTED = "PAYMENT_VOUCHER_VOID_REQUESTED";
    private static final String EVENT_PAYABLE_WRITEOFF_CREATED = "PAYABLE_WRITEOFF_CREATED";
    private static final String EVENT_PAYABLE_WRITEOFF_VOID_REQUESTED = "PAYABLE_WRITEOFF_VOID_REQUESTED";

    private final ITreasuryAccountingInputPort treasuryAccounting;
    private final ObjectMapper mapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAccountingEvent(TreasuryAccountingEvent event) {
        try {
            if (event == null || event.eventType() == null) {
                log.warn("Evento contable de tesoreria sin tipo; se ignora.");
                return;
            }
            AccountingEventContext context = new AccountingEventContext(
                    event.eventId(), event.eventType(), event.tenantId(),
                    event.enterpriseId(), event.correlationId());

            switch (event.eventType()) {
                case EVENT_PAYMENT_VOUCHER_CREATED -> {
                    PaymentVoucher voucher = convert(event, PaymentVoucher.class);
                    treasuryAccounting.postPaymentVoucher(voucher, context);
                }
                case EVENT_PAYMENT_VOUCHER_VOID_REQUESTED ->
                    treasuryAccounting.voidPaymentVoucher(documentId(event), context);
                case EVENT_PAYABLE_WRITEOFF_CREATED -> {
                    PayableWriteOff writeOff = convert(event, PayableWriteOff.class);
                    treasuryAccounting.postPayableWriteOff(writeOff, context);
                }
                case EVENT_PAYABLE_WRITEOFF_VOID_REQUESTED ->
                    treasuryAccounting.voidPayableWriteOff(documentId(event), context);
                default -> log.warn("Tipo de evento contable de tesoreria '{}' no soportado; se ignora.",
                        event.eventType());
            }
        } catch (Exception ex) {
            log.error("Error procesando evento contable de tesoreria '{}': {}",
                    event != null ? event.eventType() : "unknown", ex.getMessage(), ex);
        }
    }

    private <T> T convert(TreasuryAccountingEvent event, Class<T> type) {
        if (event.payload() == null || event.payload().isNull()) {
            throw new IllegalStateException("El evento " + event.eventType()
                    + " no trae payload; no es posible contabilizarlo");
        }
        try {
            return mapper.treeToValue(event.payload(), type);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible leer el payload del evento "
                    + event.eventType(), ex);
        }
    }

    private Long documentId(TreasuryAccountingEvent event) {
        return event.sourceDocument() == null ? null : event.sourceDocument().id();
    }
}
