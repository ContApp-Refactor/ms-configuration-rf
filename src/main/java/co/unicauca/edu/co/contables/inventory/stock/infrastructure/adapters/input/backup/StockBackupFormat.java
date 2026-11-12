package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.backup;

import java.math.BigDecimal;

/**
 * @brief Formato (path y registro NDJSON) de la réplica de stock de la copia completa.
 *
 * {@code productId} es el id de producto de la BD de ORIGEN; se remapea con el namespace
 * {@code PRODUCT} del IdMap al restaurar.
 */
public final class StockBackupFormat {

    public static final String STOCK = "inventory/stock.ndjson";

    private StockBackupFormat() {
    }

    public record StockRow(Long id, Long productId, String name, int quantity, BigDecimal price, boolean state) {
    }
}
