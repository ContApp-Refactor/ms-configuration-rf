package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.counterpartyFor;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.unicauca.edu.co.contables.billing.application.ports.input.AnnulDocumentUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.CreateDocumentIdempotentlyUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.DeleteDocumentDraftUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.DuplicateInvoiceDraftUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.EmitDocumentUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.GetDocumentUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.GetReturnAvailabilityUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.ListDocumentReturnsUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.ListDocumentsUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.ListInvoicesWithReturnsUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.SaveInvoiceDraftUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentView;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.controller.BillingSaleInvoiceController;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.support.BillingInvoiceEndpointSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Forma de pago por REST en la factura de venta: request, response y codigos de error (422). */
@WebMvcTest(controllers = BillingSaleInvoiceController.class)
@ContextConfiguration(classes = {BillingSaleInvoiceController.class, BillingInvoiceEndpointSupport.class,
        BillingWebMvcTestConfig.class})
@ActiveProfiles("test")
@WithMockUser(authorities = {"BFV#R", "BFV#C", "BFV#U", "BFV#D", "BFV#EM", "BFV#AN"})
class BillingSaleInvoicePaymentRestTest {

    private static final String BASE = "/api/billing/sale-invoices";
    private static final DocumentType TYPE = DocumentType.SALE_INVOICE;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SaveInvoiceDraftUseCase saveUseCase;
    @MockitoBean
    private CreateDocumentIdempotentlyUseCase idempotentCreateUseCase;
    @MockitoBean
    private DuplicateInvoiceDraftUseCase duplicateUseCase;
    @MockitoBean
    private DeleteDocumentDraftUseCase deleteUseCase;
    @MockitoBean
    private GetDocumentUseCase getUseCase;
    @MockitoBean
    private ListDocumentsUseCase listUseCase;
    @MockitoBean
    private EmitDocumentUseCase emitUseCase;
    @MockitoBean
    private AnnulDocumentUseCase annulUseCase;
    @MockitoBean
    private GetReturnAvailabilityUseCase availabilityUseCase;
    @MockitoBean
    private ListDocumentReturnsUseCase returnsUseCase;
    @MockitoBean
    private ListInvoicesWithReturnsUseCase withReturnsUseCase;

    private static String body(String payment) {
        return "{\"clientId\":\"10\",\"date\":\"2026-01-15\",\"lines\":[{\"productId\":\"1\",\"cantidad\":2}],"
                + "\"version\":3" + (payment == null ? "" : ",\"payment\":" + payment) + "}";
    }

    private static BillingDocument saleWith(co.unicauca.edu.co.contables.billing.domain.model.PaymentTerms payment) {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(counterpartyFor(TYPE), TODAY, null, List.of(lineOf(1, product(1L), "2")), payment,
                NOW, USER);
        return withVersion(doc, 1L);
    }

    @Test
    void put_passes_only_the_method_and_ignores_bank_and_legacy_form_fields() throws Exception {
        BillingDocument doc = saleWith(methodTerms(1L));
        when(saveUseCase.update(eq(ENTERPRISE_ID), eq(TYPE), eq(doc.id()), any(InvoiceDraftCommand.class)))
                .thenReturn(doc);

        mockMvc.perform(put(BASE + "/" + doc.id()).param("enterpriseId", ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("{\"methodId\":7,\"bankAccountId\":3,\"form\":\"CREDIT\",\"termDays\":30,\"dueDate\":\"1999-01-01\"}")))
                .andExpect(status().isOk());

        ArgumentCaptor<InvoiceDraftCommand> captor = ArgumentCaptor.forClass(InvoiceDraftCommand.class);
        verify(saveUseCase).update(eq(ENTERPRISE_ID), eq(TYPE), eq(doc.id()), captor.capture());
        InvoiceDraftCommand.PaymentCommand payment = captor.getValue().payment();
        assertThat(payment.paymentMethodId()).isEqualTo(7L);
        assertThat(InvoiceDraftCommand.PaymentCommand.class.getRecordComponents()).hasSize(1);
    }

