package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.billing.application.ports.output.ProductPort;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.TaxRate;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IUnitOfMeasureServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Catalogo de productos resuelto <strong>en el mismo proceso</strong> mediante los puertos de
 * entrada del modulo inventory (D8, H0-1): precio de referencia = {@code Product.cost} (escala 2,
 * HALF_UP) en ventas; en compras (kind PURCHASE) = ultimo precio de compra del kardex
 * ({@link BillingLastPurchasePriceLookup}) con {@code Product.cost} como respaldo; e IVA = el de la categoria del
 * producto segun {@link BillingVatRateResolver} (con {@code billing.default-vat-rate} como respaldo),
 * junto con su desglose por impuesto.
 * Se devuelven tambien los productos inactivos ({@code active=false}); la unidad de medida
 * inexistente solo deja el nombre en {@code null}.
 */
@Component("billingProductAdapter")
public class BillingProductAdapter implements ProductPort {

    private final IProductServicePort productService;
    private final IUnitOfMeasureServicePort unitOfMeasureService;
    private final BillingVatRateResolver vatRateResolver;
    private final BillingLastPurchasePriceLookup lastPurchasePrices;

    public BillingProductAdapter(IProductServicePort productService, IUnitOfMeasureServicePort unitOfMeasureService,
            BillingVatRateResolver vatRateResolver, BillingLastPurchasePriceLookup lastPurchasePrices) {
        this.productService = productService;
        this.unitOfMeasureService = unitOfMeasureService;
        this.vatRateResolver = vatRateResolver;
        this.lastPurchasePrices = lastPurchasePrices;
    }

    @Override
    public Optional<ProductSnapshot> findProduct(String enterpriseId, long productId, DocumentKind kind) {
        Product product = load(enterpriseId, productId);
        if (product == null) {
            return Optional.empty();
        }
        return Optional.of(snapshot(enterpriseId, productId, product,
                vatRateResolver.resolveBreakdown(enterpriseId, product.getCategoryId()),
                purchasePrices(enterpriseId, List.of(productId), kind).get(productId)));
    }

    /**
     * Version por lote (H7): los productos se leen primero y el IVA se resuelve UNA vez para todas
     * las categorias distintas ({@link BillingVatRateResolver#resolveAll}: una consulta de impuestos
     * activos y una por categoria). Nada se conserva entre invocaciones: los impuestos pueden cambiar.
     */
    @Override
    public Map<Long, ProductSnapshot> findProducts(String enterpriseId, Collection<Long> productIds,
            DocumentKind kind) {
        Map<Long, Product> loaded = new LinkedHashMap<>();
        for (Long productId : new LinkedHashSet<>(productIds)) {
            Product product = load(enterpriseId, productId);
            if (product != null) {
                loaded.put(productId, product);
            }
        }
        if (loaded.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Set<Long> categoryIds = new LinkedHashSet<>();
        loaded.values().forEach(product -> categoryIds.add(product.getCategoryId()));
        Map<Long, BillingVatRateResolver.Resolution> rates =
                vatRateResolver.resolveAllBreakdowns(enterpriseId, categoryIds);
        Map<Long, ProductSnapshot> found = new LinkedHashMap<>();
        Map<Long, Money> purchasePrices = purchasePrices(enterpriseId, loaded.keySet(), kind);
        loaded.forEach((productId, product) -> found.put(productId,
                snapshot(enterpriseId, productId, product, rates.get(product.getCategoryId()),
                        purchasePrices.get(productId))));
        return found;
    }

    private Product load(String enterpriseId, long productId) {
        try {
            return productService.findById(productId, enterpriseId);
        } catch (ProductNotFoundException ex) {
            return null;
        }
    }

    /**
     * Compra: ultimo precio de compra del kardex (solo los productos que lo tienen; el resto cae al
     * {@code cost}). Venta u otro tipo: no se consulta el kardex.
     */
    private Map<Long, Money> purchasePrices(String enterpriseId, Collection<Long> productIds, DocumentKind kind) {
        if (kind != DocumentKind.PURCHASE) {
            return Map.of();
        }
        return lastPurchasePrices.findAll(enterpriseId, productIds);
    }

    private ProductSnapshot snapshot(String enterpriseId, long productId, Product product,
            BillingVatRateResolver.Resolution vat, Money lastPurchasePrice) {
        return new ProductSnapshot(
                productId,
                product.getCode(),
                product.getName(),
                product.getUnitOfMeasureId(),
                unitOfMeasureName(product.getUnitOfMeasureId(), enterpriseId),
                lastPurchasePrice != null ? lastPurchasePrice : Money.of(BigDecimal.valueOf(product.getCost())),
                TaxRate.of(vat.rate()),
                product.isState(),
                vat.taxes());
    }

    private String unitOfMeasureName(Long unitOfMeasureId, String enterpriseId) {
        if (unitOfMeasureId == null) {
            return null;
        }
        try {
            UnitOfMeasure unit = unitOfMeasureService.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId);
            return unit == null ? null : unit.getName();
        } catch (UnitOfMeasureNotFoundException ex) {
            return null;
        }
    }
}
