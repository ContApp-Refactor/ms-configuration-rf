package co.unicauca.edu.co.contables.accounting.debt_payments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.replicas.InvoiceReplicaEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IMessageProcessingErrorRepository;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.replicas.IInvoiceRepository;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.listener.InvoiceEventListener;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.listener.SpringEventsInvoiceEventListener;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.InvoiceSaleEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@DisplayName("ms-debt-payments §2 — flujo de eventos de factura")
class InvoiceEventFlowTest {

    private static final String ENT_ID = "ENT-1";

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @MockitoBean
    private co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener.SpringEventsInvoiceEventListener catalogueInvoiceEventListener;

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private IMessageProcessingErrorRepository errorRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void cleanDatabase() {
        invoiceRepository.deleteAll();
        errorRepository.deleteAll();
    }

    @Test
    @DisplayName("Publicar Mensaje de Factura Válido - se persiste la réplica pendiente")
    void saleValido_persisteReplicaPendiente() {
        events.publishEvent(new InvoiceSaleEvent(postmanSale()));

        List<InvoiceReplicaEntity> invoices = invoiceRepository.findByEntIdAndStatus(ENT_ID, InvoiceStatus.PENDING);
        assertEquals(1, invoices.size());
        InvoiceReplicaEntity invoice = invoices.get(0);
        assertEquals(2024001L, invoice.getFactCode());
        assertEquals(ENT_ID, invoice.getEntId());
        assertEquals(456L, invoice.getThirdId());
        assertEquals(200000L, invoice.getTotalValue());
        assertEquals(50000L, invoice.getTotalPay());
        assertEquals(150000L, invoice.getPendingValue());
        assertEquals(LocalDate.of(2024, 7, 1), invoice.getCreationDate());
        assertEquals(LocalDate.of(2025, 7, 15), invoice.getExpirationDate());
        assertEquals(130505L, invoice.getAccountingAccount());
        assertTrue(invoice.isActive());
        assertTrue(errorRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("EventType is null - persiste 'Validation failed: Event type is null'")
    void tipoNulo_persisteError() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(postmanSale());
        event.setType(null);

        events.publishEvent(event);

        assertValidationError("Validation failed: Event type is null", "\"factCode\":2024001", "null_event_type");
    }

    @Test
    @DisplayName("EventData is null - persiste 'Validation failed: Event data is null'")
    void dataNula_persisteError() {
        events.publishEvent(new InvoiceSaleEvent(null));

        assertValidationError("Validation failed: Event data is null",
                "{\"error\": \"Event data is null\", \"eventType\": \"SALE\"}", "SALE");
    }

    @Test
    @DisplayName("FactCode is null - persiste 'Validation failed: FactCode is null'")
    void factCodeNulo_persisteError() {
        ReceiptSalesDtoRequest dto = postmanSale();
        dto.setFactCode(null);

        events.publishEvent(new InvoiceSaleEvent(dto));

        assertValidationError("Validation failed: FactCode is null", "\"factCode\":null", "SALE");
    }

    @Test
    @DisplayName("AccountingAccount is null - persiste 'Validation failed: AccountingAccount is null'")
    void accountingAccountNulo_persisteError() {
        ReceiptSalesDtoRequest dto = postmanSale();
        dto.setAccountingAccount(null);

        events.publishEvent(new InvoiceSaleEvent(dto));

        assertValidationError("Validation failed: AccountingAccount is null", "\"accountingAccount\":null", "SALE");
    }

    @Test
    @DisplayName("EnterpriseId is null - persiste 'Validation failed: EntId is null'")
    void entIdNulo_persisteError() {
        ReceiptSalesDtoRequest dto = postmanSale();
        dto.setEntId(null);

        events.publishEvent(new InvoiceSaleEvent(dto));

        assertValidationError("Validation failed: EntId is null", "\"entId\":null", "SALE");
    }

    @Test
    @DisplayName("ExpirationDate is null - persiste 'Validation failed: ExpirationDate is null'")
    void expirationDateNula_persisteError() {
        ReceiptSalesDtoRequest dto = postmanSale();
        dto.setExpirationDate(null);

        events.publishEvent(new InvoiceSaleEvent(dto));

        assertValidationError("Validation failed: ExpirationDate is null", "\"expirationDate\":null", "SALE");
    }

    @Test
    @DisplayName("PendingValue is null - persiste 'Validation failed: PendingValue is null'")
    void pendingValueNulo_persisteError() {
        ReceiptSalesDtoRequest dto = postmanSale();
        dto.setPendingValue(null);

        events.publishEvent(new InvoiceSaleEvent(dto));

        assertValidationError("Validation failed: PendingValue is null", "\"pendingValue\":null", "SALE");
    }

    @Test
    @DisplayName("ThirdId is null - persiste 'Validation failed: ThirdId is null'")
    void thirdIdNulo_persisteError() {
        ReceiptSalesDtoRequest dto = postmanSale();
        dto.setThirdId(null);

        events.publishEvent(new InvoiceSaleEvent(dto));

        assertValidationError("Validation failed: ThirdId is null", "\"thirdId\":null", "SALE");
    }

    @Test
    @DisplayName("TotalPay is null - persiste 'Validation failed: TotalPay is null'")
    void totalPayNulo_persisteError() {
        ReceiptSalesDtoRequest dto = postmanSale();
        dto.setTotalPay(null);

        events.publishEvent(new InvoiceSaleEvent(dto));

        assertValidationError("Validation failed: TotalPay is null", "\"totalPay\":null", "SALE");
    }

    @Test
    @DisplayName("TotalValue is null - persiste 'Validation failed: TotalValue is null'")
    void totalValueNulo_persisteError() {
        ReceiptSalesDtoRequest dto = postmanSale();
        dto.setTotalValue(null);

        events.publishEvent(new InvoiceSaleEvent(dto));

        assertValidationError("Validation failed: TotalValue is null", "\"totalValue\":null", "SALE");
    }

    @Test
    @DisplayName("Perfil por defecto - listener Spring activo y listener/topología Rabbit ausentes")
    void perfilPorDefecto_soloSpringEvents() {
        assertNotNull(applicationContext.getBean("debtPaymentsSpringEventsInvoiceEventListener",
                SpringEventsInvoiceEventListener.class));

        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(InvoiceEventListener.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean("invoicePaymentsQueue"));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean("invoicePaymentsDlq"));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean("invoicePaymentsRetryQueue"));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean("invoiceExchange"));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean("invoicePaymentsDlx"));
    }

    private void assertValidationError(String expectedDescription, String expectedJsonFragment,
            String expectedEventType) {
        assertTrue(invoiceRepository.findAll().isEmpty(), "no se debe persistir la factura");
        List<MessageProcessingErrorEntity> errors = errorRepository.findAll();
        assertEquals(1, errors.size());
        MessageProcessingErrorEntity error = errors.get(0);
        assertEquals(expectedDescription, error.getErrorDescription());
        assertEquals("Invoice", error.getEntityType());
        assertEquals(expectedEventType, error.getEventType());
        assertTrue(error.getMessageData().contains(expectedJsonFragment),
                () -> "messageData inesperado: " + error.getMessageData());
    }

    private ReceiptSalesDtoRequest postmanSale() {
        return ReceiptSalesDtoRequest.builder()
                .factCode(2024001L)
                .entId(ENT_ID)
                .thirdId(456L)
                .totalValue(200000L)
                .totalPay(50000L)
                .pendingValue(150000L)
                .creationDate(LocalDate.of(2024, 7, 1))
                .expirationDate(LocalDate.of(2025, 7, 15))
                .active(true)
                .accountingAccount(130505L)
                .build();
    }
}
