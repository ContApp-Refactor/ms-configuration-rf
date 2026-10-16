package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.input.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IProductQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller.ProductQueryController;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.ProductDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.mapper.IProductResponseMapper;

/**
 * @brief Unit tests for ProductQueryController
 * 
 * Tests the REST controller for product query operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductQueryController Tests")
public class ProductQueryControllerUnitTest {
    
    @Mock
    private IProductResponseMapper productResponseMapper;
    
    @Mock
    private IProductQueryOutputPort productQueryOutputPort;
    
    @InjectMocks
    private ProductQueryController productQueryController;
    
    private String enterpriseId;
    private List<Product> productList;
    private List<ProductDTOResponse> productDtoResponseList;
    private Product product;
    private ProductDTOResponse productDtoResponse;
    
    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        
        // Setup product
        product = Product.builder()
            .id(1L)
            .productId(100L)
            .name("Product A")
            .reference("REF-001")
            .presentation("Box")
            .enterpriseId(enterpriseId)
            .state(true)
            .build();
        
        // Setup product list
        productList = new ArrayList<>();
        productList.add(product);
        
        // Setup DTO response
        productDtoResponse = new ProductDTOResponse();
        productDtoResponse.setId(1L);
        productDtoResponse.setProductId(100L);
        productDtoResponse.setName("Product A");
        productDtoResponse.setReference("REF-001");
        productDtoResponse.setPresentation("Box");
        productDtoResponse.setEnterpriseId(enterpriseId);
        
        // Setup DTO response list
        productDtoResponseList = new ArrayList<>();
        productDtoResponseList.add(productDtoResponse);
    }
    
    @Test
    @DisplayName("Should retrieve all products by enterprise successfully")
    void testGetAllProductsByEnterprise_Success() {
        // Arrange
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(productList);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("Products retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(productDtoResponse, response.getData().get(0));
        
        verify(productQueryOutputPort, times(1)).findAll(enterpriseId);
        verify(productResponseMapper, times(1)).toDtoResponse(product);
    }
    
    @Test
    @DisplayName("Should handle empty product list")
    void testGetAllProductsByEnterprise_EmptyList() {
        // Arrange
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(new ArrayList<>());
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getData());
        assertEquals(0, response.getData().size());
    }
    
    @Test
    @DisplayName("Should retrieve multiple products successfully")
    void testGetAllProductsByEnterprise_MultipleProducts() {
        // Arrange
        List<Product> multipleProducts = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            multipleProducts.add(Product.builder()
                .id((long) i)
                .productId((long) (100 + i))
                .name("Product " + i)
                .reference("REF-00" + i)
                .enterpriseId(enterpriseId)
                .build());
        }
        
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(multipleProducts);
        when(productResponseMapper.toDtoResponse(any(Product.class)))
            .thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                ProductDTOResponse dto = new ProductDTOResponse();
                dto.setId(p.getId());
                dto.setProductId(p.getProductId());
                dto.setName(p.getName());
                return dto;
            });
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(5, response.getData().size());
        verify(productResponseMapper, times(5)).toDtoResponse(any(Product.class));
    }
    
    @Test
    @DisplayName("Should handle different enterprise IDs")
    void testGetAllProductsByEnterprise_DifferentEnterpriseIds() {
        // Arrange
        String differentEnterpriseId = "ENT-999";
        when(productQueryOutputPort.findAll(differentEnterpriseId)).thenReturn(productList);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(differentEnterpriseId);
        
        // Assert
        assertNotNull(response);
        verify(productQueryOutputPort, times(1)).findAll(differentEnterpriseId);
    }
    
    @Test
    @DisplayName("Should verify mapper is called for each product")
    void testGetAllProductsByEnterprise_MapperVerification() {
        // Arrange
        List<Product> threeProducts = new ArrayList<>();
        threeProducts.add(product);
        threeProducts.add(product);
        threeProducts.add(product);
        
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(threeProducts);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        verify(productResponseMapper, times(3)).toDtoResponse(any(Product.class));
    }
    
    @Test
    @DisplayName("Should verify response structure")
    void testGetAllProductsByEnterprise_ResponseStructure() {
        // Arrange
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(productList);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getData());
        assertNotNull(response.getMessage());
        assertNotNull(response.getStatus());
    }
    
    @Test
    @DisplayName("Should handle products with different states")
    void testGetAllProductsByEnterprise_DifferentStates() {
        // Arrange
        List<Product> mixedProducts = new ArrayList<>();
        mixedProducts.add(Product.builder()
            .id(1L)
            .productId(100L)
            .name("Active Product")
            .state(true)
            .enterpriseId(enterpriseId)
            .build());
        mixedProducts.add(Product.builder()
            .id(2L)
            .productId(200L)
            .name("Inactive Product")
            .state(false)
            .enterpriseId(enterpriseId)
            .build());
        
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(mixedProducts);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(2, response.getData().size());
    }
    
    @Test
    @DisplayName("Should handle large product lists")
    void testGetAllProductsByEnterprise_LargeList() {
        // Arrange
        List<Product> largeProductList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeProductList.add(Product.builder()
                .id((long) i)
                .productId((long) (1000 + i))
                .name("Product " + i)
                .enterpriseId(enterpriseId)
                .build());
        }
        
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(largeProductList);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(100, response.getData().size());
        verify(productResponseMapper, times(100)).toDtoResponse(any(Product.class));
    }
    
    @Test
    @DisplayName("Should verify status code is 200")
    void testGetAllProductsByEnterprise_StatusCode() {
        // Arrange
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(productList);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        assertEquals(200, response.getStatus());
    }
    
    @Test
    @DisplayName("Should verify success message")
    void testGetAllProductsByEnterprise_SuccessMessage() {
        // Arrange
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(productList);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        assertEquals("Products retrieved successfully", response.getMessage());
    }
    
    @Test
    @DisplayName("Should handle empty enterprise ID")
    void testGetAllProductsByEnterprise_EmptyEnterpriseId() {
        // Arrange
        String emptyEnterpriseId = "";
        when(productQueryOutputPort.findAll(emptyEnterpriseId)).thenReturn(new ArrayList<>());
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(emptyEnterpriseId);
        
        // Assert
        assertNotNull(response);
        verify(productQueryOutputPort, times(1)).findAll(emptyEnterpriseId);
    }
    
    @Test
    @DisplayName("Should handle products with all fields populated")
    void testGetAllProductsByEnterprise_AllFieldsPopulated() {
        // Arrange
        Product completeProduct = Product.builder()
            .id(1L)
            .productId(100L)
            .name("Complete Product")
            .reference("REF-COMPLETE")
            .presentation("Premium Box")
            .enterpriseId(enterpriseId)
            .state(true)
            .recordsKardex(new ArrayList<>())
            .build();
        
        List<Product> completeList = new ArrayList<>();
        completeList.add(completeProduct);
        
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(completeList);
        when(productResponseMapper.toDtoResponse(completeProduct)).thenReturn(productDtoResponse);
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getData().size());
    }
    
    @Test
    @DisplayName("Should verify port is called exactly once")
    void testGetAllProductsByEnterprise_PortVerification() {
        // Arrange
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(productList);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        verify(productQueryOutputPort, times(1)).findAll(eq(enterpriseId));
    }
    
    @Test
    @DisplayName("Should handle multiple sequential queries")
    void testMultipleSequentialQueries() {
        // Arrange
        String enterprise1 = "ENT-001";
        String enterprise2 = "ENT-002";
        
        when(productQueryOutputPort.findAll(enterprise1)).thenReturn(productList);
        when(productQueryOutputPort.findAll(enterprise2)).thenReturn(new ArrayList<>());
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response1 = 
            productQueryController.getAllProductsByEnterprise(enterprise1);
        ResponseDTO<List<ProductDTOResponse>> response2 = 
            productQueryController.getAllProductsByEnterprise(enterprise2);
        
        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(1, response1.getData().size());
        assertEquals(0, response2.getData().size());
        
        verify(productQueryOutputPort, times(1)).findAll(enterprise1);
        verify(productQueryOutputPort, times(1)).findAll(enterprise2);
    }
    
    @Test
    @DisplayName("Should handle products with special characters in name")
    void testGetAllProductsByEnterprise_SpecialCharacters() {
        // Arrange
        Product specialProduct = Product.builder()
            .id(1L)
            .productId(100L)
            .name("Prod@ct #1 & Sp€cial")
            .reference("REF-SP#1")
            .enterpriseId(enterpriseId)
            .build();
        
        List<Product> specialList = new ArrayList<>();
        specialList.add(specialProduct);
        
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(specialList);
        when(productResponseMapper.toDtoResponse(any(Product.class))).thenReturn(productDtoResponse);
        
        // Act
        ResponseDTO<List<ProductDTOResponse>> response = 
            productQueryController.getAllProductsByEnterprise(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getData().size());
    }
}

