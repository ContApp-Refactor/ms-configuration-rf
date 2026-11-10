package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Precio sugerido de compra de un producto (FACTURA DE COMPRA) segun el metodo de valoracion de la
 * empresa: en {@code PEPS}, el {@code unitPrice} de la entrada comercial mas reciente (PURCHASE o
 * ADJUSTMENTENTRY); en {@code WEIGHTED_AVERAGE}, el costo promedio ponderado vigente (columna
 * {@code balance_unit_price} de la fila mas reciente del kardex con balance positivo). Cada metodo
 * tiene su propio kardex.
 *
 * <p>Es una consulta de solo lectura por puertos de entrada de Inventario y NUNCA rompe la factura:
 * si el metodo es nulo, la empresa es invalida/inexistente, el producto no tiene entradas o la
 * consulta falla, el producto simplemente no aparece en el resultado y quien llama usa el
 * {@code cost} del catalogo. El precio del kardex (pesos, escala 2) se convierte con
 * {@link Money#of} (HALF_UP a centavos, sin flotantes). Un precio nulo, cero o negativo se descarta (cae al {@code cost}).
 */
@Component("billingLastPurchasePriceLookup")
public class BillingLastPurchasePriceLookup {

    private static final Logger log = LoggerFactory.getLogger(BillingLastPurchasePriceLookup.class);

    private final IEnterpriseService enterpriseService;
    private final co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexQueryPort
            weightedAverageKardex;
    private final co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort pepsKardex;

    public BillingLastPurchasePriceLookup(
            IEnterpriseService enterpriseService,
            co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexQueryPort
                    weightedAverageKardex,
            co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort pepsKardex) {
        this.enterpriseService = enterpriseService;
        this.weightedAverageKardex = weightedAverageKardex;
        this.pepsKardex = pepsKardex;
    }

    /**
     * Precio sugerido de compra (ultimo en PEPS, promedio vigente en promedio ponderado) de cada producto que lo tenga (el metodo de valoracion se resuelve una
     * sola vez). Los productos sin precio o ante cualquier fallo quedan fuera del mapa.
     */
    public Map<Long, Money> findAll(String enterpriseId, Collection<Long> productIds) {
        Map<Long, Money> prices = new LinkedHashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            return prices;
        }
        InventoryConfigurationTypeEnum method = resolveMethod(enterpriseId);
        if (method == null) {
            return prices;
        }
        for (Long productId : productIds) {
            find(method, productId).ifPresent(price -> prices.put(productId, price));
        }
        return prices;
    }

    private Optional<Money> find(InventoryConfigurationTypeEnum method, Long productId) {
        try {
            Optional<BigDecimal> price = method == InventoryConfigurationTypeEnum.PEPS
                    ? pepsKardex.findLastEntryUnitPrice(productId)
                    : weightedAverageKardex.findCurrentAverageUnitCost(productId);
            if (price == null || price.isEmpty() || price.get().signum() <= 0) {
                return Optional.empty();
            }
            return Optional.of(Money.of(price.get()));
        } catch (RuntimeException ex) {
            log.warn("No se pudo consultar el precio sugerido de compra del producto {}; se usa el costo del catalogo",
                    productId, ex);
            return Optional.empty();
        }
    }

    private InventoryConfigurationTypeEnum resolveMethod(String enterpriseId) {
        try {
            Enterprise enterprise = enterpriseService.findById(UUID.fromString(enterpriseId.trim()));
            return enterprise == null ? null : enterprise.getInventoryConfigurationType();
        } catch (RuntimeException ex) {
            log.warn("No se pudo resolver el metodo de valoracion de la empresa {}; se usa el costo del catalogo",
                    enterpriseId, ex);
            return null;
        }
    }
}
