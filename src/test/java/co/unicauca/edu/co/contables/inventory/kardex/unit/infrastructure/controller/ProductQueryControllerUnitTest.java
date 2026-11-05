package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.product.IProductQueryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller.ProductQueryController;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.ProductDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper.IProductResponseMapper;

@ExtendWith(MockitoExtension.class)
public class ProductQueryControllerUnitTest {
    
    @Mock
    private IProductQueryPort productQueryPort;
    
    @Mock
    private IProductResponseMapper productResponseMapper;
    
    @InjectMocks
    private ProductQueryController productQueryController;
    
    private String enterpriseId;
    private Product product1;
    private Product product2;
    private ProductDtoResponse productResponse1;
    private ProductDtoResponse productResponse2;
    
    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        
        // Setup product 1
        product1 = Product.builder()
                .id(1L)
                .productId(100L)
                .reference("REF-001")
                .name("Product A")
                .presentation("Box")
                .enterpriseId(enterpriseId)
                .state(true)
                .build();
        
        // Setup product 2
        product2 = Product.builder()
                .id(2L)
                .productId(200L)
                .reference("REF-002")
                .name("Product B")
                .presentation("Bottle")
                .enterpriseId(enterpriseId)
                .state(true)
                .build();
        
        // Setup response 1
        productResponse1 = new ProductDtoResponse();
        productResponse1.setId(1L);
        productResponse1.setProductId(100L);
        productResponse1.setReference("REF-001");
        productResponse1.setName("Product A");
        productResponse1.setPresentation("Box");
        productResponse1.setEnterpriseId(enterpriseId);
        
