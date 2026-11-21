package co.unicauca.edu.co.contables.accounting.debt_payments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IAccountingEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IResourceUsageNotifierPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.replicas.InvoiceReplicaEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IMessageProcessingErrorRepository;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IPortfolioWriteOffRepository;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IReceiptRepository;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.replicas.IInvoiceRepository;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.InvoiceSaleEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("ms-debt-payments §3 — recibos y castigos con facturas creadas por eventos")
class ReceiptWriteOffRestFlowTest {

    private static final String ENT_ID = "ENT-1";
    private static final long THIRD_ID = 456L;

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @MockitoBean
    private co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener.SpringEventsInvoiceEventListener catalogueInvoiceEventListener;

    @MockitoBean
    private IAccountingEventPublisher accountingEventPublisher;

    @MockitoBean
    private IResourceUsageNotifierPort resourceUsageNotifier;

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private IReceiptRepository receiptRepository;

    @Autowired
    private IPortfolioWriteOffRepository writeOffRepository;

    @Autowired
    private IMessageProcessingErrorRepository errorRepository;

    @BeforeEach
    void cleanDatabase() {

        TenantContext.setTenantId("default");
        writeOffRepository.deleteAll();
        receiptRepository.deleteAll();
        invoiceRepository.deleteAll();
        errorRepository.deleteAll();
    }

