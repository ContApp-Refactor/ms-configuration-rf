package co.unicauca.edu.co.contables.accounting.treasury.application.output;

public interface ITransactionRunnerPort {
    void run(Runnable action);
}
