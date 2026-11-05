package co.unicauca.edu.co.contables.inventory.products.unit.application.service.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.product.ProductUsageService;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductUsageServiceUnitTest {

    @Mock
    private IProductPersistencePort productPersistencePort;

    @InjectMocks
    private ProductUsageService productUsageService;

    private Product product;
    private Long productId;

    @BeforeEach
    void setUp() {
        productId = 1L;

        product = Product.builder()
                .id(productId)
                .name("PRODUCTO TEST")
                .code("PROD-1-1")
                .reference("REF001")
                .description("Producto para testing")
                .unitOfMeasureId(1L)
                .categoryId(1L)
                .productTypeId(1L)
                .enterpriseId("1")
                .quantity(100)
                .cost(100.0)
                .usageCount(0)
                .state(true)
                .build();
    }

    @Test
    @DisplayName("Debe incrementar contador de uso exitosamente")
    void testIncrementUsageCountSuccess() {
        // Arrange
        when(productPersistencePort.findById(productId))
                .thenReturn(Optional.of(product));
        when(productPersistencePort.create(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        productUsageService.incrementUsageCount(productId);

        // Assert
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productPersistencePort, times(1)).findById(productId);
        verify(productPersistencePort, times(1)).create(productCaptor.capture());
        
        Product savedProduct = productCaptor.getValue();
        assertEquals(1, savedProduct.getUsageCount());
    }

    @Test
    @DisplayName("Debe incrementar contador de uso múltiples veces")
    void testIncrementUsageCountMultipleTimes() {
        // Arrange
        product.setUsageCount(5);
        when(productPersistencePort.findById(productId))
                .thenReturn(Optional.of(product));
        when(productPersistencePort.create(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        productUsageService.incrementUsageCount(productId);

        // Assert
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productPersistencePort, times(1)).findById(productId);
        verify(productPersistencePort, times(1)).create(productCaptor.capture());
        
        Product savedProduct = productCaptor.getValue();
        assertEquals(6, savedProduct.getUsageCount());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando producto no existe")
    void testIncrementUsageCountProductNotFound() {
        // Arrange
        when(productPersistencePort.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () ->
                productUsageService.incrementUsageCount(productId)
        );
        verify(productPersistencePort, times(1)).findById(productId);
        verify(productPersistencePort, times(0)).create(any(Product.class));
    }

    @Test
    @DisplayName("Debe incrementar contador desde cero")
    void testIncrementUsageCountFromZero() {
        // Arrange
        product.setUsageCount(0);
        when(productPersistencePort.findById(productId))
                .thenReturn(Optional.of(product));
        when(productPersistencePort.create(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        productUsageService.incrementUsageCount(productId);

        // Assert
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productPersistencePort, times(1)).create(productCaptor.capture());
        
        Product savedProduct = productCaptor.getValue();
        assertEquals(1, savedProduct.getUsageCount());
    }

    @Test
    @DisplayName("Debe incrementar contador con valor alto")
    void testIncrementUsageCountWithHighValue() {
        // Arrange
        product.setUsageCount(999);
        when(productPersistencePort.findById(productId))
                .thenReturn(Optional.of(product));
        when(productPersistencePort.create(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        productUsageService.incrementUsageCount(productId);

        // Assert
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productPersistencePort, times(1)).create(productCaptor.capture());
        
        Product savedProduct = productCaptor.getValue();
        assertEquals(1000, savedProduct.getUsageCount());
    }

    @Test
    @DisplayName("Debe mantener otras propiedades del producto sin cambios")
    void testIncrementUsageCountPreservesOtherProperties() {
        // Arrange
        when(productPersistencePort.findById(productId))
                .thenReturn(Optional.of(product));
        when(productPersistencePort.create(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        productUsageService.incrementUsageCount(productId);

        // Assert
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productPersistencePort, times(1)).create(productCaptor.capture());
        
        Product savedProduct = productCaptor.getValue();
        assertEquals(productId, savedProduct.getId());
        assertEquals("PRODUCTO TEST", savedProduct.getName());
        assertEquals("PROD-1-1", savedProduct.getCode());
        assertEquals("REF001", savedProduct.getReference());
        assertEquals(100, savedProduct.getQuantity());
        assertEquals(100.0, savedProduct.getCost());
        assertEquals(true, savedProduct.isState());
    }
}
