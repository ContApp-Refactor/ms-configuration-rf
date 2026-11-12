package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.output.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mapstruct.factory.Mappers;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.CategoryEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces.ICategoryPersistenceMapper;

/**
 * Tests del mapper de persistencia. toCategory es un método default escrito
 * a mano (no generado por MapStruct), por lo que debe poblar todos los
 * campos manualmente. Regresión PP7 HU2: sin esto la lectura entity→domain
 * perdía productTypeId/returnPurchaseId y el GET devolvía null.
 */
@DisplayName("CategoryPersistenceMapper - mapeo entity a dominio")
class CategoryPersistenceMapperUnitTest {

    private final ICategoryPersistenceMapper persistenceMapper =
            Mappers.getMapper(ICategoryPersistenceMapper.class);

    private CategoryEntity buildEntity() {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(17L);
        entity.setName("Alimentos");
        entity.setDescription("Alimentos Pescado");
        entity.setEnterpriseId("1");
        entity.setInventoryId(14050101L);
        entity.setCostId(61350101L);
        entity.setSaleId(41350101L);
        entity.setReturnId(41750101L);
        entity.setProductTypeId(1L);
        entity.setReturnPurchaseId(41750101L);
        entity.setState(true);
        return entity;
    }

    @Test
    @DisplayName("toCategory conserva productTypeId y returnPurchaseId (PP7 HU2)")
    void testToCategory_PreservesProductTypeAndReturnPurchase() {
        Category category = persistenceMapper.toCategory(buildEntity());

        assertNotNull(category);
        assertEquals(1L, category.getProductTypeId(), "productTypeId debe sobrevivir entity→domain");
        assertEquals(41750101L, category.getReturnPurchaseId(), "returnPurchaseId debe sobrevivir entity→domain");
        assertEquals(41750101L, category.getReturnId(), "returnId (devolución en venta) no regresa");
    }

    @Test
    @DisplayName("toCategory conserva los campos clásicos sin regresión")
    void testToCategory_MapsClassicFields() {
        Category category = persistenceMapper.toCategory(buildEntity());

        assertNotNull(category);
        assertEquals(17L, category.getId());
        assertEquals("Alimentos", category.getName());
        assertEquals("Alimentos Pescado", category.getDescription());
        assertEquals("1", category.getEnterpriseId());
        assertEquals(14050101L, category.getInventoryId());
        assertEquals(61350101L, category.getCostId());
        assertEquals(41350101L, category.getSaleId());
        assertEquals(true, category.isState());
    }
}
