package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.context;

import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Contexto de ejecucion basado en {@link TenantContext} y SecurityContext.
 * No usa IJwtUtils: debe funcionar fuera de una peticion HTTP (relay del outbox).
 */
@Component("billingExecutionContextAdapter")
public class BillingExecutionContextAdapter implements BillingExecutionContextPort {

    @Override
    public String tenantId() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("No hay tenant en el contexto de ejecucion");
        }
        return tenantId;
    }

    @Override
    public String username() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "system";
        }
        return authentication.getName();
    }

    @Override
    public void runAsTenant(String tenantId, Runnable action) {
        String previous = TenantContext.getTenantId();
        TenantContext.setTenantId(tenantId);
        try {
            action.run();
        } finally {
            if (previous == null) {
                TenantContext.clear();
            } else {
                TenantContext.setTenantId(previous);
            }
        }
    }
}
