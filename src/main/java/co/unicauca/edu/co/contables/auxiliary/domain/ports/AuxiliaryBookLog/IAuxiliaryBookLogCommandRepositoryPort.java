package co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookLog;

import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;

/**
 * @brief Puerto de salida para operaciones de escritura de logs del libro auxiliar.
 *
 * Define el contrato para persistir eventos asociados al libro auxiliar
 * (registro, generación, exportación, errores) en el almacenamiento.
 */
public interface IAuxiliaryBookLogCommandRepositoryPort {
    /**
     * @brief Persiste un registro de log del libro auxiliar.
     * @param auxiliaryBookLog Registro de log a guardar.
     * @return El log guardado con los identificadores generados.
     */
    AuxiliaryBookLog registerAuxiliaryBookLog(AuxiliaryBookLog auxiliaryBookLog);
}
