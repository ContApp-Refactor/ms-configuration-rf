package co.unicauca.edu.co.contables.inventory.kardex.unit.application.kardex;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.command.KardexValidationService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.BusinessRuleException;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.ProductMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

@ExtendWith(MockitoExtension.class)
public class KardexValidationServiceUnitTest {
    
    @Mock
    private IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    
    @Mock
    private IProductRepository productRepository;
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private IMessageServicePort messageService;
    
    @InjectMocks
    private KardexValidationService kardexValidationService;
    
    private String factCode;
    private Long productId;
    private MovementType movementType;
    private ProductEntity productEntity;
    private Product product;
    private Kardex lastKardex;
    
    @BeforeEach
    void setUp() {
        factCode = "FACT001";
        productId = 1L;
        movementType = MovementType.PURCHASE;
        
        productEntity = new ProductEntity();
        productEntity.setProductId(productId);
        productEntity.setEnterpriseId("EMP001");
        productEntity.setState(true);
        
        product = Product.builder()
            .productId(productId)
            .enterpriseId("EMP001")
            .state(true)
            .build();
            
        lastKardex = new Kardex();
        lastKardex.setProductId(productId);
    }
    
    @Test
    @DisplayName("Should validate business rules and return product when all validations pass")
    void testValidateBusinessRulesAndGetProductSuccess() {
        // Arrange
        when(kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType))
            .thenReturn(false);
        when(productRepository.getReferenceByProductId(productId)).thenReturn(productEntity);
        when(productMapper.toDomain(productEntity)).thenReturn(product);
        
        // Act
        Product result = kardexValidationService.validateBusinessRulesAndGetProduct(factCode, productId, movementType);
        
        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals("EMP001", result.getEnterpriseId());
        assertTrue(result.isActive());
        
        verify(kardexQueryRepositoryPort).existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        verify(productRepository).getReferenceByProductId(productId);
        verify(productMapper).toDomain(productEntity);
    }
    
    @Test
    @DisplayName("Should throw error when duplicate record exists")
    void testValidateBusinessRulesAndGetProductWithDuplicate() {
        // Arrange
        when(kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType))
            .thenReturn(true);
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Duplicate record error");
        
        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            kardexValidationService.validateBusinessRulesAndGetProduct(factCode, productId, movementType);
        });
        
        assertEquals(400, exception.getStatus());
        verify(kardexQueryRepositoryPort).existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        verify(productRepository, never()).getReferenceByProductId(any());
    }
    
    @Test
    @DisplayName("Should throw error when product does not exist")
    void testValidateBusinessRulesAndGetProductWithNonExistentProduct() {
        // Arrange
        when(kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType))
            .thenReturn(false);
        when(productRepository.getReferenceByProductId(productId)).thenReturn(null);
        when(messageService.getMessage(anyString(), any(Object[].class))).thenReturn("Product not found");
        
        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            kardexValidationService.validateBusinessRulesAndGetProduct(factCode, productId, movementType);
        });
        
        assertEquals(404, exception.getStatus());
        verify(kardexQueryRepositoryPort).existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        verify(productRepository).getReferenceByProductId(productId);
        verify(productMapper, never()).toDomain(any());
    }
    
    @Test
    @DisplayName("Should throw error when product is inactive")
    void testValidateBusinessRulesAndGetProductWithInactiveProduct() {
        // Arrange
        productEntity.setState(false);
        product.setState(false);
        
        when(kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType))
            .thenReturn(false);
        when(productRepository.getReferenceByProductId(productId)).thenReturn(productEntity);
        when(productMapper.toDomain(productEntity)).thenReturn(product);
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Product is not active");
        
        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            kardexValidationService.validateBusinessRulesAndGetProduct(factCode, productId, movementType);
        });
        
        assertEquals(400, exception.getStatus());
        verify(kardexQueryRepositoryPort).existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        verify(productRepository).getReferenceByProductId(productId);
        verify(productMapper).toDomain(productEntity);
    }
    
    @Test
    @DisplayName("Should validate previous kardex exists successfully")
    void testValidatePreviousKardexExistsSuccess() {
        // Act
        kardexValidationService.validatePreviousKardexExists(lastKardex, "sale");
        
        // Assert - No exception should be thrown
    }
    
    @Test
    @DisplayName("Should throw error when previous kardex does not exist")
    void testValidatePreviousKardexExistsWithNull() {
        // Arrange
        String operation = "sale";
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Missing record error");
        
        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            kardexValidationService.validatePreviousKardexExists(null, operation);
        });
        
        assertEquals(404, exception.getStatus());
        verify(messageService).getMessage(anyString(), eq(operation));
    }
}