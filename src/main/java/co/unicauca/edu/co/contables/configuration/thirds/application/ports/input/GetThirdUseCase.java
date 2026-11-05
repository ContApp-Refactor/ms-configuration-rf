package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;

/**
 * @brief Caso de uso para consulta individual de terceros
 *
 * Proporciona operaciones para obtener terceros específicos
 * y verificar su existencia en el sistema.
 */
public interface GetThirdUseCase {
    /**
     * @brief Obtiene un tercero por su id y empresa
     * @param id El id del tercero a obtener
     * @param entId El id de la empresa
     * @return El tercero obtenido
     */
    Third getThirdById(Long id, String entId);

    /**
     * @brief Verifica si un tercero existe por su id
     * @param id El id del tercero a verificar
     * @param entId El id de la empresa
     * @return true si el tercero existe, false en caso contrario
     */
    boolean existThirdById(long id, String entId);
}
