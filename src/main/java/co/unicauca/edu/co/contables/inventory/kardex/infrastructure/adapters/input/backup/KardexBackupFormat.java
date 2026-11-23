package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup;

/**
 * @brief Formato (path y registro NDJSON) de la réplica de producto del kardex de promedio
 * ponderado ({@code kardex_products}) de la copia completa.
 *
 * {@code productId} es el id de producto de la BD de ORIGEN; se remapea con {@code PRODUCT}.
 */
public final class KardexBackupFormat {

    public static final String PRODUCTS = "inventory/kardex_products.ndjson";

    private KardexBackupFormat() {
    }

    public record KardexProductRow(Long id, Long productId, String reference, String name, String presentation,
            boolean state) {
    }
}
