package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceStatus;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonProduct;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonFactureRepository;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductCostPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Mantiene {@code Product.cost} al día con el último precio de compra (HU4).
 *
 * La fuente de verdad son las líneas de las facturas de compra vigentes
 * ({@code SkeletonProduct.unitPrice}), no el kardex: el kardex solo registra
 * productos inventariables, de modo que un producto comprable no inventariable
 * quedaría sin precio si se consultara allí.
 *
 * El cálculo corre únicamente cuando una compra se crea, se edita o se anula;
 * la lectura del precio en las pantallas sigue siendo una lectura de campo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseProductCostSynchronizer {

    private final SkeletonFactureRepository factureRepository;
    private final IProductCostPort productCostPort;

    /**
     * @brief Identificadores de producto presentes en la factura informada
     *
     * Función pura: quien edita una factura la invoca antes y después de la
     * modificación para detectar qué líneas se eliminaron, de modo que esos
     * productos también se resincronicen contra sus compras vigentes.
     *
     * @param facture factura a examinar (puede ser null)
     * @return conjunto de identificadores, vacío si no hay líneas
     */
    public static Set<Long> productIdsOf(SkeletonFacture facture) {
        Set<Long> productIds = new LinkedHashSet<>();
        if (facture == null || facture.getProducts() == null) {
            return productIds;
        }
        for (SkeletonProduct line : facture.getProducts()) {
            if (line != null && line.getProductId() != null) {
                productIds.add(line.getProductId());
            }
        }
        return productIds;
    }

    /**
     * @brief Recalcula el costo de cada producto que figura en la factura informada
     * @param facture factura de compra recién guardada o anulada
     */
    public void syncProducts(SkeletonFacture facture) {
        syncProducts(productIdsOf(facture));
    }

    /**
     * @brief Recalcula el costo de los productos indicados
     * @param productIds identificadores de los productos a sincronizar
     */
    public void syncProducts(Collection<Long> productIds) {
        if (productIds == null) {
            return;
        }
        productIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(this::syncProduct);
    }

    /**
     * @brief Recalcula el costo de un producto desde su última compra vigente
     *
     * Si el producto ya no tiene compras vigentes (por ejemplo, se anuló la
     * única), se conserva el costo actual: la HU solo gobierna el precio
     * cuando existe una compra que lo respalde.
     *
     * @param productId identificador del producto
     */
    public void syncProduct(Long productId) {
        if (productId == null) {
            return;
        }
        Optional<Double> lastPurchasePrice = factureRepository
                .findLastPurchaseUnitPrice(
                        productId,
                        SkeletonFactureType.PURCHASE,
                        PurchaseInvoiceStatus.VOIDED,
                        PageRequest.of(0, 1))
                .stream()
                .findFirst();

        if (lastPurchasePrice.isPresent()) {
            productCostPort.updateLastPurchaseCost(productId, lastPurchasePrice.get());
        } else {
            log.debug("Producto {} sin compras vigentes; se conserva el costo actual", productId);
        }
    }
}
