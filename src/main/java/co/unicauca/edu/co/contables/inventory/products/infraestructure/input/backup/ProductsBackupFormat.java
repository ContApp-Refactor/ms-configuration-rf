package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.backup;

import java.util.List;

/**
 * @brief Formato (paths y registros NDJSON) del módulo {@code products} de la copia completa.
 *
 * Los ids son los de la BD de ORIGEN: el restore nunca los inserta, solo los usa como llave del
 * {@code IdMap} (UOM, PRODUCT_TYPE, CATEGORY, PRODUCT). Las FKs a otros módulos
 * ({@code Category.inventoryId/costId/saleId/returnId} → ACCOUNT y {@code taxes} → TAX) también
 * viajan con el id de origen y se remapean al restaurar. {@code usageCount} no viaja: se
 * restaura en 0 porque cuenta usos por documentos que la copia no incluye.
 */
public final class ProductsBackupFormat {

    public static final String UNITS_OF_MEASURE = "products/units_of_measure.ndjson";
    public static final String PRODUCT_TYPES = "products/product_types.ndjson";
    public static final String CATEGORIES = "products/categories.ndjson";
    public static final String PRODUCTS = "products/products.ndjson";

    private ProductsBackupFormat() {
    }

    public record UnitOfMeasureRow(Long id, String name, String description, String abbreviation, boolean state) {
    }

    public record ProductTypeRow(Long id, String name, String description, boolean state) {
    }

    public record CategoryRow(Long id, String name, String description, Long inventoryId, Long costId, Long saleId,
            Long returnId, List<Long> taxes, boolean state) {
    }

    public record ProductRow(Long id, String code, String name, String description, Integer quantity,
            Long unitOfMeasureId, Long categoryId, double cost, boolean state, String reference, Long productTypeId,
            String presentation) {
    }
}