    @Test
    @DisplayName("SETUP facturas A y B - GET by-enterprise devuelve las dos facturas creadas por eventos")
    void setupFacturas_visiblesPorEmpresa() throws Exception {
        publishPendingInvoice(2024001L);
        publishPendingInvoice(2024002L);

        mockMvc.perform(get("/api/payments/invoices/by-enterprise/{enterpriseId}", ENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].factCode",
                        org.hamcrest.Matchers.containsInAnyOrder("2024001", "2024002")));
    }

    @Test
    @DisplayName("[201 Created] Pago completo de la Factura A - queda PAID con pendiente 0")
    void pagoCompleto_facturaQuedaPaid() throws Exception {
        long invoiceId = publishPendingInvoice(2024001L);

        mockMvc.perform(post("/api/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullPaymentBody(invoiceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Recibo creado exitosamente."))
                .andExpect(jsonPath("$.data.receiptTypeId").value(1))
                .andExpect(jsonPath("$.data.thirdPartyId").value(THIRD_ID))
                .andExpect(jsonPath("$.data.enterpriseId").value(ENT_ID))
                .andExpect(jsonPath("$.data.totalAmount").value(150000))
                .andExpect(jsonPath("$.data.details[0].invoiceId").value(invoiceId))
                .andExpect(jsonPath("$.data.details[0].amountPaid").value(150000))
                .andExpect(jsonPath("$.data.details[0].invoiceCode").value("2024001"))
                .andExpect(jsonPath("$.data.details[0].accountingAccount").value(130505));

        tenant();
        InvoiceReplicaEntity invoice = invoiceRepository.findById(invoiceId).orElseThrow();
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(0L, invoice.getPendingValue());
        assertEquals(200000L, invoice.getTotalPay());
        assertEquals(1, receiptRepository.findAll().size());
        assertTrue(errorRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("[201 Created] Abono parcial a la Factura B - queda PENDING con pendiente 50000")
    void abonoParcial_facturaQuedaPending() throws Exception {
        long invoiceId = publishPendingInvoice(2024002L);

        mockMvc.perform(post("/api/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partialPaymentBody(invoiceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.totalAmount").value(100000))
                .andExpect(jsonPath("$.data.details[0].amountPaid").value(100000));

        tenant();
        InvoiceReplicaEntity invoice = invoiceRepository.findById(invoiceId).orElseThrow();
        assertEquals(InvoiceStatus.PENDING, invoice.getStatus());
        assertEquals(50000L, invoice.getPendingValue());
        assertEquals(150000L, invoice.getTotalPay());
    }

    @Test
    @DisplayName("[409 Conflict] Pagar una factura ya PAID - INVALID_STATE_OPERATION")
    void pagarFacturaPaid_devuelve409() throws Exception {
        long invoiceId = publishPendingInvoice(2024001L);
        String body = fullPaymentBody(invoiceId);

        mockMvc.perform(post("/api/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_STATE_OPERATION"))
                .andExpect(jsonPath("$.message",
                        org.hamcrest.Matchers.containsString("Cannot apply payment to a fully paid invoice")));
    }

    @Test
    @DisplayName("[201/200/200] Castigo de la Factura C - crear, confirmar y anular")
    void castigo_crearConfirmarYAnular() throws Exception {
        long invoiceId = publishPendingInvoice(2024003L);

        MvcResult created = mockMvc.perform(post("/api/payments/write-offs/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeOffBody(invoiceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("Castigo de cartera creado exitosamente."))
                .andExpect(jsonPath("$.data.status").value("PENDING_CONFIRMATION"))
                .andExpect(jsonPath("$.data.totalAmount").value(150000))
                .andExpect(jsonPath("$.data.details[0].amountWrittenOff").value(150000))
                .andExpect(jsonPath("$.data.details[0].invoice.factCode").value("2024003"))
                .andReturn();

        long writeOffId = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asLong();
        tenant();
        InvoiceReplicaEntity pendingWriteOff = invoiceRepository.findById(invoiceId).orElseThrow();
        assertEquals(InvoiceStatus.PENDING_WRITTEN_OFF, pendingWriteOff.getStatus());

        mockMvc.perform(get("/api/payments/write-offs/{id}", writeOffId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING_CONFIRMATION"));

        mockMvc.perform(put("/api/payments/write-offs/{id}/confirm", writeOffId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Castigo de cartera confirmado."))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        tenant();
        InvoiceReplicaEntity writtenOff = invoiceRepository.findById(invoiceId).orElseThrow();
        assertEquals(InvoiceStatus.WRITTEN_OFF, writtenOff.getStatus());
        assertEquals(0L, writtenOff.getPendingValue());

        mockMvc.perform(put("/api/payments/write-offs/{id}/void", writeOffId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Confirmación de castigo de cartera anulada."))
                .andExpect(jsonPath("$.data.status").value("VOIDED"));

        tenant();
        InvoiceReplicaEntity reversed = invoiceRepository.findById(invoiceId).orElseThrow();
        assertEquals(InvoiceStatus.PENDING, reversed.getStatus());
        assertEquals(150000L, reversed.getPendingValue());
    }

    @Test
    @DisplayName("[409 Conflict] Anular dos veces y confirmar un castigo anulado - INVALID_STATE_OPERATION")
    void castigo_transicionesInvalidas_devuelven409() throws Exception {
        long invoiceId = publishPendingInvoice(2024004L);
        MvcResult created = mockMvc.perform(post("/api/payments/write-offs/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeOffBody(invoiceId)))
                .andExpect(status().isCreated())
                .andReturn();
        long writeOffId = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(put("/api/payments/write-offs/{id}/void", writeOffId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_OPERATION"))
                .andExpect(jsonPath("$.message").value("Only a CONFIRMED write-off can be voided."));

        mockMvc.perform(put("/api/payments/write-offs/{id}/confirm", writeOffId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/payments/write-offs/{id}/confirm", writeOffId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_OPERATION"))
                .andExpect(jsonPath("$.message").value(
                        "Only a write-off with PENDING_CONFIRMATION status can be confirmed."));
    }

    private long publishPendingInvoice(long factCode) {

        TenantContext.setTenantId("default");
        events.publishEvent(new InvoiceSaleEvent(postmanSale(factCode)));

        List<InvoiceReplicaEntity> invoices = invoiceRepository.findByEntIdAndStatus(ENT_ID, InvoiceStatus.PENDING);
        return invoices.stream()
                .filter(invoice -> invoice.getFactCode().equals(factCode))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se persistió la factura " + factCode))
                .getId();
    }

    private void tenant() {
        TenantContext.setTenantId("default");
    }

    private ReceiptSalesDtoRequest postmanSale(long factCode) {
        return ReceiptSalesDtoRequest.builder()
                .factCode(factCode)
                .entId(ENT_ID)
                .thirdId(THIRD_ID)
                .totalValue(200000L)
                .totalPay(50000L)
                .pendingValue(150000L)
                .creationDate(LocalDate.of(2024, 7, 1))
                .expirationDate(LocalDate.of(2025, 7, 15))
                .active(true)
                .accountingAccount(130505L)
                .build();
    }

    private String fullPaymentBody(long invoiceId) {
        return """
                {
                  "thirdPartyId": 456,
                  "paymentMethodId": 5,
                  "paymentMethodAccount": 11050101,
                  "receiptTypeId": 1,
                  "observations": "Abono a factura FACT-1001",
                  "enterpriseId": "ENT-1",
                  "totalAmount": 150000,
                  "details": [
                    { "invoiceId": %d, "amountPaid": 150000 }
                  ]
                }
                """.formatted(invoiceId);
    }

    private String partialPaymentBody(long invoiceId) {
        return """
                {
                  "thirdPartyId": 456,
                  "paymentMethodId": 5,
                  "paymentMethodAccount": 11050101,
                  "receiptTypeId": 1,
                  "observations": "Abono a factura FACT-1002",
                  "enterpriseId": "ENT-1",
                  "totalAmount": 100000,
                  "details": [
                    { "invoiceId": %d, "amountPaid": 100000 }
                  ]
                }
                """.formatted(invoiceId);
    }

    private String writeOffBody(long invoiceId) {
        return """
                {
                  "justification": "Prueba de crear un borrador de castigo",
                  "writeOffDate": "%s",
                  "debitAuxiliaryAccount": 11050102,
                  "debitAuxiliaryAccountId": 6,
                  "thirdId": 456,
                  "enterpriseId": "ENT-1",
                  "costCenterId": 1,
                  "details": [
                    { "invoiceId": %d }
                  ]
                }
                """.formatted(LocalDate.now().plusDays(10), invoiceId);
    }
}
