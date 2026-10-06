package co.unicauca.edu.co.contables.auxiliary.application.ports.out;

import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;

import java.util.List;

/**
 * @brief Puerto de salida hacia el servicio externo de información contable.
 *
 * Define el contrato para consumir el microservicio que expone los
 * datos contables (cuentas, movimientos y comprobantes) utilizados
 * como fuente para generar los libros auxiliares.
 */
public interface IAccountingInfoClient {
    /**
     * @brief Obtiene toda la información contable disponible.
     * @return Lista con la información contable entregada por el servicio externo.
     */
    List<AccountingInfo> getAllAccountInfo();
}
