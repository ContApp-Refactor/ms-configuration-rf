package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.input.rest.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.CategoryResponse;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces.ICategoryRestMapper;
import org.mapstruct.factory.Mappers;

/**
 * Tests del mapper de categoría. toCategoryResponse es un método default
 * escrito a mano (no generado por MapStruct), por lo que debe poblar todos
 * los campos manualmente.
 */
@DisplayName("CategoryRestMapper - mapeo de respuesta")
class CategoryRestMapperUnitTest {

    private final ICategoryRestMapper categoryRestMapper = Mappers.getMapper(ICategoryRestMapper.class);

    private Category buildCategory() {
        return Category.builder()
                .id(17L)
                .name("Alimentos")
                .description("Categoría alimentos")
                .enterpriseId("1")
                .inventoryId(10L)
                .costId(11L)
                .saleId(12L)
                .returnId(136L)
                .productTypeId(1L)
                .returnPurchaseId(137L)
                .taxes(List.of(1L))
                .state(true)
                .build();
    }

    @Test
    @DisplayName("toCategoryResponse incluye productTypeId y returnPurchaseId (PP7 HU2)")
    void testToCategoryResponse_IncludesProductTypeAndReturnPurchase() {
        CategoryResponse response = categoryRestMapper.toCategoryResponse(buildCategory());

        assertNotNull(response);
        assertEquals(1L, response.getProductTypeId(), "productTypeId debe viajar en la respuesta");
        assertEquals(137L, response.getReturnPurchaseId(), "returnPurchaseId debe viajar en la respuesta");
        assertEquals(136L, response.getReturnId(), "returnId (devolución en venta) debe seguir viajando");
    }

    @Test
    @DisplayName("toCategoryResponseList propaga los campos nuevos a cada elemento")
    void testToCategoryResponseList_IncludesNewFields() {
        List<CategoryResponse> responses = categoryRestMapper.toCategoryResponseList(List.of(buildCategory()));

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getProductTypeId());
        assertEquals(137L, responses.get(0).getReturnPurchaseId());
    }

    @Test
    @DisplayName("toCategoryResponse mapea los campos clásicos sin regresión")
    void testToCategoryResponse_MapsClassicFields() {
        CategoryResponse response = categoryRestMapper.toCategoryResponse(buildCategory());

        assertNotNull(response);
        assertEquals(17L, response.getId());
        assertEquals("Alimentos", response.getName());
        assertEquals(10L, response.getInventoryId());
        assertEquals(11L, response.getCostId());
        assertEquals(12L, response.getSaleId());
        assertEquals(136L, response.getReturnId());
        assertEquals(List.of(1L), response.getTaxes());
        assertEquals(true, response.isState());
    }
}
