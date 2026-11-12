package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.backup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupPaging;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.backup.ProductsBackupFormat.CategoryRow;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.backup.ProductsBackupFormat.ProductRow;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.backup.ProductsBackupFormat.ProductTypeRow;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.backup.ProductsBackupFormat.UnitOfMeasureRow;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.CategoryEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductTypeEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.UnitOfMeasureEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.ICategoryRepository;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IProductRepository;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IProductTypeRepository;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IUnitOfMeasureRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code products} (order 50) de la copia completa: unidades de medida, tipos de
 * producto, categorías (con sus impuestos) y productos.
 *
 * Restore: escribe SOLO por repositorios JPA. No pasa por {@code ProductService}, así que no se
 * publica ningún evento a {@code product.exchange} (no se disparan los listeners de stock/kardex/PEPS,
 * que además correrían aunque el restore hiciera rollback) ni se audita. Las réplicas de inventario
 * las restauran los participantes {@code inventory.*}.
 *
 * Orden y namespaces del IdMap: {@code UOM}, {@code PRODUCT_TYPE}, {@code CATEGORY} (que remapea
 * {@code inventoryId/costId/saleId/returnId} con {@code ACCOUNT} y {@code taxes} con {@code TAX}) y
 * {@code PRODUCT} (que remapea unidad, tipo y categoría). Todas las PK se regeneran (IDENTITY);
 * {@code enterpriseId} es la empresa destino y {@code tenantId} lo pone {@code @TenantId}.
 * {@code usageCount} vuelve a 0 y {@code quantity}/{@code cost} se copian tal cual (snapshot
 * coherente con el stock del mismo ZIP).
 */
