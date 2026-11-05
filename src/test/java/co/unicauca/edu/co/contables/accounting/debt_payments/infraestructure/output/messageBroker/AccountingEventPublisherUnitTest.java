package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config.RabbitAccountingConfig;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.AccountingEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.PortfolioWriteOffEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper.IPortfolioWriteOffEventMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper.IReceiptEventMapper;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for AccountingEventPublisher")
public class AccountingEventPublisherUnitTest {

    @Mock
    private IReceiptEventMapper receiptEventMapper;
    
    @Mock
    private RabbitTemplate rabbitTemplate;
    
    @Mock
    private IPortfolioWriteOffEventMapper writeOffEventMapper;
    
    @Mock
    private IInvoiceProviderPort invoiceProviderPort;
    
    @Mock
    private IJwtUtils jwtUtils;

    @InjectMocks
    private AccountingEventPublisher publisher;

    @Test
    @DisplayName("publishReceiptCreatedEvent should call mapper and send to exchange")
    void shouldPublishReceiptCreatedEvent() {
        // Arrange
        var receipt = receiptForDirectIncome("ENT-1", 1L, 2L, "test", 1000L, 100L);
        var receiptDto = new ReceiptEventDto();
        
        when(receiptEventMapper.toEventDto(receipt)).thenReturn(receiptDto);
        lenient().when(jwtUtils.getToken()).thenReturn("test-token");

        // Act
        publisher.publishReceiptCreatedEvent(receipt);

        // Assert
        verify(receiptEventMapper).toEventDto(receipt);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitAccountingConfig.RECEIPT_EXCHANGE),
                eq(""),
                isA(Object.class),
                isA(org.springframework.amqp.core.MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("publishVoidReceiptEvent should call mapper and send to exchange")
    void shouldPublishVoidReceiptEvent() {
        // Arrange
        var receipt = receiptForDirectIncome("ENT-1", 2L, 3L, "voided", 2000L, 200L);
        var receiptDto = new ReceiptEventDto();
        
        when(receiptEventMapper.toEventDto(receipt)).thenReturn(receiptDto);
        lenient().when(jwtUtils.getToken()).thenReturn("test-token");

        // Act
        publisher.publishVoidReceiptEvent(receipt);

        // Assert
        verify(receiptEventMapper).toEventDto(receipt);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitAccountingConfig.RECEIPT_EXCHANGE),
                eq(""),
                isA(Object.class),
                isA(org.springframework.amqp.core.MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("publishWriteOffConfirmedEvent should enrich with invoices and publish")
    void shouldPublishWriteOffConfirmedEvent() {
        // Arrange
        var detail = writeOffDetail(1L, 500L);
        var writeOff = portfolioWriteOffWithDetails("ENT-1", 10L, "test", List.of(detail));
        var writeOffDto = new PortfolioWriteOffEventDto();
        var invoice = invoiceReplicaWith(1L, 1000L, 500L, InvoiceStatus.PENDING);
        
        when(writeOffEventMapper.toEventDto(writeOff)).thenReturn(writeOffDto);
        when(invoiceProviderPort.findInvoicesByIds(List.of(1L))).thenReturn(List.of(invoice));
        lenient().when(jwtUtils.getToken()).thenReturn("test-token");

        // Act
        publisher.publishWriteOffConfirmedEvent(writeOff);

        // Assert
        verify(writeOffEventMapper).toEventDto(writeOff);
        verify(invoiceProviderPort).findInvoicesByIds(List.of(1L));
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitAccountingConfig.WRITEOFF_EXCHANGE),
                eq(""),
                isA(Object.class),
                isA(org.springframework.amqp.core.MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("publishWriteOffVoidedEvent should enrich with invoices and publish")
    void shouldPublishWriteOffVoidedEvent() {
        // Arrange
        var detail = writeOffDetail(2L, 300L);
        var writeOff = portfolioWriteOffWithDetails("ENT-2", 20L, "voided", List.of(detail));
        var writeOffDto = new PortfolioWriteOffEventDto();
        var invoice = invoiceReplicaWith(2L, 600L, 300L, InvoiceStatus.PENDING);
        
        when(writeOffEventMapper.toEventDto(writeOff)).thenReturn(writeOffDto);
        when(invoiceProviderPort.findInvoicesByIds(List.of(2L))).thenReturn(List.of(invoice));
        lenient().when(jwtUtils.getToken()).thenReturn("test-token");

        // Act
        publisher.publishWriteOffVoidedEvent(writeOff);

        // Assert
        verify(writeOffEventMapper).toEventDto(writeOff);
        verify(invoiceProviderPort).findInvoicesByIds(List.of(2L));
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitAccountingConfig.WRITEOFF_EXCHANGE),
                eq(""),
                isA(Object.class),
                isA(org.springframework.amqp.core.MessagePostProcessor.class)
        );
    }
}
