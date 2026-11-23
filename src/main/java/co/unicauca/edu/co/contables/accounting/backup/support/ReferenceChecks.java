package co.unicauca.edu.co.contables.accounting.backup.support;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Comprobaciones de huérfanos para {@code verifyIntegrity} contra tablas de OTROS módulos
 * (terceros, centros de costo, PUC) sin importar sus clases: SQL nativo y explícito por tenant
 * (las consultas nativas no pasan por el filtro {@code @TenantId}).
 */
@Component
@RequiredArgsConstructor
public class ReferenceChecks {

    private final EntityManager entityManager;

    /**
     * Cuenta filas de {@code from ... where} cuyo valor {@code columnExpr} no existe en
     * {@code refTable.refKeyColumn} dentro de la empresa y el tenant actuales. Los {@code null} no cuentan.
     *
     * @param from       tabla (con alias) a revisar, p. ej. {@code "invoice_replica x"}
     * @param where      predicado que acota a la empresa, con parámetros {@code :e} (empresa) y {@code :t} (tenant)
     * @param columnExpr expresión de la FK, p. ej. {@code "x.third_id"}
     */
    public long orphans(String from, String where, String columnExpr, String refTable, String refKeyColumn,
            String refEnterpriseColumn, String enterpriseId) {
        TxSupport.flushIfActive(entityManager);
        String sql = "SELECT COUNT(*) FROM " + from + " WHERE " + where + " AND " + columnExpr
                + " IS NOT NULL AND NOT EXISTS (SELECT 1 FROM " + refTable + " r WHERE r." + refKeyColumn + " = "
                + columnExpr + " AND r." + refEnterpriseColumn + " = :e AND r.tenant_id = :t)";
        Number n = (Number) entityManager.createNativeQuery(sql)
                .setParameter("e", enterpriseId)
                .setParameter("t", TenantContext.getTenantId())
                .getSingleResult();
        return n.longValue();
    }

    public long orphanThirds(String from, String where, String columnExpr, String enterpriseId) {
        return orphans(from, where, columnExpr, "THIRDS", "th_id", "ent_id", enterpriseId);
    }

    public long orphanCostCenters(String from, String where, String columnExpr, String enterpriseId) {
        return orphans(from, where, columnExpr, "cost_centers", "id", "id_enterprise", enterpriseId);
    }

    public long orphanAccountIds(String from, String where, String columnExpr, String enterpriseId) {
        return orphans(from, where, columnExpr, "account", "id", "id_enterprise", enterpriseId);
    }

    /** FK por CÓDIGO de cuenta (columnas numéricas que guardan el código del PUC). */
    public long orphanAccountCodes(String from, String where, String numericColumnExpr, String enterpriseId) {
        return orphans(from, where, "CAST(" + numericColumnExpr + " AS VARCHAR(100))", "account", "code",
                "id_enterprise", enterpriseId);
    }
}
