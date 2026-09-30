package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import java.math.BigDecimal;

/**
 * @brief Puerto de salida para operaciones de actualización de cuentas contables
 *
 * Define el contrato para modificar cuentas contables existentes
 * en el repositorio de datos.
 */
public interface IAccountCatalogueUpdateOutputPort {
    /**
     * @brief Actualiza cuenta contable existente en base de datos
     * @param id ID de la cuenta a actualizar
     * @param accountCatalogue datos actualizados de la cuenta
     * @return cuenta actualizada
     */
    AccountCatalogue updateAccountCatalogue(long id,AccountCatalogue accountCatalogue);

    /**
     * @brief Incrementa el contador de uso de forma atómica
     * @param id ID de la cuenta
     * @return cuenta actualizada con el nuevo contador
     */
    AccountCatalogue incrementUsageCount(long id);

    /**
     * @brief Actualiza el monto de una cuenta
     * @param id ID de la cuenta
     * @param amount Nuevo valor del monto
     * @return true si se actualizó correctamente
     */
    boolean updateAmount(long id, BigDecimal amount);
}
