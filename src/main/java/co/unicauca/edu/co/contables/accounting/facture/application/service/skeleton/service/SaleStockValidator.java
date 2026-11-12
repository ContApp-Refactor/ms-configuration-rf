package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonFactureRequestDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonProductRequestDto;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductTypeServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
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
    private final IProductServicePort productServicePort;
    private final IProductTypeServicePort productTypeServicePort;

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
            validateLine(request.getEntId(), line.getProductId(), line.getAmount());
        }
    }

    /**
     * @brief Valida una linea de venta contra el kardex
     *
     * Compartido por POST /api/factures/skeleton/sale y por
     * POST /api/factures/ (FactureRestAdapter), que es el camino que usa
     * el front de Factura de Venta.
     *
     * @param enterpriseId empresa de la factura
     * @param productId producto de la linea
     * @param requestedAmount cantidad pedida
     * @throws IllegalArgumentException si no hay stock suficiente
     */
    public void validateLine(String enterpriseId, Long productId, Number requestedAmount) {
        if (productId == null) {
            return;
        }
        if (skipStockCheck(productId, enterpriseId)) {
            log.debug("Producto {} no inventariable: se omite la validacion de stock", productId);
            return;
        }
        int requested = requestedAmount == null ? 0 : requestedAmount.intValue();
        int available = availableQuantity(productId);
        if (requested > available) {
            log.info("Venta rechazada por stock insuficiente - producto {}, solicitado {}, disponible {}",
                    productId, requested, available);
            throw new IllegalArgumentException(
                    messageService.getMessage(MessageKeys.ERROR_INSUFFICIENT_STOCK, requested, available));
        }
    }

    /**
     * @brief Indica si la linea queda fuera del control de stock por no ser inventariable
     *
     * El kardex solo gobierna los productos inventariables (HU1): un producto que no
     * lo es nunca acumula lotes, por lo que validarlo lo dejaría sin venta posible.
     * Si no se puede determinar la clasificación se valida de todos modos.
     *
     * @param productId producto de la linea
     * @param enterpriseId empresa de la factura
     * @return true si el tipo de producto no es inventariable
     */
    private boolean skipStockCheck(Long productId, String enterpriseId) {
        try {
            Product product = productServicePort.findById(productId, enterpriseId);
            if (product == null || product.getProductTypeId() == null) {
                return false;
            }
            ProductType productType =
                    productTypeServicePort.getProductTypeByIdAndEnterpriseId(product.getProductTypeId(), enterpriseId);
            return productType != null && !productType.isInventoryable();
        } catch (RuntimeException e) {
            log.debug("No se pudo determinar si el producto {} es inventariable ({}); se valida stock",
                    productId, e.getMessage());
            return false;
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
