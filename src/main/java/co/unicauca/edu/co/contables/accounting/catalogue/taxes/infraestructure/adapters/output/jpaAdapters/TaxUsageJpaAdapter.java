package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxUsageOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * @brief Adaptador JPA para gestión de uso de impuestos
 *
 * Implementa las operaciones de persistencia para el contador de uso de impuestos
 * cuando otros servicios notifican el uso de un impuesto.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaxUsageJpaAdapter implements ITaxUsageOutputPort {

    private final ITaxRepository taxRepository;

    /**
     * @brief Incrementa el contador de uso de un impuesto
     * @param taxId ID del impuesto
     * @param enterpriseId ID de la empresa
     */
    @Override
    public void incrementUsageCount(Long taxId, String enterpriseId) {
        log.debug("Incrementing usage count for taxId: {} in enterprise: {}", taxId, enterpriseId);

        taxRepository.incrementUsageCount(taxId, enterpriseId);

        log.debug("Usage count incremented for taxId: {}", taxId);
    }
}
