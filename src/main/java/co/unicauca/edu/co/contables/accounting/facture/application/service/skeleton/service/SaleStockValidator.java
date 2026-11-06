package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonFactureRequestDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonProductRequestDto;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Valida que la venta tenga stock suficiente en el kardex (HU5).
 *
 * La regla se aplica ANTES de crear la factura: el kardex rechaza el movimiento
 * en diferido, pero para entonces el documento ya existe y queda sin poder
 * cumplirse.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaleStockValidator {

    private final IKardexQueryPort kardexQueryPort;
    private final IMessageServicePort messageService;

    /**
     * @brief Rechaza la venta si alguna línea pide más de lo disponible en el kardex
     * @param request factura de venta a validar
     * @throws IllegalArgumentException si no hay stock suficiente
     */
    public void validate(SkeletonFactureRequestDto request) {
        if (request == null || request.getProducts() == null) {
            return;
        }
        for (SkeletonProductRequestDto line : request.getProducts()) {
            if (line == null || line.getProductId() == null) {
                continue;
            }
            int requested = line.getAmount() == null ? 0 : line.getAmount();
            int available = availableQuantity(line.getProductId());
            if (requested > available) {
                log.info("Venta rechazada por stock insuficiente - producto {}, solicitado {}, disponible {}",
                        line.getProductId(), requested, available);
                throw new IllegalArgumentException(
                        messageService.getMessage(MessageKeys.ERROR_INSUFFICIENT_STOCK, requested, available));
            }
        }
    }

    /**
     * @brief Stock disponible de un producto sumando sus lotes con existencias en el kardex
     * @param productId producto a consultar
     * @return cantidad disponible,0 si no tiene lotes
     */
    private int availableQuantity(Long productId) {
        List<Kardex> lots = kardexQueryPort.getKardexAvailableQuantityByProduct(productId);
        if (lots == null || lots.isEmpty()) {
            return 0;
        }
        return lots.stream().mapToInt(Kardex::getAvailableQuantity).sum();
    }
}
