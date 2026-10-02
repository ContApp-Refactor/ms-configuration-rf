package co.unicauca.edu.co.contables.configuration.enterprise;

import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * Test configuration to provide a working multi-tenancy setup for H2.
 * Uses a fixed tenant ID to avoid schema generation issues with @TenantId.
 */
@Configuration
class TestMultiTenancyConfig {

    @Bean
    @Primary
    CurrentTenantIdentifierResolver testTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolver() {
            @Override
            public String resolveCurrentTenantIdentifier() {
                String tenantId = TenantContext.getTenantId();
                if (!ObjectUtils.isEmpty(tenantId)) {
                    return tenantId;
                }
                // Use a fixed tenant for tests instead of "BOOTSTRAP" to ensure consistent schema
                return "test-tenant";
            }

            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
        };
    }

    @Bean
    @Primary
    HibernatePropertiesCustomizer hibernatePropertiesCustomizer(CurrentTenantIdentifierResolver resolver) {
        return hibernateProperties -> {
            hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
            hibernateProperties.put("hibernate.multiTenancy", "DISCRIMINATOR");
        };
    }
}