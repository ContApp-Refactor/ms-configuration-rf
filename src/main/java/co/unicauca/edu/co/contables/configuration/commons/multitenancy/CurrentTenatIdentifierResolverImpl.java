package co.unicauca.edu.co.contables.configuration.commons.multitenancy;

import co.unicauca.edu.co.contables.configuration.commons.multitenancy.utils.TenantContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Map;

@SuppressWarnings("rawtypes")
/** @brief Implementación del resolvedor de identificador de inquilino actual para Hibernate */
@Component
class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    /**
     * @brief Resuelve el identificador de inquilino actual.
     * 
     * @return el identificador de inquilino actual del TenantContext si está
     *         disponible;
     *         de lo contrario, devuelve "BOOTSTRAP" para permitir la inicialización
     *         del EntityManagerFactory.
     */
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        if (!ObjectUtils.isEmpty(tenantId)) {
            return tenantId;
        } else {
            // Allow bootstrapping the EntityManagerFactory, in which case no tenant is
            // needed
            return "BOOTSTRAP";
        }
    }

    
    /**
     * @brief Valida las sesiones actuales existentes.
     * 
     * @return true siempre, ya que las sesiones actuales son consideradas válidas
     *         por defecto.
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    /**
     * @brief Personaliza las propiedades de Hibernate dadas agregando el resolutor de
     * identificador
     * de inquilino actual como el valor de la configuración
     * {@link AvailableSettings#MULTI_TENANT_IDENTIFIER_RESOLVER}.
     * 
     * @param hibernateProperties las propiedades a personalizar
     */
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

}