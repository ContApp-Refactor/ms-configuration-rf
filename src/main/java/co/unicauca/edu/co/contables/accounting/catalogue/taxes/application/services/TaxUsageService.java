package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxUsageInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxUsageOutputPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * @brief Servicio para gestión de uso de impuestos
 *
 * Implementa el caso de uso de incremento de contador de uso.
 * Mantiene la separación arquitectónica entre adaptadores de entrada (listeners)
 * y adaptadores de salida (persistencia).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaxUsageService implements ITaxUsageInputPort {

    private final ITaxUsageOutputPort taxUsageOutputPort;

    @Override
    public void incrementUsageCount(Long taxId, String enterpriseId) {
        log.info("Incrementing usage count for taxId: {} in enterprise: {}", taxId, enterpriseId);

        taxUsageOutputPort.incrementUsageCount(taxId, enterpriseId);

        log.info("Usage count incremented successfully for taxId: {}", taxId);
    }
}
