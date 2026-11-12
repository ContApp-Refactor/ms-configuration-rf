package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.ITreasuryAccountingInputPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.ITreasuryAccountingInputPort.AccountingEventContext;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.config.TreasuryAccountingConsumerConfig;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryAccountingEventDtos.TreasuryAccountingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumidor de los eventos de contabilizacion publicados por tesoreria.
 *
 * <p>El {@code Message} como primer argumento no es decorativo:
 * {@code JWTContextRabbitMqAspect} lo busca para leer el header
 * {@code x-jwt-token} y armar el {@code TenantContext}. Sin ese argumento la
 * insercion del asiento se caeria por {@code tenant_id} nulo.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TreasuryAccountingListener {

    private static final String EVENT_PAYMENT_VOUCHER_CREATED = "PAYMENT_VOUCHER_CREATED";
    private static final String EVENT_PAYMENT_VOUCHER_VOID_REQUESTED = "PAYMENT_VOUCHER_VOID_REQUESTED";
    private static final String EVENT_PAYABLE_WRITEOFF_CREATED = "PAYABLE_WRITEOFF_CREATED";
    private static final String EVENT_PAYABLE_WRITEOFF_VOID_REQUESTED = "PAYABLE_WRITEOFF_VOID_REQUESTED";

    private final ITreasuryAccountingInputPort treasuryAccounting;
    private final ObjectMapper mapper;

    @RabbitListener(queues = TreasuryAccountingConsumerConfig.ACCOUNTING_QUEUE,
            containerFactory = "treasuryRabbitListenerContainerFactory")
    public void onAccountingEvent(Message message, TreasuryAccountingEvent event) {
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
