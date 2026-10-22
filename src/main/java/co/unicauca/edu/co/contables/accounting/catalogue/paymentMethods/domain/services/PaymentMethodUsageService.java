package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Servicio para gestión de uso de métodos de pago
 *
 * Maneja la lógica de negocio relacionada con el contador de uso de métodos de pago
 * cuando son utilizados por otros servicios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodUsageService implements IPaymentMethodUsage {

    private final IPaymentMethodService paymentMethodService;

    @Override
    public void incrementUsageCount(Long paymentMethodId, String enterpriseId) {
        log.info("Incrementing usage count for paymentMethodId: {} in enterprise: {}", paymentMethodId, enterpriseId);

        PaymentMethod paymentMethod = paymentMethodService.findById(paymentMethodId, enterpriseId);
        if (paymentMethod == null) {
            log.warn("PaymentMethod not found: {} in enterprise: {}", paymentMethodId, enterpriseId);
            throw new RuntimeException("Método de pago no encontrado: " + paymentMethodId);
        }

        paymentMethod.incrementUsageCount();
        paymentMethodService.updateUsageCount(paymentMethodId, paymentMethod.getUsageCount());

        log.info("Usage count incremented successfully for paymentMethodId: {}. New count: {}",
                 paymentMethodId, paymentMethod.getUsageCount());
    }
}
