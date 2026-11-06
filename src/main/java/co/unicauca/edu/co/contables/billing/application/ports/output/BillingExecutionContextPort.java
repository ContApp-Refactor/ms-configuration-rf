package co.unicauca.edu.co.contables.billing.application.ports.output;

/** Contexto de ejecucion: tenant y usuario actuales, y ejecucion en nombre de otro tenant. */
public interface BillingExecutionContextPort {

    /** @throws IllegalStateException si no hay tenant en el contexto actual. */
    String tenantId();

    /** Usuario autenticado, o "system" si no hay autenticacion. */
    String username();

    /** Ejecuta la accion con el tenant dado y restaura el contexto previo al terminar. */
    void runAsTenant(String tenantId, Runnable action);
}