@Component
@RequiredArgsConstructor
public class ProductsBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;

    private static final int BATCH = BackupPaging.PAGE_SIZE;
    private static final Pageable SAMPLE = PageRequest.of(0, 20);

    private final IUnitOfMeasureRepository unitRepository;
    private final IProductTypeRepository typeRepository;
    private final ICategoryRepository categoryRepository;
    private final IProductRepository productRepository;
    private final EntityManager entityManager;

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_PRODUCTS;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_PRODUCTS;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_PUC, BackupKeys.MODULE_TAXES);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String ent = enterpriseId.toString();
        writer.writeNdjson(ProductsBackupFormat.UNITS_OF_MEASURE, BackupPaging.iterate(
                p -> unitRepository.findByEnterpriseId(ent, BackupPaging.page(p)),
                u -> new UnitOfMeasureRow(u.getId(), u.getName(), u.getDescription(), u.getAbbreviation(),
                        u.isState()),
                entityManager::clear));
        writer.writeNdjson(ProductsBackupFormat.PRODUCT_TYPES, BackupPaging.iterate(
                p -> typeRepository.findByEnterpriseId(ent, BackupPaging.page(p)),
                t -> new ProductTypeRow(t.getId(), t.getName(), t.getDescription(), t.isState()),
                entityManager::clear));
        writer.writeNdjson(ProductsBackupFormat.CATEGORIES, BackupPaging.iterate(
                p -> categoryRepository.findByEnterpriseId(ent, BackupPaging.page(p)),
                c -> new CategoryRow(c.getId(), c.getName(), c.getDescription(), c.getInventoryId(),
                        c.getCostId(), c.getSaleId(), c.getReturnId(),
                        c.getTaxes() == null ? List.of() : new ArrayList<>(c.getTaxes()), c.isState()),
                entityManager::clear));
        writer.writeNdjson(ProductsBackupFormat.PRODUCTS, BackupPaging.iterate(
                p -> productRepository.findByEnterpriseId(ent, BackupPaging.page(p)),
                p -> new ProductRow(p.getId(), p.getCode(), p.getName(), p.getDescription(), p.getQuantity(),
                        p.getUnitOfMeasureId(), p.getCategoryId(), p.getCost(), p.isState(), p.getReference(),
                        p.getProductTypeId(), p.getPresentation()),
                entityManager::clear));
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        String ent = ctx.requireTargetEnterpriseId().toString();
        restoreUnits(reader, idMap, ent);
        restoreTypes(reader, idMap, ent);
        restoreCategories(reader, idMap, ent);
        restoreProducts(reader, idMap, ent);
    }

    private void restoreUnits(BackupReader reader, IdMap idMap, String ent) {
        if (!reader.has(ProductsBackupFormat.UNITS_OF_MEASURE)) {
            return;
        }
        reader.forEachBatch(ProductsBackupFormat.UNITS_OF_MEASURE, UnitOfMeasureRow.class, BATCH, rows -> {
            List<UnitOfMeasureEntity> entities = new ArrayList<>(rows.size());
            for (UnitOfMeasureRow r : rows) {
                UnitOfMeasureEntity e = new UnitOfMeasureEntity();
                e.setName(r.name());
                e.setDescription(r.description());
                e.setAbbreviation(r.abbreviation());
                e.setEnterpriseId(ent);
                e.setState(r.state());
                entities.add(e);
            }
            List<UnitOfMeasureEntity> saved = unitRepository.saveAll(entities);
            for (int i = 0; i < rows.size(); i++) {
                idMap.put(BackupKeys.UOM, rows.get(i).id(), saved.get(i).getId());
            }
            flushAndClear();
        });
    }

    private void restoreTypes(BackupReader reader, IdMap idMap, String ent) {
        if (!reader.has(ProductsBackupFormat.PRODUCT_TYPES)) {
            return;
        }
        reader.forEachBatch(ProductsBackupFormat.PRODUCT_TYPES, ProductTypeRow.class, BATCH, rows -> {
            List<ProductTypeEntity> entities = new ArrayList<>(rows.size());
            for (ProductTypeRow r : rows) {
                ProductTypeEntity e = new ProductTypeEntity();
                e.setName(r.name());
                e.setDescription(r.description());
                e.setEnterpriseId(ent);
                e.setState(r.state());
                entities.add(e);
            }
            List<ProductTypeEntity> saved = typeRepository.saveAll(entities);
            for (int i = 0; i < rows.size(); i++) {
                idMap.put(BackupKeys.PRODUCT_TYPE, rows.get(i).id(), saved.get(i).getId());
            }
            flushAndClear();
        });
    }

    private void restoreCategories(BackupReader reader, IdMap idMap, String ent) {
        if (!reader.has(ProductsBackupFormat.CATEGORIES)) {
            return;
        }
        reader.forEachBatch(ProductsBackupFormat.CATEGORIES, CategoryRow.class, BATCH, rows -> {
            List<CategoryEntity> entities = new ArrayList<>(rows.size());
            for (CategoryRow r : rows) {
                String owner = "la categoría '" + r.name() + "' (id " + r.id() + ")";
                CategoryEntity e = new CategoryEntity();
                e.setName(r.name());
                e.setDescription(r.description());
                e.setEnterpriseId(ent);
                e.setInventoryId(remap(idMap, BackupKeys.ACCOUNT, r.inventoryId(), owner, "cuenta de inventario"));
                e.setCostId(remap(idMap, BackupKeys.ACCOUNT, r.costId(), owner, "cuenta de costo"));
                e.setSaleId(remap(idMap, BackupKeys.ACCOUNT, r.saleId(), owner, "cuenta de venta"));
                e.setReturnId(remap(idMap, BackupKeys.ACCOUNT, r.returnId(), owner, "cuenta de devolución"));
                List<Long> taxes = new ArrayList<>();
                if (r.taxes() != null) {
                    for (Long taxId : r.taxes()) {
                        taxes.add(remap(idMap, BackupKeys.TAX, taxId, owner, "impuesto"));
                    }
                }
                e.setTaxes(taxes);
                e.setState(r.state());
                entities.add(e);
            }
            List<CategoryEntity> saved = categoryRepository.saveAll(entities);
            for (int i = 0; i < rows.size(); i++) {
                idMap.put(BackupKeys.CATEGORY, rows.get(i).id(), saved.get(i).getId());
            }
            flushAndClear();
        });
    }

    private void restoreProducts(BackupReader reader, IdMap idMap, String ent) {
        if (!reader.has(ProductsBackupFormat.PRODUCTS)) {
            return;
        }
        reader.forEachBatch(ProductsBackupFormat.PRODUCTS, ProductRow.class, BATCH, rows -> {
            List<ProductEntity> entities = new ArrayList<>(rows.size());
            for (ProductRow r : rows) {
                String owner = "el producto '" + r.name() + "' (id " + r.id() + ")";
                ProductEntity e = new ProductEntity();
                e.setCode(r.code());
                e.setName(r.name());
                e.setDescription(r.description());
                e.setQuantity(r.quantity());
                e.setUnitOfMeasureId(remap(idMap, BackupKeys.UOM, r.unitOfMeasureId(), owner, "unidad de medida"));
                e.setCategoryId(remap(idMap, BackupKeys.CATEGORY, r.categoryId(), owner, "categoría"));
                e.setProductTypeId(remap(idMap, BackupKeys.PRODUCT_TYPE, r.productTypeId(), owner, "tipo de producto"));
                e.setEnterpriseId(ent);
                e.setCost(r.cost());
                e.setState(r.state());
                e.setReference(r.reference());
                e.setPresentation(r.presentation());
                e.setUsageCount(0);
                entities.add(e);
            }
            List<ProductEntity> saved = productRepository.saveAll(entities);
            for (int i = 0; i < rows.size(); i++) {
                idMap.put(BackupKeys.PRODUCT, rows.get(i).id(), saved.get(i).getId());
            }
            flushAndClear();
        });
    }

    /** FK opcional hacia otro módulo/entrada: {@code null} se conserva; un id que no se puede remapear es error. */
    private static Long remap(IdMap idMap, String namespace, Long oldId, String owner, String what) {
        if (oldId == null) {
            return null;
        }
        try {
            return idMap.requireLong(namespace, oldId);
        } catch (BackupIntegrityException e) {
            throw new BackupIntegrityException(BackupKeys.MODULE_PRODUCTS,
                    owner + " referencia una " + what + " que no está en la copia: " + e.getDetail(), e);
        }
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /* ------------------------------------------------------------------ */
    /* Purge (restore en el lugar)                                        */
    /* ------------------------------------------------------------------ */

    /**
     * Borra productos, categorías (con su tabla category_taxes), unidades y tipos de la empresa, en ese orden
     * (las categorías/unidades/tipos son referenciados por los productos).
     */
    @Override
    public void purge(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        productRepository.deleteAllByEnterpriseIdBulk(ent);
        categoryRepository.deleteTaxLinksByEnterpriseId(ent);
        categoryRepository.deleteAllByEnterpriseIdBulk(ent);
        unitRepository.deleteAllByEnterpriseIdBulk(ent);
        typeRepository.deleteAllByEnterpriseIdBulk(ent);
        flushAndClear();
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put(ProductsBackupFormat.UNITS_OF_MEASURE, unitRepository.countByEnterpriseId(ent));
        counts.put(ProductsBackupFormat.PRODUCT_TYPES, typeRepository.countByEnterpriseId(ent));
        counts.put(ProductsBackupFormat.CATEGORIES, categoryRepository.countByEnterpriseId(ent));
        counts.put(ProductsBackupFormat.PRODUCTS, productRepository.countByEnterpriseId(ent));
        return counts;
    }

    /**
     * Las FKs de producto no tienen restricción real en la BD: se revisa que categoría, unidad y tipo
     * existan en la empresa destino. Las cuentas e impuestos de las categorías ya quedaron garantizados
     * por {@code idMap.requireLong(ACCOUNT/TAX, ...)} al restaurar.
     */
    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        List<String> problems = new ArrayList<>();
        addProblem(problems, productRepository.findIdsWithMissingCategory(ent, SAMPLE),
                "productos referencian una categoría inexistente");
        addProblem(problems, productRepository.findIdsWithMissingUnitOfMeasure(ent, SAMPLE),
                "productos referencian una unidad de medida inexistente");
        addProblem(problems, productRepository.findIdsWithMissingProductType(ent, SAMPLE),
                "productos referencian un tipo de producto inexistente");
        return problems;
    }

    private static void addProblem(List<String> problems, List<Long> ids, String text) {
        if (!ids.isEmpty()) {
            problems.add(ids.size() + " " + text + " (ids " + ids + ")");
        }
    }
}
