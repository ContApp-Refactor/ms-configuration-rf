package co.unicauca.edu.co.contables.configuration.enterprise.domain.enums;

/**
 * @brief Estado de un proceso de copia, tal como lo consume el frontend
 * (Hito 6): {@code CopyProcess.estado}.
 */
public enum CopyProcessEstadoEnum {
    EN_PROCESO,
    COMPLETADO,
    ERROR,
    FALLIDO,
    CANCELADO
}
