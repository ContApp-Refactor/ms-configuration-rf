package co.unicauca.edu.co.contables.inventory.products.unit.application.service.productType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.productType.ProductTypeService;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeAssociatedException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeInUseException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeNameAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductTypeServiceUnitTest {

    @Mock
    private IProductTypePersistencePort productTypePersistencePort;

    @Mock
    private IProductServicePort productServicePort;

    @InjectMocks
    private ProductTypeService productTypeService;

    private ProductType productType;
    private String enterpriseId;
    private Long productTypeId;

    @BeforeEach
    void setUp() {
        enterpriseId = "enterprise-123";
        productTypeId = 1L;

        productType = ProductType.builder()
                .id(productTypeId)
                .name("Tipo Producto Test")
                .description("Descripción del tipo de producto")
                .enterpriseId(enterpriseId)
                .state(true)
                .build();
    }

    // CREATE tests
    @Test
    @DisplayName("Debe crear tipo de producto exitosamente")
    void testCreateProductTypeSuccess() {
        // Arrange
        when(productTypePersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(productTypePersistencePort.save(any(ProductType.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        ProductType result = productTypeService.createProductType(productType);

        // Assert
        assertNotNull(result);
        assertEquals("Tipo producto test", result.getName());
        verify(productTypePersistencePort, times(1)).existsByNameAndEnterpriseId(anyString(), eq(enterpriseId));
        verify(productTypePersistencePort, times(1)).save(any(ProductType.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear tipo de producto con nombre duplicado")
    void testCreateProductTypeWithDuplicateName() {
        // Arrange
        when(productTypePersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ProductTypeNameAlreadyExistsException.class, () ->
                productTypeService.createProductType(productType)
        );
        verify(productTypePersistencePort, times(1)).existsByNameAndEnterpriseId(anyString(), eq(enterpriseId));
        verify(productTypePersistencePort, never()).save(any(ProductType.class));
    }

    @Test
    @DisplayName("Debe normalizar nombre al crear tipo de producto")
    void testCreateProductTypeNormalizesName() {
        // Arrange
        productType.setName("  tipo   producto   test  ");
        when(productTypePersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(productTypePersistencePort.save(any(ProductType.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        ProductType result = productTypeService.createProductType(productType);

        // Assert
        assertNotNull(result);
        assertEquals("Tipo   producto   test", result.getName());
        verify(productTypePersistencePort, times(1)).save(any(ProductType.class));
    }

    // UPDATE tests
    @Test
    @DisplayName("Debe actualizar tipo de producto exitosamente")
    void testUpdateProductTypeSuccess() {
        // Arrange
        ProductType updateData = ProductType.builder()
                .name("Tipo Producto Actualizado")
                .description("Nueva descripción")
                .enterpriseId(enterpriseId)
                .build();

        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productServicePort.findAllByProductType(productTypeId))
                .thenReturn(Collections.emptyList());
        when(productTypePersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productTypeId)))
                .thenReturn(false);
        when(productTypePersistencePort.update(eq(productTypeId), any(ProductType.class)))
                .thenAnswer(inv -> inv.getArgument(1));

        // Act
        ProductType result = productTypeService.updateProductType(productTypeId, enterpriseId, updateData);

        // Assert
        assertNotNull(result);
        assertEquals("Tipo producto actualizado", result.getName());
        verify(productTypePersistencePort, times(1)).update(eq(productTypeId), any(ProductType.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar tipo de producto que no existe")
    void testUpdateProductTypeNotFound() {
        // Arrange
        ProductType updateData = ProductType.builder()
                .name("Tipo Actualizado")
                .enterpriseId(enterpriseId)
                .build();

        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductTypeNotFoundException.class, () ->
                productTypeService.updateProductType(productTypeId, enterpriseId, updateData)
        );
        verify(productTypePersistencePort, never()).update(anyLong(), any(ProductType.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar tipo de producto en uso")
    void testUpdateProductTypeInUse() {
        // Arrange
        ProductType updateData = ProductType.builder()
                .name("Tipo Actualizado")
                .enterpriseId(enterpriseId)
                .build();

        Product productInUse = Product.builder()
                .id(1L)
                .usageCount(5)
                .build();

        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productServicePort.findAllByProductType(productTypeId))
                .thenReturn(Arrays.asList(productInUse));

        // Act & Assert
        assertThrows(ProductTypeInUseException.class, () ->
                productTypeService.updateProductType(productTypeId, enterpriseId, updateData)
        );
        verify(productTypePersistencePort, never()).update(anyLong(), any(ProductType.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con nombre duplicado en otro registro")
    void testUpdateProductTypeWithDuplicateName() {
        // Arrange
        ProductType updateData = ProductType.builder()
                .name("Tipo Existente")
                .enterpriseId(enterpriseId)
                .build();

        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productServicePort.findAllByProductType(productTypeId))
                .thenReturn(Collections.emptyList());
        when(productTypePersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productTypeId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ProductTypeNameAlreadyExistsException.class, () ->
                productTypeService.updateProductType(productTypeId, enterpriseId, updateData)
        );
        verify(productTypePersistencePort, never()).update(anyLong(), any(ProductType.class));
    }

    @Test
    @DisplayName("Debe normalizar nombre al actualizar tipo de producto")
    void testUpdateProductTypeNormalizesName() {
        // Arrange
        ProductType updateData = ProductType.builder()
                .name("  tipo   actualizado  ")
                .enterpriseId(enterpriseId)
                .build();

        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productServicePort.findAllByProductType(productTypeId))
                .thenReturn(Collections.emptyList());
        when(productTypePersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productTypeId)))
                .thenReturn(false);
        when(productTypePersistencePort.update(eq(productTypeId), any(ProductType.class)))
                .thenAnswer(inv -> inv.getArgument(1));

        // Act
        ProductType result = productTypeService.updateProductType(productTypeId, enterpriseId, updateData);

        // Assert
        assertNotNull(result);
        assertEquals("Tipo   actualizado", result.getName());
    }

    @Test
    @DisplayName("Debe permitir actualizar cuando productos asociados no están en uso")
    void testUpdateProductTypeWithProductsNotInUse() {
        // Arrange
        ProductType updateData = ProductType.builder()
                .name("Tipo Actualizado")
                .enterpriseId(enterpriseId)
                .build();

        Product productNotInUse = Product.builder()
                .id(1L)
                .usageCount(0)
                .build();

        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productServicePort.findAllByProductType(productTypeId))
                .thenReturn(Arrays.asList(productNotInUse));
        when(productTypePersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(productTypeId)))
                .thenReturn(false);
        when(productTypePersistencePort.update(eq(productTypeId), any(ProductType.class)))
                .thenAnswer(inv -> inv.getArgument(1));

        // Act
        ProductType result = productTypeService.updateProductType(productTypeId, enterpriseId, updateData);

        // Assert
        assertNotNull(result);
        verify(productTypePersistencePort, times(1)).update(eq(productTypeId), any(ProductType.class));
    }

    // DELETE tests
    @Test
    @DisplayName("Debe eliminar tipo de producto exitosamente")
    void testDeleteProductTypeSuccess() {
        // Arrange
        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productServicePort.findAllByProductType(productTypeId))
                .thenReturn(Collections.emptyList());

        // Act
        productTypeService.deleteProductType(productTypeId, enterpriseId);

        // Assert
        verify(productTypePersistencePort, times(1)).findByIdAndEnterpriseId(productTypeId, enterpriseId);
        verify(productServicePort, times(1)).findAllByProductType(productTypeId);
        verify(productTypePersistencePort, times(1)).delete(productTypeId);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar tipo de producto que no existe")
    void testDeleteProductTypeNotFound() {
        // Arrange
        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductTypeNotFoundException.class, () ->
                productTypeService.deleteProductType(productTypeId, enterpriseId)
        );
        verify(productTypePersistencePort, never()).delete(anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar tipo de producto con productos asociados")
    void testDeleteProductTypeWithAssociatedProducts() {
        // Arrange
        Product associatedProduct = Product.builder()
                .id(1L)
                .productTypeId(productTypeId)
                .build();

        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productServicePort.findAllByProductType(productTypeId))
                .thenReturn(Arrays.asList(associatedProduct));

        // Act & Assert
        assertThrows(ProductTypeAssociatedException.class, () ->
                productTypeService.deleteProductType(productTypeId, enterpriseId)
        );
        verify(productTypePersistencePort, never()).delete(anyLong());
    }

    // QUERY tests
    @Test
    @DisplayName("Debe buscar tipo de producto por ID exitosamente")
    void testFindByIdSuccess() {
        // Arrange
        when(productTypePersistencePort.findById(productTypeId))
                .thenReturn(Optional.of(productType));

        // Act
        Optional<ProductType> result = productTypeService.findById(productTypeId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(productTypeId, result.get().getId());
        verify(productTypePersistencePort, times(1)).findById(productTypeId);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando tipo de producto no existe")
    void testFindByIdNotFound() {
        // Arrange
        when(productTypePersistencePort.findById(productTypeId))
                .thenReturn(Optional.empty());

        // Act
        Optional<ProductType> result = productTypeService.findById(productTypeId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe obtener tipo de producto por ID y empresa exitosamente")
    void testGetProductTypeByIdAndEnterpriseIdSuccess() {
        // Arrange
        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.of(productType));

        // Act
        ProductType result = productTypeService.getProductTypeByIdAndEnterpriseId(productTypeId, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(productTypeId, result.getId());
        assertEquals(enterpriseId, result.getEnterpriseId());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando tipo de producto no existe por ID y empresa")
    void testGetProductTypeByIdAndEnterpriseIdNotFound() {
        // Arrange
        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductTypeNotFoundException.class, () ->
                productTypeService.getProductTypeByIdAndEnterpriseId(productTypeId, enterpriseId)
        );
    }

    @Test
    @DisplayName("Debe obtener tipos de producto paginados con ordenamiento")
    void testGetAllProductTypesByWithSort() {
        // Arrange
        List<ProductType> productTypes = Arrays.asList(productType);
        Page<ProductType> page = new PageImpl<>(productTypes);
        when(productTypePersistencePort.getAllProductTypesByWithSort(enterpriseId, 0, 10, "name", "asc"))
                .thenReturn(page);

        // Act
        Page<ProductType> result = productTypeService.getAllProductTypesByWithSort(enterpriseId, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productTypePersistencePort, times(1)).getAllProductTypesByWithSort(enterpriseId, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("Debe buscar tipos de producto por empresa y término de búsqueda")
    void testFindByEnterpriseIdAndSearch() {
        // Arrange
        List<ProductType> productTypes = Arrays.asList(productType);
        Page<ProductType> page = new PageImpl<>(productTypes);
        when(productTypePersistencePort.findByEnterpriseIdAndSearch(enterpriseId, "test", 0, 10, "name", "asc"))
                .thenReturn(page);

        // Act
        Page<ProductType> result = productTypeService.findByEnterpriseIdAndSearch(enterpriseId, "test", 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productTypePersistencePort, times(1)).findByEnterpriseIdAndSearch(enterpriseId, "test", 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("Debe contar tipos de producto por empresa y búsqueda")
    void testCountByEnterpriseIdAndSearch() {
        // Arrange
        when(productTypePersistencePort.countByEnterpriseIdAndSearch(enterpriseId, "test"))
                .thenReturn(5L);

        // Act
        long result = productTypeService.countByEnterpriseIdAndSearch(enterpriseId, "test");

        // Assert
        assertEquals(5L, result);
        verify(productTypePersistencePort, times(1)).countByEnterpriseIdAndSearch(enterpriseId, "test");
    }

    @Test
    @DisplayName("Debe contar tipos de producto por empresa")
    void testCountByEnterpriseId() {
        // Arrange
        when(productTypePersistencePort.countByEnterpriseId(enterpriseId))
                .thenReturn(10L);

        // Act
        long result = productTypeService.countByEnterpriseId(enterpriseId);

        // Assert
        assertEquals(10L, result);
        verify(productTypePersistencePort, times(1)).countByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Debe obtener tipos de producto activados con paginación")
    void testFindActivatedWithPagination() {
        // Arrange
        List<ProductType> productTypes = Arrays.asList(productType);
        Page<ProductType> page = new PageImpl<>(productTypes);
        when(productTypePersistencePort.findActivatedByEnterpriseId(enterpriseId, 0, 10))
                .thenReturn(page);

        // Act
        Page<ProductType> result = productTypeService.findActivatedWithPagination(enterpriseId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productTypePersistencePort, times(1)).findActivatedByEnterpriseId(enterpriseId, 0, 10);
    }

    @Test
    @DisplayName("Debe contar tipos de producto activados por empresa")
    void testCountActivatedByEnterpriseId() {
        // Arrange
        when(productTypePersistencePort.countActivatedByEnterpriseId(enterpriseId))
                .thenReturn(8L);

        // Act
        long result = productTypeService.countActivatedByEnterpriseId(enterpriseId);

        // Assert
        assertEquals(8L, result);
        verify(productTypePersistencePort, times(1)).countActivatedByEnterpriseId(enterpriseId);
    }

    // STATE CHANGE tests
    @Test
    @DisplayName("Debe cambiar estado de activo a inactivo")
    void testChangeStateFromActiveToInactive() {
        // Arrange
        productType.setState(true);
        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productTypePersistencePort.save(any(ProductType.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        productTypeService.changeState(productTypeId, enterpriseId);

        // Assert
        verify(productTypePersistencePort, times(1)).findByIdAndEnterpriseId(productTypeId, enterpriseId);
        verify(productTypePersistencePort, times(1)).save(any(ProductType.class));
        assertFalse(productType.isState());
    }

    @Test
    @DisplayName("Debe cambiar estado de inactivo a activo")
    void testChangeStateFromInactiveToActive() {
        // Arrange
        productType.setState(false);
        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.of(productType));
        when(productTypePersistencePort.save(any(ProductType.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        productTypeService.changeState(productTypeId, enterpriseId);

        // Assert
        verify(productTypePersistencePort, times(1)).findByIdAndEnterpriseId(productTypeId, enterpriseId);
        verify(productTypePersistencePort, times(1)).save(any(ProductType.class));
        assertTrue(productType.isState());
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar estado de tipo de producto que no existe")
    void testChangeStateNotFound() {
        // Arrange
        when(productTypePersistencePort.findByIdAndEnterpriseId(productTypeId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductTypeNotFoundException.class, () ->
                productTypeService.changeState(productTypeId, enterpriseId)
        );
        verify(productTypePersistencePort, never()).save(any(ProductType.class));
    }
}
