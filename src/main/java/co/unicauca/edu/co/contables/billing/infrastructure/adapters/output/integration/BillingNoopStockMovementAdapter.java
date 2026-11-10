package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.billing.application.ports.output.StockMovementPort;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;

/**
 * Adaptador vacio de stock/kardex. YA NO es un bean: lo reemplaza
 * {@link BillingInventoryStockMovementAdapter} (dos beans de {@link StockMovementPort} romperian
 * el arranque). Se conserva como implementacion nula para pruebas o para desactivar el movimiento
 * de inventario si se vuelve a registrar manualmente.
 */
public class BillingNoopStockMovementAdapter implements StockMovementPort {

    @Override
    public void onEmitted(BillingDocument document) {
        // sin movimiento de inventario por ahora
    }

    @Override
    public void onAnnulled(BillingDocument document) {
        // sin reversa de inventario por ahora
    }
}
