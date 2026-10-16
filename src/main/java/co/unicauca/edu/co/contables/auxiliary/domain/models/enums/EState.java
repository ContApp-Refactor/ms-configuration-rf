package co.unicauca.edu.co.contables.auxiliary.domain.models.enums;

/**
 * @brief Enumeración de estados del ciclo de vida de un libro auxiliar
 * (pendiente, programado, generado, exportado, error, enviado).
 */
public enum EState {
    PENDING,
    SCHEDULED,
    GENERATED,
    EXPORT,
    ERROR,
    SENT,
}
