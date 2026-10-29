package co.unicauca.edu.co.contables.billing.application.service;

import co.unicauca.edu.co.contables.billing.application.ports.output.EnterprisePort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ProductPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ThirdPartyPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartySnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.IssuerSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Resuelve referencias a otros modulos (tercero, productos, emisor) y las traduce a errores de
 * negocio. No decide actividad: los productos/terceros inactivos se devuelven tal cual y cada
 * caso de uso decide si los admite (borrador) o los rechaza (emision).
 */
public class BillingReferenceResolver {

    private final ThirdPartyPort thirdParties;
    private final ProductPort products;
    private final EnterprisePort enterprises;

    public BillingReferenceResolver(ThirdPartyPort thirdParties, ProductPort products, EnterprisePort enterprises) {
        this.thirdParties = Objects.requireNonNull(thirdParties, "thirdParties");
        this.products = Objects.requireNonNull(products, "products");
        this.enterprises = Objects.requireNonNull(enterprises, "enterprises");
    }

    /** Tercero con el rol del tipo de documento; {@code COUNTERPARTY_NOT_FOUND} si no existe. */
    public CounterpartySnapshot requireCounterparty(String enterpriseId, long thirdId, DocumentKind kind) {
        return thirdParties.findCounterparty(enterpriseId, thirdId, kind.counterpartyRole())
                .orElseThrow(() -> new BillingException(BillingErrorCode.BILLING_COUNTERPARTY_NOT_FOUND,
                        "El tercero " + thirdId + " no existe"));
    }

    /**
     * Snapshots de los productos pedidos (cada identificador distinto se consulta una sola vez,
     * en orden de aparicion); {@code PRODUCT_NOT_FOUND} ante el primero inexistente.
     */
    public Map<Long, ProductSnapshot> requireProducts(String enterpriseId, Collection<Long> productIds,
            DocumentKind kind) {
        Map<Long, ProductSnapshot> found = new LinkedHashMap<>();
        for (Long productId : distinct(productIds)) {
            ProductSnapshot snapshot = products.findProduct(enterpriseId, productId, kind)
                    .orElseThrow(() -> new BillingException(BillingErrorCode.BILLING_PRODUCT_NOT_FOUND,
                            "El producto " + productId + " no existe"));
            found.put(productId, snapshot);
        }
        return found;
    }

    /** Como {@link #requireProducts} pero omite los inexistentes (lectura de un borrador, DRF-8). */
    public Map<Long, ProductSnapshot> tryProducts(String enterpriseId, Collection<Long> productIds,
            DocumentKind kind) {
        Map<Long, ProductSnapshot> found = new LinkedHashMap<>();
        for (Long productId : distinct(productIds)) {
            Optional<ProductSnapshot> snapshot = products.findProduct(enterpriseId, productId, kind);
            snapshot.ifPresent(value -> found.put(productId, value));
        }
        return found;
    }

    /** Emisor de la empresa; {@code ENTERPRISE_NOT_FOUND} si no existe. */
    public IssuerSnapshot requireIssuer(String enterpriseId) {
        return enterprises.findIssuer(enterpriseId)
                .orElseThrow(() -> new BillingException(BillingErrorCode.BILLING_ENTERPRISE_NOT_FOUND,
                        "La empresa emisora no existe"));
    }

    private static Collection<Long> distinct(Collection<Long> productIds) {
        return productIds == null ? List.of() : new LinkedHashSet<>(productIds);
    }
}
