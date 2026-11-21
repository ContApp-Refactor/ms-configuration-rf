package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config.RabbitInvoiceConfig;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.InvoiceSaleEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("facture SpringEventsReceiptPublisher Unit Tests")
class SpringEventsReceiptPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private IJwtUtils jwtUtils;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsReceiptPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsReceiptPublisher(rabbitTemplate, jwtUtils, eventPublisher);
    }

    @Test
    @DisplayName("SALE - publica el evento concreto en el bus en proceso")
    void sale_publicaEventoConcreto() {
        ReceiptSalesDtoRequest dto = sale();

        publisher.publishSaleReceiptEvent(dto);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        InvoiceSaleEvent event = assertInstanceOf(InvoiceSaleEvent.class, captor.getValue());
        assertEquals(EventFactureType.SALE, event.getType());
        assertEquals(InvoiceSaleEvent.TYPE, event.getType());
        assertSame(dto, event.getData());
    }

    @Test
    @DisplayName("SALE - publica en el bus en proceso")
    void sale_noPublicaEnRabbit() {
        ReceiptSalesDtoRequest dto = sale();

        publisher.publishSaleReceiptEvent(dto);

        verify(eventPublisher).publishEvent(any(Object.class));
        verify(rabbitTemplate, never()).convertAndSend(
                eq(RabbitInvoiceConfig.INVOICE_EXCHANGE),
                eq(""),
                any(),
                any(MessagePostProcessor.class));
    }

    @Test
    @DisplayName("PURCHASE - publica el DTO en el bus en proceso")
    void purchase_publicaEventoEnElBus() {
        PurchaseInvoiceEventDto dto = PurchaseInvoiceEventDto.builder()
                .eventId("evt-1")
                .eventType("PURCHASE_INVOICE_CREATED")
                .reference("FV-1")
                .tenantId("ENT-1")
                .build();

        publisher.publishPurchaseInvoiceEvent(dto);

        verify(eventPublisher).publishEvent(dto);
        verifyNoInteractions(rabbitTemplate);
    }

    private ReceiptSalesDtoRequest sale() {
        return ReceiptSalesDtoRequest.builder()
                .factCode(1001L)
                .entId("ENT-1")
                .thirdId(55L)
                .totalPay(40000L)
                .totalValue(100000L)
                .pendingValue(60000L)
                .creationDate(LocalDate.of(2026, 1, 15))
                .expirationDate(LocalDate.of(2026, 2, 15))
                .active(true)
                .accountingAccount(130505L)
                .build();
    }
}
