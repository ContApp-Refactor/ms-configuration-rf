package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.WriteOffDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceSummaryEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.PortfolioWriteOffEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptAccountingEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.WriteOffAccountingEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.WriteOffDetailEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper.IPortfolioWriteOffEventMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper.IReceiptEventMapper;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class SpringEventsAccountingEventPublisher extends AccountingEventPublisher {

    private final IReceiptEventMapper receiptEventMapper;
    private final IPortfolioWriteOffEventMapper writeOffEventMapper;
    private final IInvoiceProviderPort invoiceProviderPort;
    private final ApplicationEventPublisher eventPublisher;

    public SpringEventsAccountingEventPublisher(
            IReceiptEventMapper receiptEventMapper,
            RabbitTemplate rabbitTemplate,
            IPortfolioWriteOffEventMapper writeOffEventMapper,
            IInvoiceProviderPort invoiceProviderPort,
            IJwtUtils jwtUtils,
            ApplicationEventPublisher eventPublisher) {
        super(receiptEventMapper, rabbitTemplate, writeOffEventMapper, invoiceProviderPort, jwtUtils);
        this.receiptEventMapper = receiptEventMapper;
        this.writeOffEventMapper = writeOffEventMapper;
        this.invoiceProviderPort = invoiceProviderPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishReceiptCreatedEvent(Receipt receipt) {
        ReceiptEventDto receiptEventDto = receiptEventMapper.toEventDto(receipt);
        log.info("Publishing receipt created event: {}", receiptEventDto.getReceiptCode());

        eventPublisher.publishEvent(new ReceiptAccountingEvent(
                ReceiptAccountingEvent.RECEIPT_CREATED,
                receiptEventDto));
    }

    @Override
    public void publishVoidReceiptEvent(Receipt receipt) {
        ReceiptEventDto receiptEventDto = receiptEventMapper.toEventDto(receipt);
        log.info("Publishing receipt voided event: {}", receiptEventDto.getReceiptCode());

        eventPublisher.publishEvent(new ReceiptAccountingEvent(
                ReceiptAccountingEvent.RECEIPT_VOIDED,
                receiptEventDto));
    }

    @Override
    public void publishWriteOffConfirmedEvent(PortfolioWriteOff writeOff) {
        PortfolioWriteOffEventDto eventDto = buildEnrichedEventDto(writeOff);
        log.info("Publishing write-off confirmed event: {}", eventDto.getCode());

        eventPublisher.publishEvent(new WriteOffAccountingEvent(
                WriteOffAccountingEvent.WRITEOFF_CONFIRMED,
                eventDto));
    }

    @Override
    public void publishWriteOffVoidedEvent(PortfolioWriteOff writeOff) {
        PortfolioWriteOffEventDto eventDto = buildEnrichedEventDto(writeOff);
        log.info("Publishing write-off voided event: {}", eventDto.getCode());

        eventPublisher.publishEvent(new WriteOffAccountingEvent(
                WriteOffAccountingEvent.WRITEOFF_VOIDED,
                eventDto));
    }

    private PortfolioWriteOffEventDto buildEnrichedEventDto(PortfolioWriteOff writeOff) {
        PortfolioWriteOffEventDto eventDto = writeOffEventMapper.toEventDto(writeOff);

        List<Long> invoiceIds = writeOff.getDetails().stream().map(WriteOffDetail::getInvoiceId).toList();

        Map<Long, InvoiceReplica> invoiceMap = invoiceProviderPort.findInvoicesByIds(invoiceIds).stream()
                .collect(Collectors.toMap(InvoiceReplica::getId, Function.identity()));

        List<WriteOffDetailEventDto> detailDtos = writeOff.getDetails().stream()
                .map(detail -> {
                    InvoiceReplica invoice = invoiceMap.get(detail.getInvoiceId());
                    if (invoice == null) {
                        return null;
                    }

                    InvoiceSummaryEventDto invoiceSummary = InvoiceSummaryEventDto.builder()
                            .id(invoice.getId())
                            .factCode(invoice.getFactCode())
                            .totalValue(invoice.getTotalValue())
                            .pendingValue(detail.getAmountWrittenOff())
                            .expirationDate(invoice.getExpirationDate())
                            .accountingAccount(invoice.getAccountingAccount())
                            .build();

                    return WriteOffDetailEventDto.builder()
                            .amountWrittenOff(detail.getAmountWrittenOff())
                            .invoice(invoiceSummary)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        eventDto.setDetails(detailDtos);
        return eventDto;
    }
}
