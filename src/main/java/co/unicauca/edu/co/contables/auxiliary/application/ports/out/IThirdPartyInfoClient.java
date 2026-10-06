package co.unicauca.edu.co.contables.auxiliary.application.ports.out;

import co.unicauca.edu.co.contables.auxiliary.domain.models.external.thirdParty.ThirdParty;

/**
 * @brief Puerto de salida hacia el servicio externo de terceros.
 *
 * Define el contrato para consultar el microservicio que expone la
 * información de los terceros (clientes, proveedores, etc.) usados
 * dentro de los libros auxiliares por tercero.
 */
public interface IThirdPartyInfoClient {
    /**
     * @brief Obtiene el tercero asociado a un identificador.
     * @param thirdPartyId Identificador único del tercero.
     * @return Datos del tercero reportados por el servicio externo.
     */
    ThirdParty getThirdPartyById(Long thirdPartyId);
}
