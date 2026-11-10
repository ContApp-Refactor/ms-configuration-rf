package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.unicauca.edu.co.contables.billing.application.ports.input.ListPaymentCatalogUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.output.PaymentCatalogPort.PaymentMethodRef;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.controller.BillingPaymentCatalogController;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Selector de pago: metodos activos con id y nombre (sin cuentas bancarias). */
@WebMvcTest(controllers = BillingPaymentCatalogController.class)
@ContextConfiguration(classes = {BillingPaymentCatalogController.class, BillingWebMvcTestConfig.class})
@ActiveProfiles("test")
class BillingPaymentCatalogControllerTest {

    private static final String BASE = BillingPaymentCatalogController.BASE_PATH;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListPaymentCatalogUseCase useCase;

    @Test
    @WithMockUser
    void methods_expose_id_and_name_only() throws Exception {
        when(useCase.paymentMethods(ENTERPRISE_ID)).thenReturn(List.of(
                new PaymentMethodRef(1L, "Efectivo", true),
                new PaymentMethodRef(2L, "Transferencia bancaria", true)));

        mockMvc.perform(get(BASE + "/methods").param("enterpriseId", ENTERPRISE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Efectivo"))
                .andExpect(jsonPath("$[0].requiresBankAccount").doesNotExist())
                .andExpect(jsonPath("$[1].name").value("Transferencia bancaria"))
                .andExpect(jsonPath("$[1].requiresBankAccount").doesNotExist());
    }

    @Test
    @WithMockUser
    void the_bank_accounts_endpoint_no_longer_exists() throws Exception {
        mockMvc.perform(get(BASE + "/bank-accounts").param("enterpriseId", ENTERPRISE_ID))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser
    void a_missing_enterprise_is_a_400() throws Exception {
        mockMvc.perform(get(BASE + "/methods")).andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }

    @Test
    void without_credentials_the_endpoint_is_401() throws Exception {
        mockMvc.perform(get(BASE + "/methods").param("enterpriseId", ENTERPRISE_ID))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(useCase);
    }
}
