package co.unicauca.edu.co.contables.commons.backup;

/**
 * @brief Base de las excepciones de dominio de la copia de seguridad.
 *
 * Vive en {@code commons.backup} (shared kernel) porque la lanzan el lector,
 * el escritor y los participantes de todos los módulos; por eso no puede
 * depender de las excepciones de {@code configuration.enterprise}. Cada
 * subclase declara su {@code @ResponseStatus}; el advice del módulo enterprise
 * debe mapear esta jerarquía (si no, {@code GlobalExceptionHandler} la
 * convierte en 500 genérico).
 */
public abstract class BackupException extends RuntimeException {

    protected BackupException(String message) {
        super(message);
    }

    protected BackupException(String message, Throwable cause) {
        super(message, cause);
    }
}
