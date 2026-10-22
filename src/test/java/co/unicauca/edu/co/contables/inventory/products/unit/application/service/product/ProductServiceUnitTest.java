package co.unicauca.edu.co.contables.inventory.products.unit.application.service.product;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductEventPort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.ICategoryPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.product.ProductService;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductInUseException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNameAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductReferenceAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceUnitTest {

    @Mock
    private IProductPersistencePort productPersistencePort;

    @Mock
    private IProductEventPort productEventPort;

    @Mock
    private IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort;

    @Mock
    private ICategoryPersistencePort categoryPersistencePort;

    @Mock
    private IProductTypePersistencePort productTypePersistencePort;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private String enterpriseId;
    private Long productId;
    private UnitOfMeasure unitOfMeasure;
    private Category category;
    private ProductType productType;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT123";
        productId = 1L;

        unitOfMeasure = UnitOfMeasure.builder()
                .id(1L)
                .name("Unidad")
                .abbreviation("UND")
                .state(true)
                .enterpriseId(enterpriseId)
                .build();

        category = Category.builder()
                .id(1L)
                .name("Categoría Test")
                .state(true)
                .enterpriseId(enterpriseId)
                .build();

        productType = ProductType.builder()
                .id(1L)
                .name("Tipo Test")
                .state(true)
                .enterpriseId(enterpriseId)
                .build();

        product = Product.builder()
                .id(productId)
                .name("Producto Test")
                .reference("REF001")
                .description("Descripción test")
                .quantity(100)
                .cost(150.50)
                .presentation("Caja x12")
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .state(true)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("Debe encontrar producto por ID y empresa")
    void testFindById() {
        // Arrange
        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));

        // Act
        Product result = productService.findById(productId, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals(enterpriseId, result.getEnterpriseId());
        verify(productPersistencePort, times(1)).findByIdAndEnterpriseId(productId, enterpriseId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando producto no existe")
    void testFindByIdNotFound() {
        // Arrange
        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () ->
                productService.findById(productId, enterpriseId)
        );
        verify(productPersistencePort, times(1)).findByIdAndEnterpriseId(productId, enterpriseId);
    }

    @Test
    @DisplayName("Debe obtener productos con filtros")
    void testFindAllWithFilters() {
        // Arrange
        String search = "test";
        Page<Product> expectedPage = new PageImpl<>(List.of(product));
        when(productPersistencePort.findByEnterpriseIdWithFilters(
                enterpriseId, search, 0, 10, "name", "asc"))
                .thenReturn(expectedPage);

        // Act
        Page<Product> result = productService.findAllWithFilters(
                enterpriseId, search, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productPersistencePort, times(1))
                .findByEnterpriseIdWithFilters(enterpriseId, search, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("Debe obtener productos paginados sin búsqueda")
    void testFindAllPaginatedWithoutSearch() {
        // Arrange
        Page<Product> expectedPage = new PageImpl<>(List.of(product));
        when(productPersistencePort.countByEnterpriseId(enterpriseId)).thenReturn(1L);
        when(productPersistencePort.findByEnterpriseIdWithFilters(
                eq(enterpriseId), isNull(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(expectedPage);

        // Act
        Page<Product> result = productService.findAllPaginated(
                enterpriseId, Optional.empty(), Optional.empty(), "name", "asc", Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productPersistencePort, times(1)).countByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Debe obtener productos paginados con búsqueda")
    void testFindAllPaginatedWithSearch() {
        // Arrange
        String search = "test";
        Page<Product> expectedPage = new PageImpl<>(List.of(product));
        when(productPersistencePort.countByEnterpriseIdWithFilters(enterpriseId, search)).thenReturn(1L);
        when(productPersistencePort.findByEnterpriseIdWithFilters(
                eq(enterpriseId), eq(search), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(expectedPage);

        // Act
        Page<Product> result = productService.findAllPaginated(
                enterpriseId, Optional.empty(), Optional.empty(), "name", "asc", Optional.of(search));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productPersistencePort, times(1)).countByEnterpriseIdWithFilters(enterpriseId, search);
    }

    @Test
    @DisplayName("Debe contar productos por empresa")
    void testCountByEnterpriseId() {
        // Arrange
        when(productPersistencePort.countByEnterpriseId(enterpriseId)).thenReturn(5L);

        // Act
        long result = productService.countByEnterpriseId(enterpriseId);

        // Assert
        assertEquals(5L, result);
        verify(productPersistencePort, times(1)).countByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Debe contar productos activos por empresa")
    void testCountActivatedByEnterpriseId() {
        // Arrange
        when(productPersistencePort.countActivatedByEnterpriseId(enterpriseId)).thenReturn(3L);

        // Act
        long result = productService.countActivatedByEnterpriseId(enterpriseId);

        // Assert
        assertEquals(3L, result);
        verify(productPersistencePort, times(1)).countActivatedByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Debe obtener productos activos paginados")
    void testFindActivatedPaginated() {
        // Arrange
        Page<Product> expectedPage = new PageImpl<>(List.of(product));
        when(productPersistencePort.countActivatedByEnterpriseId(enterpriseId)).thenReturn(1L);
        when(productPersistencePort.findActivatedWithPagination(eq(enterpriseId), anyInt(), anyInt()))
                .thenReturn(expectedPage);

        // Act
        Page<Product> result = productService.findActivatedPaginated(
                enterpriseId, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productPersistencePort, times(1)).countActivatedByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Debe crear producto exitosamente")
    void testCreate() {
        // Arrange
        Product newProduct = Product.builder()
                .name("nuevo producto")
                .reference("ref002")
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .quantity(50)
                .cost(100.0)
                .state(true)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(productPersistencePort.existsByReferenceAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        doNothing().when(productEventPort).publishCreatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.create(newProduct);

        // Assert
        assertNotNull(result);
        assertEquals("NUEVO PRODUCTO", result.getName());
        assertEquals("REF002", result.getReference());
        verify(productPersistencePort, times(2)).create(any(Product.class));
        verify(productEventPort, times(1)).publishCreatedStockEvent(any(ProductSyncDto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando nombre de producto ya existe")
    void testCreateWithDuplicateName() {
        // Arrange
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ProductNameAlreadyExistsException.class, () ->
                productService.create(product)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando referencia de producto ya existe")
    void testCreateWithDuplicateReference() {
        // Arrange
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(productPersistencePort.existsByReferenceAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ProductReferenceAlreadyExistsException.class, () ->
                productService.create(product)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando unidad de medida no existe")
    void testCreateWithNonExistentUnitOfMeasure() {
        // Arrange
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnitOfMeasureNotFoundException.class, () ->
                productService.create(product)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando unidad de medida está inactiva")
    void testCreateWithInactiveUnitOfMeasure() {
        // Arrange
        unitOfMeasure.setState(false);
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));

        // Act & Assert
        assertThrows(UnitOfMeasureNotFoundException.class, () ->
                productService.create(product)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando categoría no existe")
    void testCreateWithNonExistentCategory() {
        // Arrange
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, () ->
                productService.create(product)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando categoría está inactiva")
    void testCreateWithInactiveCategory() {
        // Arrange
        category.setState(false);
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, () ->
                productService.create(product)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando tipo de producto no existe")
    void testCreateWithNonExistentProductType() {
        // Arrange
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductTypeNotFoundException.class, () ->
                productService.create(product)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando tipo de producto está inactivo")
    void testCreateWithInactiveProductType() {
        // Arrange
        productType.setState(false);
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));

        // Act & Assert
        assertThrows(ProductTypeNotFoundException.class, () ->
                productService.create(product)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar producto exitosamente")
    void testUpdate() {
        // Arrange
        Product updateData = Product.builder()
                .name("producto actualizado")
                .reference("REF002")
                .description("Nueva descripción")
                .quantity(200)
                .cost(250.75)
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .build();

        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.existsByReferenceAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(productEventPort).publishUpdatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.update(productId, updateData, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals("PRODUCTO ACTUALIZADO", result.getName());
        assertEquals("REF002", result.getReference());
        assertEquals(200, result.getQuantity());
        verify(productPersistencePort, times(1)).create(any(Product.class));
        verify(productEventPort, times(1)).publishUpdatedStockEvent(any(ProductSyncDto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar producto en uso")
    void testUpdateProductInUse() {
        // Arrange
        product.setUsageCount(5);
        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(ProductInUseException.class, () ->
                productService.update(productId, product, enterpriseId)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar producto inexistente")
    void testUpdateNonExistentProduct() {
        // Arrange
        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () ->
                productService.update(productId, product, enterpriseId)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe cambiar estado de producto")
    void testChangeState() {
        // Arrange
        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        productService.changeState(productId, enterpriseId);

        // Assert
        verify(productPersistencePort, times(1)).findByIdAndEnterpriseId(productId, enterpriseId);
        verify(productPersistencePort, times(1)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar estado de producto inexistente")
    void testChangeStateNonExistentProduct() {
        // Arrange
        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () ->
                productService.changeState(productId, enterpriseId)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe eliminar producto exitosamente")
    void testDeleteById() {
        // Arrange
        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));
        doNothing().when(productPersistencePort).deleteById(productId);
        doNothing().when(productEventPort).publishDeletedStockEvent(any(ProductSyncDto.class));

        // Act
        productService.deleteById(productId, enterpriseId);

        // Assert
        verify(productPersistencePort, times(1)).deleteById(productId);
        verify(productEventPort, times(1)).publishDeletedStockEvent(any(ProductSyncDto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar producto en uso")
    void testDeleteProductInUse() {
        // Arrange
        product.setUsageCount(5);
        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(ProductInUseException.class, () ->
                productService.deleteById(productId, enterpriseId)
        );
        verify(productPersistencePort, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar producto inexistente")
    void testDeleteNonExistentProduct() {
        // Arrange
        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () ->
                productService.deleteById(productId, enterpriseId)
        );
        verify(productPersistencePort, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe encontrar productos por categoría")
    void testFindAllByCategory() {
        // Arrange
        Long categoryId = 1L;
        when(productPersistencePort.findByCategoryId(categoryId))
                .thenReturn(List.of(product));

        // Act
        List<Product> result = productService.findAllByCategory(categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productPersistencePort, times(1)).findByCategoryId(categoryId);
    }

    @Test
    @DisplayName("Debe encontrar productos por unidad de medida")
    void testFindAllByUnitOfMeasure() {
        // Arrange
        Long unitId = 1L;
        when(productPersistencePort.findByUnitOfMeasureId(unitId))
                .thenReturn(List.of(product));

        // Act
        List<Product> result = productService.findAllByUnitOfMeasure(unitId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productPersistencePort, times(1)).findByUnitOfMeasureId(unitId);
    }

    @Test
    @DisplayName("Debe encontrar productos por tipo de producto")
    void testFindAllByProductType() {
        // Arrange
        Long typeId = 1L;
        when(productPersistencePort.findByProductTypeId(typeId))
                .thenReturn(List.of(product));

        // Act
        List<Product> result = productService.findAllByProductType(typeId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productPersistencePort, times(1)).findByProductTypeId(typeId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay productos por categoría")
    void testFindAllByCategoryEmpty() {
        // Arrange
        Long categoryId = 1L;
        when(productPersistencePort.findByCategoryId(categoryId))
                .thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productService.findAllByCategory(categoryId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productPersistencePort, times(1)).findByCategoryId(categoryId);
    }

    @Test
    @DisplayName("Debe crear producto con referencia vacía")
    void testCreateWithEmptyReference() {
        // Arrange
        Product newProduct = Product.builder()
                .name("producto sin referencia")
                .reference("   ")
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .quantity(50)
                .cost(100.0)
                .state(true)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        doNothing().when(productEventPort).publishCreatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.create(newProduct);

        // Assert
        assertNotNull(result);
        verify(productPersistencePort, never()).existsByReferenceAndEnterpriseId(anyString(), anyString());
        verify(productPersistencePort, times(2)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar con nombre diferente y regenerar código")
    void testUpdateWithDifferentNameRegenerateCode() {
        // Arrange
        product.setName("PRODUCTO TEST");
        Product updateData = Product.builder()
                .name("producto modificado")
                .reference("REF001")
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .quantity(100)
                .cost(150.0)
                .build();

        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.existsByReferenceAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(productEventPort).publishUpdatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.update(productId, updateData, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals("PRODUCTO MODIFICADO", result.getName());
        assertNotEquals("PRODUCTO TEST", result.getName());
        verify(productPersistencePort, times(1)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con nombre duplicado en otro producto")
    void testUpdateWithDuplicateNameInAnotherProduct() {
        // Arrange
        Product updateData = Product.builder()
                .name("Producto Existente")
                .reference("REF001")
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .build();

        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ProductNameAlreadyExistsException.class, () ->
                productService.update(productId, updateData, enterpriseId)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con referencia duplicada en otro producto")
    void testUpdateWithDuplicateReferenceInAnotherProduct() {
        // Arrange
        Product updateData = Product.builder()
                .name("Producto Test")
                .reference("REF_EXISTENTE")
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .build();

        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.existsByReferenceAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ProductReferenceAlreadyExistsException.class, () ->
                productService.update(productId, updateData, enterpriseId)
        );
        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar con categoría diferente y regenerar código")
    void testUpdateWithDifferentCategoryRegenerateCode() {
        // Arrange
        product.setCategoryId(1L);
        Product updateData = Product.builder()
                .name("Producto Test")
                .reference("REF001")
                .unitOfMeasureId(1L)
                .categoryId(2L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .quantity(100)
                .cost(150.0)
                .build();

        Category newCategory = Category.builder()
                .id(2L)
                .name("Nueva Categoría")
                .state(true)
                .enterpriseId(enterpriseId)
                .build();

        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(2L, enterpriseId))
                .thenReturn(Optional.of(newCategory));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.existsByReferenceAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(productEventPort).publishUpdatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.update(productId, updateData, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getCategoryId());
        verify(productPersistencePort, times(1)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar sin regenerar código cuando no cambian campos críticos")
    void testUpdateWithoutRegenerateCode() {
        // Arrange
        product.setName("PRODUCTO TEST");
        product.setCategoryId(1L);
        Product updateData = Product.builder()
                .name("producto test")
                .reference("REF002")
                .description("Nueva descripción")
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .quantity(200)
                .cost(250.0)
                .build();

        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.existsByReferenceAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(productEventPort).publishUpdatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.update(productId, updateData, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals("PRODUCTO TEST", result.getName());
        assertEquals(200, result.getQuantity());
        assertEquals(250.0, result.getCost());
        verify(productPersistencePort, times(1)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe crear producto sin referencia null")
    void testCreateWithNullReference() {
        // Arrange
        Product newProduct = Product.builder()
                .name("producto sin referencia")
                .reference(null)
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .quantity(50)
                .cost(100.0)
                .state(true)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        doNothing().when(productEventPort).publishCreatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.create(newProduct);

        // Assert
        assertNotNull(result);
        verify(productPersistencePort, never()).existsByReferenceAndEnterpriseId(anyString(), anyString());
        verify(productPersistencePort, times(2)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar con referencia vacía sin validar duplicados")
    void testUpdateWithEmptyReference() {
        // Arrange
        Product updateData = Product.builder()
                .name("producto test")
                .reference("  ")
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .quantity(100)
                .cost(150.0)
                .build();

        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(productEventPort).publishUpdatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.update(productId, updateData, enterpriseId);

        // Assert
        assertNotNull(result);
        verify(productPersistencePort, never()).existsByReferenceAndEnterpriseIdAndIdNot(anyString(), anyString(), anyLong());
        verify(productPersistencePort, times(1)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar con referencia null sin validar duplicados")
    void testUpdateWithNullReference() {
        // Arrange
        Product updateData = Product.builder()
                .name("producto test")
                .reference(null)
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .quantity(100)
                .cost(150.0)
                .build();

        when(productPersistencePort.findByIdAndEnterpriseId(productId, enterpriseId))
                .thenReturn(Optional.of(product));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(productEventPort).publishUpdatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.update(productId, updateData, enterpriseId);

        // Assert
        assertNotNull(result);
        verify(productPersistencePort, never()).existsByReferenceAndEnterpriseIdAndIdNot(anyString(), anyString(), anyLong());
        verify(productPersistencePort, times(1)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe buscar con búsqueda vacía tratándola como sin búsqueda")
    void testFindAllPaginatedWithEmptySearch() {
        // Arrange
        Page<Product> expectedPage = new PageImpl<>(List.of(product));
        when(productPersistencePort.countByEnterpriseId(enterpriseId)).thenReturn(1L);
        when(productPersistencePort.findByEnterpriseIdWithFilters(
                eq(enterpriseId), isNull(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(expectedPage);

        // Act
        Page<Product> result = productService.findAllPaginated(
                enterpriseId, Optional.empty(), Optional.empty(), "name", "asc", Optional.of("   "));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productPersistencePort, times(1)).countByEnterpriseId(enterpriseId);
        verify(productPersistencePort, never()).countByEnterpriseIdWithFilters(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe crear producto con unitOfMeasureId null sin validar")
    void testCreateWithNullUnitOfMeasureId() {
        // Arrange
        Product newProduct = Product.builder()
                .name("producto sin unidad")
                .unitOfMeasureId(null)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .quantity(50)
                .cost(100.0)
                .state(true)
                .build();

        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        doNothing().when(productEventPort).publishCreatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.create(newProduct);

        // Assert
        assertNotNull(result);
        verify(unitOfMeasurePersistencePort, never()).findByIdAndEnterpriseId(anyLong(), anyString());
        verify(productPersistencePort, times(2)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe crear producto con categoryId null sin validar")
    void testCreateWithNullCategoryId() {
        // Arrange
        Product newProduct = Product.builder()
                .name("producto sin categoría")
                .unitOfMeasureId(1L)
                .categoryId(null)
                .productTypeId(1L)
                .enterpriseId(enterpriseId)
                .quantity(50)
                .cost(100.0)
                .state(true)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(productTypePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productPersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        doNothing().when(productEventPort).publishCreatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.create(newProduct);

        // Assert
        assertNotNull(result);
        verify(categoryPersistencePort, never()).findByIdAndEnterpriseId(anyLong(), anyString());
        verify(productPersistencePort, times(2)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe crear producto con productTypeId null sin validar")
    void testCreateWithNullProductTypeId() {
        // Arrange
        Product newProduct = Product.builder()
                .name("producto sin tipo")
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(null)
                .enterpriseId(enterpriseId)
                .quantity(50)
                .cost(100.0)
                .state(true)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(categoryPersistencePort.findByIdAndEnterpriseId(1L, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productPersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(productPersistencePort.create(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        doNothing().when(productEventPort).publishCreatedStockEvent(any(ProductSyncDto.class));

        // Act
        Product result = productService.create(newProduct);

        // Assert
        assertNotNull(result);
        verify(productTypePersistencePort, never()).findByIdAndEnterpriseId(anyLong(), anyString());
        verify(productPersistencePort, times(2)).create(any(Product.class));
    }
}
