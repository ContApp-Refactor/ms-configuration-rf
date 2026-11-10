package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.billing.application.ports.output.StockMovementPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration.InventoryStockErrorTranslator.Operation;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Mueve el stock/kardex de Inventario al EMITIR un documento, dentro de la misma transaccion de
 * Billing: si Inventario falla (o rechaza), la excepcion sube como {@link BillingException} y se
 * revierte toda la emision.
 *
 * <p>Mapeo documento -> puerto de entrada de kardex (el kardex ya actualiza el stock por dentro, por
 * eso NO se llama tambien al puerto de stock):
 * <pre>
 *   metodo            FV venta        NC dev. venta        FC compra          DC dev. compra
 *   WEIGHTED_AVERAGE  registerSale    registerReturnOnSale registerPurchase   registerReturnOnPurchase
 *   PEPS              registerSale    registerSaleReturn   registerPurchase   registerPurchaseReturn
 * </pre>
 * El metodo lo define la empresa ({@code inventoryConfigurationType}); si es nulo se rechaza.
 *
 * <p>Reglas: las lineas se agrupan por producto (el kardex rechaza el mismo producto repetido bajo
 * un mismo codigo de documento): cantidad sumada y precio promedio ponderado a 2 decimales. Las
 * cantidades deben ser enteras (Inventario usa int). El codigo de documento ({@code factCode}) es el
 * numero del propio documento en facturas y el de la FACTURA ORIGINAL en devoluciones. La fecha se
 * deja nula para que Inventario asigne la actual y valide el calendario contable.
 *
 * <p>Solo se importan puertos de entrada y modelos de dominio de Inventario; sus excepciones (en
 * paquetes de infraestructura prohibidos por ArchUnit) se traducen por nombre/estado en
 * {@link InventoryStockErrorTranslator}.
 *
 * <p>Anulacion: Inventario no tiene operacion inversa, por lo que {@link #onAnnulled} FALLA con
 * {@code BILLING_STOCK_REJECTED} en vez de dejar el inventario descuadrado en silencio.
 */
@Component("billingInventoryStockMovementAdapter")
public class BillingInventoryStockMovementAdapter implements StockMovementPort {

    private final IEnterpriseService enterpriseService;
    private final co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort
            weightedAverageKardex;
    private final co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort
            pepsKardex;

    public BillingInventoryStockMovementAdapter(
            IEnterpriseService enterpriseService,
            co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort
                    weightedAverageKardex,
            co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort pepsKardex) {
        this.enterpriseService = enterpriseService;
        this.weightedAverageKardex = weightedAverageKardex;
        this.pepsKardex = pepsKardex;
    }

    @Override
    public void onEmitted(BillingDocument document) {
        Map<Long, Movement> movements = groupByProduct(document);
        InventoryConfigurationTypeEnum method = resolveMethod(document);
        String factCode = factCode(document);
        String details = details(document);
        DocumentType type = document.type();

        for (Movement movement : movements.values()) {
            try {
                if (method == InventoryConfigurationTypeEnum.PEPS) {
                    registerPeps(type, factCode, details, movement);
                } else {
                    registerWeightedAverage(type, factCode, details, movement);
                }
            } catch (RuntimeException ex) {
                throw InventoryStockErrorTranslator.translate(ex, operationOf(type), movement.label, factCode);
            }
        }
    }

    @Override
    public void onAnnulled(BillingDocument document) {
        String number = document.number() == null ? "" : " " + document.number().display();
        throw new BillingException(BillingErrorCode.BILLING_STOCK_REJECTED,
                "No se puede anular el documento" + number + ": Inventario no tiene una operacion inversa para"
                        + " revertir su movimiento de existencias"
                        + (document.type().isInvoice() ? ". Para revertir la mercancia registre una devolucion" : ""));
    }

    // ---------- movimientos por metodo de valoracion ----------

    private void registerWeightedAverage(DocumentType type, String factCode, String details, Movement m) {
        co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex kardex =
                new co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex();
        kardex.setFactCode(factCode);
        kardex.setProductId(m.productId);
        kardex.setQuantity(m.quantity);
        kardex.setUnitPrice(m.unitPrice);
        kardex.setDetails(details);
        switch (type) {
            case SALE_INVOICE -> {
                kardex.setType(MovementType.SALE);
                weightedAverageKardex.registerSale(kardex);
            }
            case SALE_RETURN -> {
                kardex.setType(MovementType.SALESRETURN);
                weightedAverageKardex.registerReturnOnSale(kardex);
            }
            case PURCHASE_INVOICE -> {
                kardex.setType(MovementType.PURCHASE);
                weightedAverageKardex.registerPurchase(kardex);
            }
            case PURCHASE_RETURN -> {
                kardex.setType(MovementType.PURCHASERETURN);
                weightedAverageKardex.registerReturnOnPurchase(kardex);
            }
        }
    }

    private void registerPeps(DocumentType type, String factCode, String details, Movement m) {
        co.unicauca.edu.co.contables.inventory.peps.domain.model.Product product =
                new co.unicauca.edu.co.contables.inventory.peps.domain.model.Product();
        product.setProductId(m.productId);
        co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex kardex =
                new co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex();
        kardex.setFactCode(factCode);
        kardex.setProduct(product);
        kardex.setQuantity(m.quantity);
        kardex.setUnitPrice(m.unitPrice);
        kardex.setDetails(details);
        switch (type) {
            case SALE_INVOICE -> pepsKardex.registerSale(kardex);
            case SALE_RETURN -> pepsKardex.registerSaleReturn(kardex);
            case PURCHASE_INVOICE -> pepsKardex.registerPurchase(kardex);
            case PURCHASE_RETURN -> pepsKardex.registerPurchaseReturn(kardex);
        }
    }

    // ---------- armado de datos ----------

    private InventoryConfigurationTypeEnum resolveMethod(BillingDocument document) {
        UUID enterpriseId;
        try {
            enterpriseId = UUID.fromString(document.enterpriseId().trim());
        } catch (IllegalArgumentException ex) {
            throw new BillingException(BillingErrorCode.BILLING_ENTERPRISE_NOT_FOUND, "La empresa emisora no existe");
        }
        Enterprise enterprise = enterpriseService.findById(enterpriseId);
        if (enterprise == null) {
            throw new BillingException(BillingErrorCode.BILLING_ENTERPRISE_NOT_FOUND, "La empresa emisora no existe");
        }
        InventoryConfigurationTypeEnum method = enterprise.getInventoryConfigurationType();
        if (method == null) {
            throw new BillingException(BillingErrorCode.BILLING_STOCK_CONFIG_MISSING,
                    "La empresa no tiene configurado el metodo de valoracion de inventario");
        }
        return method;
    }

    /**
     * Agrupa por producto (orden de primera aparicion) validando que las cantidades sean enteras.
     * Todas las lineas se mueven: en Billing toda linea referencia un producto del catalogo de
     * Inventario ({@code productId} obligatorio) y el catalogo no distingue servicios ni productos no
     * inventariables, asi que no hay lineas que omitir.
     */
    private Map<Long, Movement> groupByProduct(BillingDocument document) {
        Map<Long, BigDecimal> quantities = new LinkedHashMap<>();
        Map<Long, BigDecimal> subtotals = new LinkedHashMap<>();
        Map<Long, String> labels = new LinkedHashMap<>();
        for (DocumentLine line : document.lines()) {
            BigDecimal quantity = line.quantity().value();
            BigDecimal normalized = quantity.stripTrailingZeros();
            if (normalized.scale() > 0 || normalized.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
                throw new BillingException(BillingErrorCode.BILLING_STOCK_QUANTITY_NOT_SUPPORTED,
                        "Inventario solo admite cantidades enteras; el item " + line.lineNo() + " ("
                                + labelOf(line) + ") tiene cantidad " + normalized.toPlainString());
            }
            quantities.merge(line.productId(), quantity, BigDecimal::add);
            subtotals.merge(line.productId(), line.subtotal().value(), BigDecimal::add);
            labels.putIfAbsent(line.productId(), labelOf(line));
        }
        Map<Long, Movement> movements = new LinkedHashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : quantities.entrySet()) {
            BigDecimal total = entry.getValue();
            String label = labels.get(entry.getKey());
            if (total.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
                throw new BillingException(BillingErrorCode.BILLING_STOCK_QUANTITY_NOT_SUPPORTED,
                        "La cantidad total del " + label + " excede lo que Inventario admite");
            }
            BigDecimal unitPrice = subtotals.get(entry.getKey()).divide(total, 2, RoundingMode.HALF_UP);
            movements.put(entry.getKey(), new Movement(entry.getKey(), total.intValueExact(), unitPrice, label));
        }
        return movements;
    }

    /** "producto 7 (P-7 Tornillo)": el id primero para soporte, codigo y nombre para el usuario. */
    private static String labelOf(DocumentLine line) {
        StringBuilder label = new StringBuilder("producto ").append(line.productId());
        String code = line.productCode() == null ? "" : line.productCode().trim();
        String name = line.productName() == null ? "" : line.productName().trim();
        String extra = (code + " " + name).trim();
        if (!extra.isEmpty()) {
            label.append(" (").append(extra).append(')');
        }
        return label.toString();
    }

    /** Factura: su propio numero. Devolucion: el numero de la factura original. */
    private static String factCode(BillingDocument document) {
        if (document.type().isReturn()) {
            return document.original().number().display();
        }
        return document.number().display();
    }

    private static String details(BillingDocument document) {
        return document.type().isReturn()
                ? "Devolucion " + document.number().display()
                : "Facturacion " + document.number().display();
    }

    private static Operation operationOf(DocumentType type) {
        return switch (type) {
            case SALE_INVOICE -> Operation.SALE;
            case PURCHASE_INVOICE -> Operation.PURCHASE;
            case SALE_RETURN -> Operation.SALE_RETURN;
            case PURCHASE_RETURN -> Operation.PURCHASE_RETURN;
        };
    }

    private record Movement(long productId, int quantity, BigDecimal unitPrice, String label) {
    }
}
