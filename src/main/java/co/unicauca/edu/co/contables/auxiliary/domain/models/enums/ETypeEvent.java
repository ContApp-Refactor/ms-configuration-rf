package co.unicauca.edu.co.contables.auxiliary.domain.models.enums;

/**
 * @brief Enumeración de tipos de eventos registrados en los logs del libro auxiliar.
 *
 * Cubre el ciclo completo: registro, generación de datos, exportación,
 * programación y envío, incluyendo estados de éxito y error por etapa.
 */
public enum ETypeEvent {
    REGISTERED,
    ERROR_GENERATION,
    GENERATING,
    SUCCESS_GENERATION,
    ERROR_EXPORTING,
    SUCCESSFUL_EXPORTING,
    EXPORTING,
    ERROR_SCHEDULING,
    SCHEDULING,
    SUCCESSFUL_SCHEDULING,
    ERROR_SENT,
    SENDING,
    SUCCESSFUL_SENT,
}
