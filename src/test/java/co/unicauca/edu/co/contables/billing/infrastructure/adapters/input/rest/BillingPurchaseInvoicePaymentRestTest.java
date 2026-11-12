package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.counterpartyFor;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
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
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.controller.BillingPurchaseInvoiceController;
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

/** Forma de pago por REST en la factura de compra: request, response y codigos de error (422). */
@WebMvcTest(controllers = BillingPurchaseInvoiceController.class)
@ContextConfiguration(classes = {BillingPurchaseInvoiceController.class, BillingInvoiceEndpointSupport.class,
        BillingWebMvcTestConfig.class})
@ActiveProfiles("test")
@WithMockUser(authorities = {"BFC#R", "BFC#C", "BFC#U", "BFC#D", "BFC#EM", "BFC#AN"})
class BillingPurchaseInvoicePaymentRestTest {

    private static final String BASE = "/api/billing/purchase-invoices";
    private static final DocumentType TYPE = DocumentType.PURCHASE_INVOICE;

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
        return "{\"clientId\":\"20\",\"supplierInvoiceNumber\":\"F-77\",\"date\":\"2026-01-15\",\"lines\":[{\"productId\":\"1\",\"cantidad\":2}],"
                + "\"version\":3" + (payment == null ? "" : ",\"payment\":" + payment) + "}";
    }

    private static BillingDocument purchaseWith(co.unicauca.edu.co.contables.billing.domain.model.PaymentTerms payment) {
        BillingDocument doc = draftPurchase();
        doc.replaceInvoiceDraft(counterpartyFor(TYPE), TODAY, null, List.of(lineOf(1, product(1L), "2")), payment,
                NOW, USER);
        return withVersion(doc, 1L);
    }

    @Test
    void post_passes_the_payment_method_and_the_response_exposes_it() throws Exception {
        BillingDocument doc = purchaseWith(methodTerms(7L));
        doc.freezePaymentMethodName("Efectivo");
        when(saveUseCase.create(eq(ENTERPRISE_ID), eq(TYPE), any(InvoiceDraftCommand.class))).thenReturn(doc);

        mockMvc.perform(post(BASE).param("enterpriseId", ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(body("{\"methodId\":7}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payment.methodId").value("7"))
                .andExpect(jsonPath("$.payment.methodName").value("Efectivo"));

        ArgumentCaptor<InvoiceDraftCommand> captor = ArgumentCaptor.forClass(InvoiceDraftCommand.class);
        verify(saveUseCase).create(eq(ENTERPRISE_ID), eq(TYPE), captor.capture());
        assertThat(captor.getValue().payment().paymentMethodId()).isEqualTo(7L);
    }
}
