package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodsNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.billing.application.ports.output.PaymentCatalogPort;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** El adaptador del catalogo de pago llama en proceso a los servicios de dominio de contabilidad. */
@ExtendWith(MockitoExtension.class)
class BillingPaymentCatalogAdapterTest {

    private static final String ENTERPRISE = "11111111-1111-1111-1111-111111111111";

    @Mock
    private IPaymentMethodService paymentMethods;

    private BillingPaymentCatalogAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BillingPaymentCatalogAdapter(paymentMethods);
    }

    @Test
    void an_active_method_is_mapped_with_its_name() {
        when(paymentMethods.findById(7L, ENTERPRISE))
                .thenReturn(PaymentMethod.builder().id(7L).name("Efectivo").status(true).build());

        PaymentCatalogPort.PaymentMethodRef ref = adapter.findPaymentMethod(ENTERPRISE, 7L).orElseThrow();

        assertThat(ref.id()).isEqualTo(7L);
        assertThat(ref.name()).isEqualTo("Efectivo");
        assertThat(ref.active()).isTrue();
    }

    @Test
    void an_inactive_or_null_status_method_is_reported_inactive() {
        when(paymentMethods.findById(7L, ENTERPRISE))
                .thenReturn(PaymentMethod.builder().id(7L).name("Cheque").status(false).build());
        when(paymentMethods.findById(8L, ENTERPRISE))
                .thenReturn(PaymentMethod.builder().id(8L).name("Sin estado").build());

        assertThat(adapter.findPaymentMethod(ENTERPRISE, 7L).orElseThrow().active()).isFalse();
        assertThat(adapter.findPaymentMethod(ENTERPRISE, 8L).orElseThrow().active()).isFalse();
    }

    @Test
    void a_method_not_found_in_the_enterprise_is_empty() {
        when(paymentMethods.findById(9L, ENTERPRISE)).thenThrow(new PaymentMethodsNotFoundException());

        assertThat(adapter.findPaymentMethod(ENTERPRISE, 9L)).isEmpty();
    }

    @Test
    void a_null_method_is_empty_and_other_errors_propagate() {
        when(paymentMethods.findById(1L, ENTERPRISE)).thenReturn(null);
        when(paymentMethods.findById(2L, ENTERPRISE)).thenThrow(new IllegalStateException("boom"));

        assertThat(adapter.findPaymentMethod(ENTERPRISE, 1L)).isEmpty();
        assertThatThrownBy(() -> adapter.findPaymentMethod(ENTERPRISE, 2L)).isInstanceOf(IllegalStateException.class);
    }

    // ---------- sin cuenta bancaria ni regla de banco ----------

    private static PaymentMethod method(long id, String name, String puc, boolean active) {
        return PaymentMethod.builder().id(id).name(name).status(active)
                .accountingAccountEntity(puc == null ? null : AccountCatalogue.builder().id(1L).code(puc).build())
                .build();
    }

    @Test
    void the_adapter_and_the_port_no_longer_know_bank_accounts() {
        assertThat(java.util.Arrays.stream(PaymentCatalogPort.class.getMethods()).map(java.lang.reflect.Method::getName))
                .containsExactlyInAnyOrder("findPaymentMethod", "listActivePaymentMethods");
        assertThat(java.util.Arrays.stream(PaymentCatalogPort.PaymentMethodRef.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)).containsExactly("id", "name", "active");
    }

    @Test
    void the_method_mapping_ignores_the_puc_and_the_name() {
        when(paymentMethods.findById(1L, ENTERPRISE)).thenReturn(method(1L, "Efectivo", "111005", true));
        when(paymentMethods.findById(2L, ENTERPRISE)).thenReturn(method(2L, "Transferencia", "110505", true));

        assertThat(adapter.findPaymentMethod(ENTERPRISE, 1L).orElseThrow().name()).isEqualTo("Efectivo");
        assertThat(adapter.findPaymentMethod(ENTERPRISE, 2L).orElseThrow().active()).isTrue();
    }

    // ---------- listados y etiqueta ----------

    @Test
    void active_methods_are_listed_with_id_and_name_only() {
        when(paymentMethods.findAllActiveByEnterprise(ENTERPRISE, Optional.of(0), Optional.of(500)))
                .thenReturn(new PageImpl<>(List.of(method(1L, "Efectivo", "110505", true),
                        method(2L, "Transferencia bancaria", "111005", true), method(3L, "Viejo", null, false))));

        List<PaymentCatalogPort.PaymentMethodRef> list = adapter.listActivePaymentMethods(ENTERPRISE);

        assertThat(list).extracting(PaymentCatalogPort.PaymentMethodRef::name)
                .containsExactly("Efectivo", "Transferencia bancaria");
        assertThat(list).extracting(PaymentCatalogPort.PaymentMethodRef::id).containsExactly(1L, 2L);
    }
}