        // Setup response 2
        productResponse2 = new ProductDtoResponse();
        productResponse2.setId(2L);
        productResponse2.setProductId(200L);
        productResponse2.setReference("REF-002");
        productResponse2.setName("Product B");
        productResponse2.setPresentation("Bottle");
        productResponse2.setEnterpriseId(enterpriseId);
    }
    
    @Test
    @DisplayName("Should get all products successfully")
    void testGetAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product1, product2);
        when(productQueryPort.findAll(enterpriseId)).thenReturn(products);
        when(productResponseMapper.toDtoResponse(product1)).thenReturn(productResponse1);
        when(productResponseMapper.toDtoResponse(product2)).thenReturn(productResponse2);
        
        // Act
        ResponseDto<List<ProductDtoResponse>> response = 
            productQueryController.getAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("Products retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
        assertEquals(100L, response.getData().get(0).getProductId());
        assertEquals(200L, response.getData().get(1).getProductId());
        
        verify(productQueryPort).findAll(enterpriseId);
        verify(productResponseMapper, times(2)).toDtoResponse(any(Product.class));
    }
    
    @Test
    @DisplayName("Should return empty list when no products found")
    void testGetAllProductsWithNoProducts() {
        // Arrange
        when(productQueryPort.findAll(enterpriseId)).thenReturn(Collections.emptyList());
        
        // Act
        ResponseDto<List<ProductDtoResponse>> response = 
            productQueryController.getAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("Products retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
        
        verify(productQueryPort).findAll(enterpriseId);
        verify(productResponseMapper, never()).toDtoResponse(any());
    }
    
    @Test
    @DisplayName("Should get products for different enterprises")
    void testGetAllProductsForDifferentEnterprises() {
        // Arrange
        String enterpriseId1 = "ENT-001";
        String enterpriseId2 = "ENT-002";
        
        when(productQueryPort.findAll(enterpriseId1)).thenReturn(Arrays.asList(product1));
        when(productQueryPort.findAll(enterpriseId2)).thenReturn(Arrays.asList(product2));
        when(productResponseMapper.toDtoResponse(product1)).thenReturn(productResponse1);
        when(productResponseMapper.toDtoResponse(product2)).thenReturn(productResponse2);
        
        // Act
        ResponseDto<List<ProductDtoResponse>> response1 = 
            productQueryController.getAllProducts(enterpriseId1);
        ResponseDto<List<ProductDtoResponse>> response2 = 
            productQueryController.getAllProducts(enterpriseId2);
        
        // Assert
        assertEquals(1, response1.getData().size());
        assertEquals(1, response2.getData().size());
        assertEquals("REF-001", response1.getData().get(0).getReference());
        assertEquals("REF-002", response2.getData().get(0).getReference());
        
        verify(productQueryPort).findAll(enterpriseId1);
        verify(productQueryPort).findAll(enterpriseId2);
    }
    
    @Test
    @DisplayName("Should map products to DTOs correctly")
    void testGetAllProductsMapping() {
        // Arrange
        List<Product> products = Arrays.asList(product1, product2);
        when(productQueryPort.findAll(enterpriseId)).thenReturn(products);
        when(productResponseMapper.toDtoResponse(product1)).thenReturn(productResponse1);
        when(productResponseMapper.toDtoResponse(product2)).thenReturn(productResponse2);
        
        // Act
        ResponseDto<List<ProductDtoResponse>> response = 
            productQueryController.getAllProducts(enterpriseId);
        
        // Assert
        List<ProductDtoResponse> data = response.getData();
        assertEquals(2, data.size());
        assertEquals("Product A", data.get(0).getName());
        assertEquals("Product B", data.get(1).getName());
        assertEquals("Box", data.get(0).getPresentation());
        assertEquals("Bottle", data.get(1).getPresentation());
    }
    
    @Test
    @DisplayName("Should verify response structure")
    void testGetAllProductsResponseStructure() {
        // Arrange
        List<Product> products = Arrays.asList(product1);
        when(productQueryPort.findAll(enterpriseId)).thenReturn(products);
        when(productResponseMapper.toDtoResponse(product1)).thenReturn(productResponse1);
        
        // Act
        ResponseDto<List<ProductDtoResponse>> response = 
            productQueryController.getAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response.getData());
        assertNotNull(response.getMessage());
        assertTrue(response.getStatus() > 0);
        assertTrue(response.getData() instanceof List);
    }
    
    @Test
    @DisplayName("Should handle single product")
    void testGetAllProductsWithSingleProduct() {
        // Arrange
        when(productQueryPort.findAll(enterpriseId)).thenReturn(Arrays.asList(product1));
        when(productResponseMapper.toDtoResponse(product1)).thenReturn(productResponse1);
        
        // Act
        ResponseDto<List<ProductDtoResponse>> response = 
            productQueryController.getAllProducts(enterpriseId);
        
        // Assert
        assertEquals(1, response.getData().size());
        assertEquals(100L, response.getData().get(0).getProductId());
        assertEquals("Product A", response.getData().get(0).getName());
        
        verify(productQueryPort).findAll(enterpriseId);
        verify(productResponseMapper, times(1)).toDtoResponse(product1);
    }
    
    @Test
    @DisplayName("Should handle large list of products")
    void testGetAllProductsWithLargeList() {
        // Arrange
        List<Product> manyProducts = Arrays.asList(product1, product2, product1, product2, product1);
        when(productQueryPort.findAll(enterpriseId)).thenReturn(manyProducts);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productResponse1);
        
        // Act
        ResponseDto<List<ProductDtoResponse>> response = 
            productQueryController.getAllProducts(enterpriseId);
        
        // Assert
        assertEquals(5, response.getData().size());
        verify(productQueryPort).findAll(enterpriseId);
        verify(productResponseMapper, times(5)).toDtoResponse(any(Product.class));
    }
    
    @Test
    @DisplayName("Should verify mapper interactions")
    void testGetAllProductsMapperInteractions() {
        // Arrange
        List<Product> products = Arrays.asList(product1, product2);
        when(productQueryPort.findAll(enterpriseId)).thenReturn(products);
        when(productResponseMapper.toDtoResponse(product1)).thenReturn(productResponse1);
        when(productResponseMapper.toDtoResponse(product2)).thenReturn(productResponse2);
        
        // Act
        productQueryController.getAllProducts(enterpriseId);
        
        // Assert
        verify(productResponseMapper, times(1)).toDtoResponse(product1);
        verify(productResponseMapper, times(1)).toDtoResponse(product2);
        verifyNoMoreInteractions(productResponseMapper);
    }
    
    @Test
    @DisplayName("Should verify query port interactions")
    void testGetAllProductsQueryPortInteractions() {
        // Arrange
        when(productQueryPort.findAll(enterpriseId)).thenReturn(Collections.emptyList());
        
        // Act
        productQueryController.getAllProducts(enterpriseId);
        
        // Assert
        verify(productQueryPort, times(1)).findAll(enterpriseId);
        verifyNoMoreInteractions(productQueryPort);
    }
    
    @Test
    @DisplayName("Should handle products with all fields populated")
    void testGetAllProductsWithAllFields() {
        // Arrange
        when(productQueryPort.findAll(enterpriseId)).thenReturn(Arrays.asList(product1));
        when(productResponseMapper.toDtoResponse(product1)).thenReturn(productResponse1);
        
        // Act
        ResponseDto<List<ProductDtoResponse>> response = 
            productQueryController.getAllProducts(enterpriseId);
        
        // Assert
        ProductDtoResponse product = response.getData().get(0);
        assertNotNull(product.getId());
        assertNotNull(product.getProductId());
        assertNotNull(product.getReference());
        assertNotNull(product.getName());
        assertNotNull(product.getPresentation());
        assertNotNull(product.getEnterpriseId());
    }
    
    @Test
    @DisplayName("Should return correct status code")
    void testGetAllProductsStatusCode() {
        // Arrange
        when(productQueryPort.findAll(enterpriseId)).thenReturn(Collections.emptyList());
        
        // Act
        ResponseDto<List<ProductDtoResponse>> response = 
            productQueryController.getAllProducts(enterpriseId);
        
        // Assert
        assertEquals(200, response.getStatus());
    }
}
