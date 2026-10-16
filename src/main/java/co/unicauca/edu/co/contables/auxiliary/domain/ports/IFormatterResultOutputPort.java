package co.unicauca.edu.co.contables.auxiliary.domain.ports;

/**
 * @brief Puerto de salida para formatear y emitir respuestas de error.
 *
 * Define el contrato para formatear y entregar distintos tipos de
 * respuestas de error hacia la capa cliente/llamadora.
 */
public interface IFormatterResultOutputPort {
    /**
     * @brief Emite una respuesta de error por regla de negocio.
     * @param status Código HTTP a retornar.
     * @param message Mensaje de error a incluir.
     */
    public void returnBusinessRuleErrorResponse(int status, String message);

    /**
     * @brief Emite una respuesta de error por entidad ya existente.
     * @param status Código HTTP a retornar.
     * @param message Mensaje de error a incluir.
     */
    public void returnEntityAlreadyExistsErrorResponse(int status, String message);

    /**
     * @brief Emite una respuesta de error por entidad inexistente.
     * @param status Código HTTP a retornar.
     * @param message Mensaje de error a incluir.
     */
    public void returnEntityDoesNotExistErrorResponse(int status, String message);

    /**
     * @brief Emite una respuesta de error genérica.
     * @param status Código HTTP a retornar.
     * @param message Mensaje de error a incluir.
     */
    public void returnErrorGenericResponse(int status, String message);
}
