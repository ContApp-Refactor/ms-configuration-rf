package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination;

/**
 * @brief Avance del coordinador por etapa y módulo (lo usa el proceso de copia
 * para exponer {@code moduloActual}/{@code modulosCompletados}/{@code progreso}).
 *
 * Se invoca en el hilo del coordinador; en RESTORE, DENTRO de la transacción
 * del restore: quien persista el avance debe hacerlo en una transacción
 * aparte ({@code REQUIRES_NEW}) y no debe lanzar excepciones.
 */
public interface BackupProgressListener {

    BackupProgressListener NONE = new BackupProgressListener() {
    };

    enum Stage { EXPORT, RESTORE, VERIFY }

    /** Empieza una etapa con {@code totalModules} módulos. */
    default void onStage(Stage stage, int totalModules) {
    }

    default void onModuleStarted(Stage stage, String moduleKey) {
    }

    default void onModuleCompleted(Stage stage, String moduleKey) {
    }

    static BackupProgressListener orNone(BackupProgressListener listener) {
        return listener == null ? NONE : listener;
    }
}
