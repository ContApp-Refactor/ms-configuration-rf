package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.security;

/**
 * @brief Interfaz utilitaria para extraer información del JWT.
 *
 * Define el contrato para obtener identificadores del usuario o
 * sujeto a partir del token JWT autenticado.
 */
public interface IJwtUtils {
    /**
     * @brief Obtiene el identificador (sub) del JWT.
     * @return Identificador del usuario autenticado.
     */
    String getId();
}
