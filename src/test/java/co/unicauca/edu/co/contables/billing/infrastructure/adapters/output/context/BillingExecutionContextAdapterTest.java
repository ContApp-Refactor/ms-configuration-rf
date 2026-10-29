package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/** TEN-5: contexto de ejecucion sin dependencia de token. */
class BillingExecutionContextAdapterTest {

    private final BillingExecutionContextAdapter adapter = new BillingExecutionContextAdapter();

    @BeforeEach
    @AfterEach
    void cleanContexts() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void TEN_5_tenantId_returns_current_tenant() {
        TenantContext.setTenantId("tenant-a");

        assertThat(adapter.tenantId()).isEqualTo("tenant-a");
    }

    @Test
    void TEN_5_tenantId_null_throws_illegal_state() {
        assertThatThrownBy(adapter::tenantId).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void TEN_5_tenantId_blank_throws_illegal_state() {
        TenantContext.setTenantId("   ");

        assertThatThrownBy(adapter::tenantId).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void TEN_5_username_falls_back_to_system_without_authentication() {
        assertThat(adapter.username()).isEqualTo("system");
    }

    @Test
    void TEN_5_username_uses_authentication_name() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("maria", "n/a", List.of()));

        assertThat(adapter.username()).isEqualTo("maria");
    }

    @Test
    void TEN_5_runAsTenant_sets_tenant_inside_and_clears_after_when_none_before() {
        List<String> seen = new ArrayList<>();

        adapter.runAsTenant("tenant-b", () -> seen.add(TenantContext.getTenantId()));

        assertThat(seen).containsExactly("tenant-b");
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void TEN_5_runAsTenant_restores_previous_tenant() {
        TenantContext.setTenantId("tenant-a");

        adapter.runAsTenant("tenant-b", () -> assertThat(TenantContext.getTenantId()).isEqualTo("tenant-b"));

        assertThat(TenantContext.getTenantId()).isEqualTo("tenant-a");
    }

    @Test
    void TEN_5_runAsTenant_restores_previous_tenant_even_when_action_throws() {
        TenantContext.setTenantId("tenant-a");

        assertThatThrownBy(() -> adapter.runAsTenant("tenant-b", () -> {
            throw new IllegalArgumentException("boom");
        })).isInstanceOf(IllegalArgumentException.class);

        assertThat(TenantContext.getTenantId()).isEqualTo("tenant-a");
    }

    @Test
    void TEN_5_runAsTenant_clears_when_action_throws_and_no_previous_tenant() {
        assertThatThrownBy(() -> adapter.runAsTenant("tenant-b", () -> {
            throw new IllegalArgumentException("boom");
        })).isInstanceOf(IllegalArgumentException.class);

        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void uuid_generator_returns_distinct_ids() {
        BillingUuidGeneratorAdapter generator = new BillingUuidGeneratorAdapter();

        UUID first = generator.newId();
        UUID second = generator.newId();

        assertThat(first).isNotNull().isNotEqualTo(second);
    }

    @Test
    void clock_uses_configured_zone_for_today() {
        BillingClockAdapter clock = new BillingClockAdapter("America/Bogota");

        assertThat(clock.now()).isNotNull();
        assertThat(clock.today()).isEqualTo(java.time.LocalDate.now(java.time.ZoneId.of("America/Bogota")));
    }
}
