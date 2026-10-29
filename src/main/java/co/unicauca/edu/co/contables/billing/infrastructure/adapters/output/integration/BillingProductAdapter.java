package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.billing.application.ports.output.ProductPort;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.TaxRate;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingProperties;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IUnitOfMeasureServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Catalogo de productos resuelto <strong>en el mismo proceso</strong> mediante los puertos de
 * entrada del modulo inventory (D8, H0-1): precio de referencia = {@code Product.cost} (escala 2,
 * HALF_UP) tanto en ventas como en compras (provisional, RN-30) e IVA = {@code billing.default-vat-rate}.
 * Se devuelven tambien los productos inactivos ({@code active=false}); la unidad de medida
 * inexistente solo deja el nombre en {@code null}.
 */
@Component("billingProductAdapter")
public class BillingProductAdapter implements ProductPort {

    private final IProductServicePort productService;
    private final IUnitOfMeasureServicePort unitOfMeasureService;
    private final BillingProperties properties;

    public BillingProductAdapter(IProductServicePort productService, IUnitOfMeasureServicePort unitOfMeasureService,
            BillingProperties properties) {
        this.productService = productService;
        this.unitOfMeasureService = unitOfMeasureService;
        this.properties = properties;
    }

    @Override
    public Optional<ProductSnapshot> findProduct(String enterpriseId, long productId, DocumentKind kind) {
        Product product;
        try {
            product = productService.findById(productId, enterpriseId);
        } catch (ProductNotFoundException ex) {
            return Optional.empty();
        }
        if (product == null) {
            return Optional.empty();
        }
        return Optional.of(new ProductSnapshot(
                productId,
                product.getCode(),
                product.getName(),
                product.getUnitOfMeasureId(),
                unitOfMeasureName(product.getUnitOfMeasureId(), enterpriseId),
                Money.of(BigDecimal.valueOf(product.getCost())),
                TaxRate.of(properties.getDefaultVatRate()),
                product.isState()));
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
