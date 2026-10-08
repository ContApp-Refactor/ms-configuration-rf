package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.remoteSync;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.adapter.ProductClientAdapter;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.mapper.IProductClientMapper;
import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductSyncServicePort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.GenericErrorException;

/**
 * @brief Unit tests for ProductClientAdapter
 *
 * Tests the in-process adapter that retrieves products from the products
 * module. In the monolith the former WebClient proxy (IProductClient) was
 * replaced by IProductSyncServicePort, so the source DTOs are now the
 * products-module ProductSyncDto while the mapper keeps receiving the
 * kardex-local ProductSyncDto built by the adapter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductClientAdapter Unit Tests")
class ProductClientAdapterUnitTest {

    @Mock
    private IProductClientMapper productClientMapper;

    @Mock
    private IProductSyncServicePort productSyncServicePort;

    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;

    @InjectMocks
    private ProductClientAdapter productClientAdapter;

    private String enterpriseId;
    private Instant since;
    private List<ProductSyncDto> productSyncDtos;
    private List<Product> products;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-123";
        since = Instant.now().minusSeconds(3600); // 1 hour ago

        // Initialize ProductSyncDto list
        productSyncDtos = new ArrayList<>();

        ProductSyncDto dto1 = new ProductSyncDto();
        dto1.setProductId(1L);
        dto1.setName("Product 1");
        dto1.setReference("REF-001");
        dto1.setEnterpriseId(enterpriseId);
        dto1.setPresentation("Box");
        dto1.setState(true);
        productSyncDtos.add(dto1);

        ProductSyncDto dto2 = new ProductSyncDto();
        dto2.setProductId(2L);
        dto2.setName("Product 2");
        dto2.setReference("REF-002");
        dto2.setEnterpriseId(enterpriseId);
        dto2.setPresentation("Pack");
        dto2.setState(true);
        productSyncDtos.add(dto2);

        // Initialize Product domain list
        products = new ArrayList<>();

        Product product1 = new Product();
        product1.setProductId(1L);
        product1.setName("Product 1");
        product1.setReference("REF-001");
        product1.setEnterpriseId(enterpriseId);
        product1.setPresentation("Box");
        products.add(product1);

        Product product2 = new Product();
        product2.setProductId(2L);
        product2.setName("Product 2");
        product2.setReference("REF-002");
        product2.setEnterpriseId(enterpriseId);
        product2.setPresentation("Pack");
        products.add(product2);
    }

    @Test
    @DisplayName("Should retrieve all products by enterprise ID successfully")
    void testFindAllProductsByEnterpriseId_Success() {
        // Arrange
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any()))
            .thenReturn(products.get(0), products.get(1));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());

        verify(productSyncServicePort, times(1)).findByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, times(2)).toDomain(any());
    }

    @Test
    @DisplayName("Should return empty list when no products found")
    void testFindAllProductsByEnterpriseId_EmptyList() {
        // Arrange
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(new ArrayList<>());

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productSyncServicePort, times(1)).findByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should retrieve single product successfully")
    void testFindAllProductsByEnterpriseId_SingleProduct() {
        // Arrange
        List<ProductSyncDto> singleDto = List.of(productSyncDtos.get(0));
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(singleDto);
        when(productClientMapper.toDomain(any()))
            .thenReturn(products.get(0));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("REF-001", result.get(0).getReference());

        verify(productSyncServicePort, times(1)).findByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, times(1)).toDomain(any());
    }

    @Test
    @DisplayName("Should handle multiple products successfully")
    void testFindAllProductsByEnterpriseId_MultipleProducts() {
        // Arrange
        List<ProductSyncDto> manyDtos = new ArrayList<>();
        List<Product> manyProducts = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ProductSyncDto dto = new ProductSyncDto();
            dto.setProductId((long) i);
            dto.setName("Product " + i);
            dto.setReference("REF-" + String.format("%03d", i));
            dto.setEnterpriseId(enterpriseId);
            manyDtos.add(dto);

            Product product = new Product();
            product.setProductId((long) i);
            product.setName("Product " + i);
            product.setReference("REF-" + String.format("%03d", i));
            product.setEnterpriseId(enterpriseId);
            manyProducts.add(product);
        }

        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(manyDtos);
        when(productClientMapper.toDomain(any()))
            .thenAnswer(invocation -> {
                co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto dto =
                    invocation.getArgument(0);
                return manyProducts.stream()
                    .filter(p -> p.getProductId().equals(dto.getProductId()))
                    .findFirst()
                    .orElse(null);
            });

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.size());

        verify(productSyncServicePort, times(1)).findByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, times(10)).toDomain(any());
    }

    @Test
    @DisplayName("Should handle different enterprise IDs")
    void testFindAllProductsByEnterpriseId_DifferentEnterpriseIds() {
        // Arrange
        String differentEnterpriseId = "ENT-456";
        when(productSyncServicePort.findByEnterpriseId(differentEnterpriseId, since))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any()))
            .thenReturn(products.get(0), products.get(1));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(differentEnterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(productSyncServicePort, times(1)).findByEnterpriseId(differentEnterpriseId, since);
    }

    @Test
    @DisplayName("Should handle different timestamps")
    void testFindAllProductsByEnterpriseId_DifferentTimestamps() {
        // Arrange
        Instant differentSince = Instant.now().minusSeconds(7200); // 2 hours ago
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, differentSince))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any()))
            .thenReturn(products.get(0), products.get(1));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, differentSince);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(productSyncServicePort, times(1)).findByEnterpriseId(enterpriseId, differentSince);
    }

    @Test
    @DisplayName("Should handle client exception")
    void testFindAllProductsByEnterpriseId_ClientException() {
        // Arrange
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenThrow(new RuntimeException("Service unavailable"));

        // Act - the adapter swallows the port exception and returns an empty list
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert - reports the generic 500 error to the formatter (monolith contract)
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productSyncServicePort, times(1)).findByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, never()).toDomain(any());
        verify(formatterResultOutputPort, times(1))
            .returnErrorGenericResponse(500, "Unexpected error communicating with product service");

        // Assert - if the formatter itself throws, the exception propagates
        doThrow(new GenericErrorException(500, "Unexpected error communicating with product service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class, () ->
            productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since)
        );
        assertEquals("Unexpected error communicating with product service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should handle mapper exception")
    void testFindAllProductsByEnterpriseId_MapperException() {
        // Arrange
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any()))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act - the adapter swallows the mapper exception and returns an empty list
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert - reports the generic 500 error to the formatter (monolith contract)
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productSyncServicePort, times(1)).findByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, times(1)).toDomain(any());
        verify(formatterResultOutputPort, times(1))
            .returnErrorGenericResponse(500, "Unexpected error communicating with product service");

        // Assert - if the formatter itself throws, the exception propagates
        doThrow(new GenericErrorException(500, "Unexpected error communicating with product service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class, () ->
            productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since)
        );
        assertEquals("Unexpected error communicating with product service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should map all product fields correctly")
    void testFindAllProductsByEnterpriseId_FieldMapping() {
        // Arrange
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(List.of(productSyncDtos.get(0)));

        Product expectedProduct = new Product();
        expectedProduct.setProductId(1L);
        expectedProduct.setName("Product 1");
        expectedProduct.setReference("REF-001");
        expectedProduct.setEnterpriseId(enterpriseId);
        expectedProduct.setPresentation("Box");

        when(productClientMapper.toDomain(any()))
            .thenReturn(expectedProduct);

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        Product actualProduct = result.get(0);
        assertEquals(expectedProduct.getProductId(), actualProduct.getProductId());
        assertEquals(expectedProduct.getName(), actualProduct.getName());
        assertEquals(expectedProduct.getReference(), actualProduct.getReference());
        assertEquals(expectedProduct.getEnterpriseId(), actualProduct.getEnterpriseId());
        assertEquals(expectedProduct.getPresentation(), actualProduct.getPresentation());
    }

    @Test
    @DisplayName("Should handle products with null presentation")
    void testFindAllProductsByEnterpriseId_NullPresentation() {
        // Arrange
        ProductSyncDto dtoWithNullPresentation = new ProductSyncDto();
        dtoWithNullPresentation.setProductId(3L);
        dtoWithNullPresentation.setName("Product 3");
        dtoWithNullPresentation.setReference("REF-003");
        dtoWithNullPresentation.setEnterpriseId(enterpriseId);
        dtoWithNullPresentation.setPresentation(null);

        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(List.of(dtoWithNullPresentation));

        Product productWithNullPresentation = new Product();
        productWithNullPresentation.setProductId(3L);
        productWithNullPresentation.setName("Product 3");
        productWithNullPresentation.setReference("REF-003");
        productWithNullPresentation.setEnterpriseId(enterpriseId);
        productWithNullPresentation.setPresentation(null);

        when(productClientMapper.toDomain(any()))
            .thenReturn(productWithNullPresentation);

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getPresentation());
    }

    @Test
    @DisplayName("Should handle instant at epoch")
    void testFindAllProductsByEnterpriseId_EpochInstant() {
        // Arrange
        Instant epoch = Instant.EPOCH;
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, epoch))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any()))
            .thenReturn(products.get(0), products.get(1));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, epoch);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(productSyncServicePort, times(1)).findByEnterpriseId(enterpriseId, epoch);
    }

    @Test
    @DisplayName("Should handle instant in the future")
    void testFindAllProductsByEnterpriseId_FutureInstant() {
        // Arrange
        Instant future = Instant.now().plusSeconds(3600);
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, future))
            .thenReturn(new ArrayList<>());

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, future);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productSyncServicePort, times(1)).findByEnterpriseId(enterpriseId, future);
    }

    @Test
    @DisplayName("Should preserve order of products from client")
    void testFindAllProductsByEnterpriseId_OrderPreservation() {
        // Arrange
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any()))
            .thenReturn(products.get(0), products.get(1));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(products.get(0).getName(), result.get(0).getName());
        assertEquals(products.get(1).getName(), result.get(1).getName());
    }

    @Test
    @DisplayName("Should call mapper for each DTO")
    void testFindAllProductsByEnterpriseId_MapperInvocations() {
        // Arrange
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any()))
            .thenReturn(products.get(0), products.get(1));

        // Act
        productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        verify(productClientMapper, times(productSyncDtos.size())).toDomain(any());
    }
}
