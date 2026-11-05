package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.domain.model.IssuerSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** EMT-1/EMT-2: el emisor se resuelve en proceso con IEnterpriseService.findById(UUID). */
@ExtendWith(MockitoExtension.class)
class BillingEnterpriseAdapterTest {

    private static final String ENTERPRISE = "11111111-1111-1111-1111-111111111111";

    @Mock
    private IEnterpriseService enterpriseService;

    private BillingEnterpriseAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BillingEnterpriseAdapter(enterpriseService);
    }

    private static Enterprise enterprise(StateEnum state) {
        Enterprise enterprise = new Enterprise();
        enterprise.setId(UUID.fromString(ENTERPRISE));
        enterprise.setName("Empresa Emisora SAS");
        enterprise.setNit("900123456");
        enterprise.setDV("1");
        enterprise.setState(state);
        return enterprise;
    }

    @Test
    void ENTERPRISE_active_enterprise_maps_to_active_issuer() {
        when(enterpriseService.findById(UUID.fromString(ENTERPRISE))).thenReturn(enterprise(StateEnum.ACTIVE));

        IssuerSnapshot issuer = adapter.findIssuer(ENTERPRISE).orElseThrow();

        assertThat(issuer.enterpriseId()).isEqualTo(ENTERPRISE);
        assertThat(issuer.name()).isEqualTo("Empresa Emisora SAS");
        assertThat(issuer.nit()).isEqualTo("900123456");
        assertThat(issuer.dv()).isEqualTo("1");
        assertThat(issuer.active()).isTrue();
    }

    @Test
    void ENTERPRISE_inactive_state_maps_to_inactive_issuer() {
        when(enterpriseService.findById(UUID.fromString(ENTERPRISE))).thenReturn(enterprise(StateEnum.INACTIVE));

        assertThat(adapter.findIssuer(ENTERPRISE).orElseThrow().active()).isFalse();
    }

    @Test
    void ENTERPRISE_suspended_state_is_not_active() {
        when(enterpriseService.findById(UUID.fromString(ENTERPRISE))).thenReturn(enterprise(StateEnum.SUSPENDED));

        assertThat(adapter.findIssuer(ENTERPRISE).orElseThrow().active()).isFalse();
    }

    @Test
    void ENTERPRISE_null_state_is_not_active() {
        when(enterpriseService.findById(UUID.fromString(ENTERPRISE))).thenReturn(enterprise(null));

        assertThat(adapter.findIssuer(ENTERPRISE).orElseThrow().active()).isFalse();
    }

    @Test
    void ENTERPRISE_service_returning_null_becomes_empty() {
        when(enterpriseService.findById(UUID.fromString(ENTERPRISE))).thenReturn(null);

        assertThat(adapter.findIssuer(ENTERPRISE)).isEmpty();
    }

    @Test
    void ENTERPRISE_invalid_uuid_becomes_empty_without_calling_the_service() {
        assertThat(adapter.findIssuer("no-es-un-uuid")).isEmpty();

        verifyNoInteractions(enterpriseService);
    }

    @Test
    void ENTERPRISE_null_or_blank_id_becomes_empty_without_calling_the_service() {
        assertThat(adapter.findIssuer(null)).isEmpty();
        assertThat(adapter.findIssuer("   ")).isEmpty();

        verifyNoInteractions(enterpriseService);
    }
}
