package co.unicauca.edu.co.contables.accounting.backup.support;

import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.persistence.EntityManager;

/**
 * @brief Utilidad mínima: los participantes hacen {@code flush} antes de consultas nativas (que no ven
 * lo pendiente en el contexto de persistencia), pero {@code flush} exige transacción activa y las
 * lecturas de verificación pueden correr fuera de una.
 */
public final class TxSupport {

    private TxSupport() {
    }

    public static void flushIfActive(EntityManager entityManager) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            entityManager.flush();
        }
    }

    /**
     * Ejecuta un {@code DELETE} nativo (sin cargar entidades, sin eventos ni auditoría) con el parámetro
     * {@code :e} = id de empresa. Las sentencias nativas no pasan por el filtro {@code @TenantId}; el
     * acotado por empresa va explícito en el SQL. Devuelve las filas borradas.
     */
    public static int purge(EntityManager entityManager, String sql, String enterpriseId) {
        return entityManager.createNativeQuery(sql).setParameter("e", enterpriseId).executeUpdate();
    }

    /** Sincroniza y limpia el contexto de persistencia tras una tanda de {@code DELETE} directos. */
    public static void flushAndClear(EntityManager entityManager) {
        entityManager.flush();
        entityManager.clear();
    }
}
