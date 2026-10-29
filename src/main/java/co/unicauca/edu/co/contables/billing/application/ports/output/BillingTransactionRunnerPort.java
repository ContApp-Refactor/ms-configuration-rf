package co.unicauca.edu.co.contables.billing.application.ports.output;

/** Ejecuta una accion en una transaccion nueva e independiente (REQUIRES_NEW). */
public interface BillingTransactionRunnerPort {

    void inNewTransaction(Runnable action);
}
