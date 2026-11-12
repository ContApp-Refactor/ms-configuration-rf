package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.counterpartyFor;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import co.unicauca.edu.co.contables.billing.domain.model.Withholding;
import co.unicauca.edu.co.contables.billing.domain.model.WithholdingCode;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.controller.BillingSaleInvoiceController;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.support.BillingInvoiceEndpointSupport;
import java.util.List;
import java.util.UUID;
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

/** Retenciones por REST: request {@code withholdings}, response {@code withholdings} + totales netos y errores 422. */
@WebMvcTest(controllers = BillingSaleInvoiceController.class)
@ContextConfiguration(classes = {BillingSaleInvoiceController.class, BillingInvoiceEndpointSupport.class,
        BillingWebMvcTestConfig.class})
@ActiveProfiles("test")
@WithMockUser(authorities = {"BFV#R", "BFV#C", "BFV#U", "BFV#D", "BFV#EM", "BFV#AN"})
class BillingInvoiceWithholdingRestTest {

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

    private static String body(String withholdings) {
        return "{\"clientId\":\"10\",\"date\":\"2026-01-15\",\"lines\":[{\"productId\":\"1\",\"cantidad\":2}],"
                + "\"version\":3,\"payment\":{\"methodId\":1}"
                + (withholdings == null ? "" : ",\"withholdings\":" + withholdings) + "}";
    }

    private static BillingDocument exampleSale() {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(counterpartyFor(TYPE), TODAY, null,
                List.of(lineOf(1, product(1L, "10000.00", "15"), "2")), methodTerms(1L), List.of(
                        new Withholding(WithholdingCode.RETE_ICA, 3L, "c", rate("20")),
                        new Withholding(WithholdingCode.RETE_FUENTE, 2L, "b", rate("10"))), NOW, USER);
        return withVersion(doc, 1L);
    }

    @Test
    void put_maps_code_and_tax_id_into_withholding_commands_in_order() throws Exception {
        BillingDocument doc = exampleSale();
        when(saveUseCase.update(eq(ENTERPRISE_ID), eq(TYPE), eq(doc.id()), any(InvoiceDraftCommand.class)))
                .thenReturn(doc);

        mockMvc.perform(put(BASE + "/" + doc.id()).param("enterpriseId", ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[{\"code\":\"RETE_ICA\",\"taxId\":3},{\"code\":\"rete_fuente\",\"taxId\":\"2\"}]")))
                .andExpect(status().isOk());

        ArgumentCaptor<InvoiceDraftCommand> captor = ArgumentCaptor.forClass(InvoiceDraftCommand.class);
        verify(saveUseCase).update(eq(ENTERPRISE_ID), eq(TYPE), eq(doc.id()), captor.capture());
        assertThat(captor.getValue().withholdings()).containsExactly(
                new InvoiceDraftCommand.WithholdingCommand(WithholdingCode.RETE_ICA, 3L),
                new InvoiceDraftCommand.WithholdingCommand(WithholdingCode.RETE_FUENTE, 2L));
    }

    @Test
    void post_without_withholdings_sends_an_empty_list() throws Exception {
        BillingDocument doc = withVersion(exampleSale(), 0L);
        when(saveUseCase.create(eq(ENTERPRISE_ID), eq(TYPE), any(InvoiceDraftCommand.class))).thenReturn(doc);

        mockMvc.perform(post(BASE).param("enterpriseId", ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(body(null)))
                .andExpect(status().isCreated());

        ArgumentCaptor<InvoiceDraftCommand> captor = ArgumentCaptor.forClass(InvoiceDraftCommand.class);
        verify(saveUseCase).create(eq(ENTERPRISE_ID), eq(TYPE), captor.capture());
        assertThat(captor.getValue().withholdings()).isEmpty();
    }

    @Test
    void an_unknown_code_is_a_422_with_its_own_error_code_and_never_reaches_the_use_case() throws Exception {
        mockMvc.perform(post(BASE).param("enterpriseId", ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[{\"code\":\"RETE_TODO\",\"taxId\":3}]")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BILLING_WITHHOLDING_INVALID"));

        verifyNoInteractions(saveUseCase);
    }

    @Test
    void a_missing_code_is_a_422() throws Exception {
        mockMvc.perform(post(BASE).param("enterpriseId", ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[{\"taxId\":3}]")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BILLING_WITHHOLDING_INVALID"));
    }

    @Test
    void a_malformed_tax_id_is_a_400() throws Exception {
        mockMvc.perform(post(BASE).param("enterpriseId", ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[{\"code\":\"RETE_ICA\",\"taxId\":\"abc\"}]")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void the_response_exposes_withholdings_and_net_totals() throws Exception {
        BillingDocument doc = exampleSale();
        when(getUseCase.get(ENTERPRISE_ID, TYPE, doc.id())).thenReturn(new BillingDocumentView(doc, null, null));

        mockMvc.perform(get(BASE + "/" + doc.id()).param("enterpriseId", ENTERPRISE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.withholdings.length()").value(2))
                .andExpect(jsonPath("$.withholdings[0].code").value("RETE_ICA"))
                .andExpect(jsonPath("$.withholdings[0].taxId").value("3"))
                .andExpect(jsonPath("$.withholdings[0].taxName").value("c"))
                .andExpect(jsonPath("$.withholdings[0].ratePct").value(20.0))
                .andExpect(jsonPath("$.withholdings[0].amountCents").value(400000))
                .andExpect(jsonPath("$.withholdings[1].code").value("RETE_FUENTE"))
                .andExpect(jsonPath("$.withholdings[1].amountCents").value(200000))
                .andExpect(jsonPath("$.totals.subtotalCents").value(2000000))
                .andExpect(jsonPath("$.totals.ivaCents").value(300000))
                .andExpect(jsonPath("$.totals.withholdingsCents").value(600000))
                .andExpect(jsonPath("$.totals.totalCents").value(1700000));
    }

    @Test
    void a_document_without_withholdings_returns_an_empty_list_and_zero_withholdings() throws Exception {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(counterpartyFor(TYPE), TODAY, null,
                List.of(lineOf(1, product(1L, "10000.00", "15"), "2")), methodTerms(1L), NOW, USER);
        when(getUseCase.get(ENTERPRISE_ID, TYPE, doc.id())).thenReturn(new BillingDocumentView(doc, null, null));

        mockMvc.perform(get(BASE + "/" + doc.id()).param("enterpriseId", ENTERPRISE_ID))
                .andExpect(jsonPath("$.withholdings.length()").value(0))
                .andExpect(jsonPath("$.totals.withholdingsCents").value(0))
                .andExpect(jsonPath("$.totals.totalCents").value(2300000));
    }

    @Test
    void the_withholding_error_codes_are_422() throws Exception {
        for (BillingErrorCode code : List.of(BillingErrorCode.BILLING_WITHHOLDING_INVALID,
                BillingErrorCode.BILLING_WITHHOLDING_TAX_INVALID, BillingErrorCode.BILLING_TOTAL_NEGATIVE)) {
            org.mockito.Mockito.reset(emitUseCase);
            UUID id = UUID.randomUUID();
            when(emitUseCase.emit(eq(ENTERPRISE_ID), eq(TYPE), eq(id), any()))
                    .thenThrow(new BillingException(code, "mensaje claro"));

            mockMvc.perform(post(BASE + "/" + id + "/emit").param("enterpriseId", ENTERPRISE_ID)
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value(code.name()));
        }
    }
}
