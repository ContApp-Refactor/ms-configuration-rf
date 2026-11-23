package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup;

/**
 * @brief Formato (path y registro NDJSON) de la réplica de producto del kardex PEPS
 * ({@code peps_product}) de la copia completa.
 *
 * {@code productId} es el id de producto de la BD de ORIGEN; se remapea con {@code PRODUCT}.
 */
public final class PepsBackupFormat {

    public static final String PRODUCTS = "inventory/peps_products.ndjson";

    private PepsBackupFormat() {
    }

    public record PepsProductRow(Long id, Long productId, String reference, String name, String presentation,
            boolean state) {
    }
}
