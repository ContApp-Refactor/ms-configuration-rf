package co.unicauca.edu.co.contables.auxiliary.application.ports.in.log;

import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;

/**
 * @brief Puerto de entrada para la escritura de logs del libro auxiliar.
 *
 * Define el contrato, dentro de la capa de aplicación, para registrar
 * entradas de log asociadas al ciclo de vida y a la generación de los
 * libros auxiliares.
 */
public interface IAuxiliaryBookLogCommandPort {
    /**
     * @brief Registra una nueva entrada de log de libro auxiliar.
     * @param auxiliaryBookLog Registro de log a persistir.
     * @return Log registrado con los identificadores generados.
     */
    AuxiliaryBookLog registerAuxiliaryBookLog(AuxiliaryBookLog auxiliaryBookLog);
}