    @Test
    void post_without_payment_sends_no_payment_command() throws Exception {
        BillingDocument doc = withVersion(readySaleDraft(), 0L);
        when(saveUseCase.create(eq(ENTERPRISE_ID), eq(TYPE), any(InvoiceDraftCommand.class))).thenReturn(doc);

        mockMvc.perform(post(BASE).param("enterpriseId", ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(body(null)))
                .andExpect(status().isCreated());

        ArgumentCaptor<InvoiceDraftCommand> captor = ArgumentCaptor.forClass(InvoiceDraftCommand.class);
        verify(saveUseCase).create(eq(ENTERPRISE_ID), eq(TYPE), captor.capture());
        assertThat(captor.getValue().payment()).isNull();
    }

    @Test
    void the_response_exposes_the_cash_method_with_its_frozen_name() throws Exception {
        BillingDocument doc = saleWith(methodTerms(7L));
        doc.freezePaymentMethodName("Efectivo");
        when(getUseCase.get(ENTERPRISE_ID, TYPE, doc.id())).thenReturn(new BillingDocumentView(doc, null, null));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get(BASE + "/" + doc.id()).param("enterpriseId", ENTERPRISE_ID))
                .andExpect(jsonPath("$.payment.methodId").value("7"))
                .andExpect(jsonPath("$.payment.methodName").value("Efectivo"))
                .andExpect(jsonPath("$.payment.form").doesNotExist())
                .andExpect(jsonPath("$.payment.termDays").doesNotExist())
                .andExpect(jsonPath("$.payment.dueDate").doesNotExist());
    }

    @Test
    void the_response_exposes_only_method_id_and_name_without_bank_fields() throws Exception {
        BillingDocument doc = saleWith(co.unicauca.edu.co.contables.billing.domain.model.PaymentTerms
                .draft(7L));
        doc.freezePayment("Transferencia bancaria");
        when(getUseCase.get(ENTERPRISE_ID, TYPE, doc.id())).thenReturn(new BillingDocumentView(doc, null, null));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get(BASE + "/" + doc.id()).param("enterpriseId", ENTERPRISE_ID))
                .andExpect(jsonPath("$.payment.methodId").value("7"))
                .andExpect(jsonPath("$.payment.methodName").value("Transferencia bancaria"))
                .andExpect(jsonPath("$.payment.bankAccountId").doesNotExist())
                .andExpect(jsonPath("$.payment.bankAccountName").doesNotExist());
    }

    @Test
    void a_sale_without_payment_returns_a_null_payment() throws Exception {
        BillingDocument doc = saleWith(null);
        when(getUseCase.get(ENTERPRISE_ID, TYPE, doc.id())).thenReturn(new BillingDocumentView(doc, null, null));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get(BASE + "/" + doc.id()).param("enterpriseId", ENTERPRISE_ID))
                .andExpect(jsonPath("$.payment").isEmpty());
    }

    @Test
    void a_malformed_method_id_is_a_400_validation_error() throws Exception {
        mockMvc.perform(post(BASE).param("enterpriseId", ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(body("{\"methodId\":\"abc\"}")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void the_payment_error_codes_are_422() throws Exception {
        for (BillingErrorCode code : List.of(BillingErrorCode.BILLING_PAYMENT_METHOD_REQUIRED, BillingErrorCode.BILLING_PAYMENT_METHOD_INVALID)) {
            org.mockito.Mockito.reset(emitUseCase);
            java.util.UUID id = java.util.UUID.randomUUID();
            when(emitUseCase.emit(eq(ENTERPRISE_ID), eq(TYPE), eq(id), any()))
                    .thenThrow(new BillingException(code, "mensaje claro"));

            mockMvc.perform(post(BASE + "/" + id + "/emit").param("enterpriseId", ENTERPRISE_ID)
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value(code.name()));
        }
    }
}
