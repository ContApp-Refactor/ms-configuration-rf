package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.dto.AccountUsedEventDto;

/**
 * @brief Puerto de entrada para gestión de uso de cuentas contables
 *
 * Define contrato para operaciones relacionadas con el contador de uso de cuentas contables,
 * utilizado cuando otros servicios notifican que han utilizado una cuenta contable.
 */
public interface IAccountCatalogueUsagePort {
    /**
     * @brief Incrementa el contador de uso de una cuenta contable
     * @param accountUsedEvent Evento con información de la cuenta utilizada
     */
    void incrementUsageCount(AccountUsedEventDto accountUsedEvent);
}

