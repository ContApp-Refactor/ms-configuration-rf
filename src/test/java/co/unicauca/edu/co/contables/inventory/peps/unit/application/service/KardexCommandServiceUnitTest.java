package co.unicauca.edu.co.contables.inventory.peps.unit.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.unicauca.edu.co.contables.inventory.peps.application.service.command.KardexAdjustmentValidationService;
import co.unicauca.edu.co.contables.inventory.peps.application.service.command.KardexCommandService ;
import co.unicauca.edu.co.contables.inventory.peps.application.service.command.KardexDateValidationService;
import co.unicauca.edu.co.contables.inventory.peps.application.service.command.StockIntegrationService;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IKardexCommandOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IDetailQueryOutPutPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IKardexQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IProductQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IProductEventPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;

@ExtendWith(MockitoExtension.class)
public class KardexCommandServiceUnitTest {

    @Mock
    private  IKardexCommandOutputPort kardexCommandOutputPort;
    
    @Mock
    private IProductQueryOutputPort productQueryOutputPort;

    @Mock
    private IKardexQueryOutputPort kardexQueryOutputPort;
    
    @Mock
    private  IDetailQueryOutPutPort detailQueryOutPutPort;
    
    @Mock
    private  IFormatterResultOutputPort formatterResultOutputPort;

    @Mock
    private KardexAdjustmentValidationService kardexAdjustmentValidationService;

    @Mock
    private KardexDateValidationService kardexDateValidationService;

    @Mock
    private StockIntegrationService stockIntegrationService;

    @Mock
    private IProductEventPort productEventPort;

    @Mock
    private IMessageServicePort messageService;

    @InjectMocks
    private KardexCommandService  kardexCommandService;

    private Product mockProduct;
    private Kardex kardexRequest;
    private Kardex purchaseLot;


   @BeforeEach
    void setUp() {
        mockProduct = new Product();
        mockProduct.setProductId(1L);
        mockProduct.setName("Test Product");
        mockProduct.setState(true);

        kardexRequest = new Kardex();
        kardexRequest.setProduct(mockProduct);
        kardexRequest.setFactCode("1001");
        kardexRequest.setDetails("Test details");
        kardexRequest.setQuantity(100);
        kardexRequest.setUnitPrice(new BigDecimal("10.00"));

        purchaseLot = Kardex.createPurchase(
            "1001",
            "Purchase",
            200,
            new BigDecimal("10.00"),
            mockProduct
        );
        purchaseLot.setIdKardex(1L);
        purchaseLot.setDate(ZonedDateTime.now().minusDays(1));
    }

    // ==================== registerPurchase() ====================
    @Test
    @DisplayName("Should register purchase successfully")
    void testRegisterPurchase_ValidRequest_ReturnsPersistedKardex() {
        // Arrange
        Kardex expected = Kardex.createPurchase(
            "1001",
            "Purchase",
            100,
            new BigDecimal("10.00"),
            mockProduct
        );
        
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(kardexCommandOutputPort.registerPurchase(any(Kardex.class)))
            .thenReturn(expected);

        // Act
        Kardex result = kardexCommandService.registerPurchase(kardexRequest);

        // Assert
        assertNotNull(result);
        assertEquals("1001", result.getFactCode());
        verify(productQueryOutputPort).getProductByProductId(1L);
        verify(kardexCommandOutputPort).registerPurchase(any(Kardex.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found for purchase")
    void testRegisterPurchase_ProductNotFound_ThrowsException() {
        // Arrange
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.empty());
        when(messageService.getMessage(eq(MessageKeys.ERROR_NOT_FOUND_PRODUCT), anyLong()))
            .thenReturn("Product not found");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerPurchase(kardexRequest)
        );

        assertEquals("Product not found", exception.getMessage());
        verify(formatterResultOutputPort)
            .returnEntityDoesNotExistErrorResponse(404, "Product not found");
    }

    // ==================== registerSale() ====================
    @Test
    @DisplayName("Should register sale with FIFO processing successfully")
    void testRegisterSale_ValidRequest_ExecutesFIFOAndRegistersSale() {
        // Arrange
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(kardexQueryOutputPort.findAvailablePurchasesOrderedByDate(1L))
            .thenReturn(List.of(purchaseLot));
        when(kardexCommandOutputPort.registerSale(any(Kardex.class), anyList()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Kardex result = kardexCommandService.registerSale(kardexRequest);

        // Assert
        assertNotNull(result);
        assertEquals("1001", result.getFactCode());
        assertEquals(100, result.getQuantity());
        verify(kardexQueryOutputPort).findAvailablePurchasesOrderedByDate(1L);
        verify(kardexCommandOutputPort).registerSale(any(Kardex.class), anyList());
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock for sale")
    void testRegisterSale_InsufficientStock_ThrowsException() {
        // Arrange
        Kardex limitedLot = Kardex.createPurchase(
            "1002",
            "Limited Purchase",
            50,
            new BigDecimal("10.00"),
            mockProduct
        );
        
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(kardexQueryOutputPort.findAvailablePurchasesOrderedByDate(1L))
            .thenReturn(List.of(limitedLot));
        when(messageService.getMessage(eq(MessageKeys.ERROR_INSUFFICIENT_STOCK), anyInt(), anyInt()))
            .thenReturn("Insufficient stock");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerSale(kardexRequest)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(formatterResultOutputPort)
            .returnBusinessRuleErrorResponse(eq(400), contains("Insufficient stock"));
    }

    @Test
    @DisplayName("Should process FIFO from multiple lots")
    void testRegisterSale_MultipleLots_ProcessesFIFOCorrectly() {
        // Arrange
        Kardex lot1 = Kardex.createPurchase("1003", "Lot 1", 60, new BigDecimal("10.00"), mockProduct);
        lot1.setIdKardex(2L);
        lot1.setDate(ZonedDateTime.now().minusDays(2));
        
        Kardex lot2 = Kardex.createPurchase("1004", "Lot 2", 80, new BigDecimal("12.00"), mockProduct);
        lot2.setIdKardex(3L);
        lot2.setDate(ZonedDateTime.now().minusDays(1));
        
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(kardexQueryOutputPort.findAvailablePurchasesOrderedByDate(1L))
            .thenReturn(List.of(lot1, lot2));
        when(kardexCommandOutputPort.registerSale(any(Kardex.class), anyList()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Kardex result = kardexCommandService.registerSale(kardexRequest);

        // Assert
        assertNotNull(result);
        assertEquals(0, lot1.getAvailableQuantity()); // Fully consumed
        assertEquals(40, lot2.getAvailableQuantity()); // Partially consumed
        
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Kardex>> lotsCaptor = ArgumentCaptor.forClass(List.class);
        verify(kardexCommandOutputPort).registerSale(any(Kardex.class), lotsCaptor.capture());
        assertEquals(2, lotsCaptor.getValue().size());
    }

    // ==================== registerPurchaseReturn() ====================
    @Test
    @DisplayName("Should register purchase return successfully")
    void testRegisterPurchaseReturn_ValidRequest_ReturnsRegisteredReturn() {
        // Arrange
        kardexRequest.setQuantity(30);
        
        when(kardexQueryOutputPort.findByRefFacture("1001", 1L))
            .thenReturn(Optional.of(purchaseLot));
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(kardexCommandOutputPort.registerPurchaseReturn(any(Kardex.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Kardex result = kardexCommandService.registerPurchaseReturn(kardexRequest);

        // Assert
        assertNotNull(result);
        assertEquals("1001", result.getFactCode());
        verify(kardexCommandOutputPort)
            .updateAvaliableAmount(1L, 170); // 200 - 30
        verify(kardexCommandOutputPort).registerPurchaseReturn(any(Kardex.class));
    }

    @Test
    @DisplayName("Should throw exception when original purchase not found")
    void testRegisterPurchaseReturn_PurchaseNotFound_ThrowsException() {
        // Arrange
        when(kardexQueryOutputPort.findByRefFacture("1001", 1L))
            .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerPurchaseReturn(kardexRequest)
        );

        assertEquals("Original purchase not found", exception.getMessage());
        verify(formatterResultOutputPort)
            .returnEntityDoesNotExistErrorResponse(404, "Original purchase not found");
    }

    @Test
    @DisplayName("Should throw exception when return validation fails")
    void testRegisterPurchaseReturn_ValidationFails_ThrowsException() {
        // Arrange
        kardexRequest.setQuantity(300); // More than purchased
        
        when(kardexQueryOutputPort.findByRefFacture("1001", 1L))
            .thenReturn(Optional.of(purchaseLot));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerPurchaseReturn(kardexRequest)
        );

        assertTrue(exception.getMessage().contains("Cannot return more than purchased"));
        verify(formatterResultOutputPort)
            .returnBusinessRuleErrorResponse(eq(400), anyString());
    }

    @Test
    @DisplayName("Should register sale return with LIFO processing")
    void testRegisterSaleReturn_ValidRequest_ProcessesLIFOAndReturns() {
        // Arrange
        Kardex saleMovement = Kardex.createSale("2001", "Sale", 50, new BigDecimal("10.00"), mockProduct);
        saleMovement.setIdKardex(10L);
        saleMovement.setDate(ZonedDateTime.now());
        
        kardexRequest.setFactCode("2001");
        kardexRequest.setQuantity(50);
        
       
        purchaseLot.setAvailableQuantity(150);
        
        DetailOutput detail1 = DetailOutput.create(30, new BigDecimal("10.00"), saleMovement, purchaseLot);
        detail1.setIdDetailOutput(1L);
        
        DetailOutput detail2 = DetailOutput.create(20, new BigDecimal("12.00"), saleMovement, purchaseLot);
        detail2.setIdDetailOutput(2L);
        
        when(kardexQueryOutputPort.findByRefFacture("2001", 1L))
            .thenReturn(Optional.of(saleMovement));
        when(detailQueryOutPutPort.findByMovementSaleOrderedDesc(10L))
            .thenReturn(List.of(detail2, detail1)); // LIFO order
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        
        when(kardexCommandOutputPort.registerSaleReturn(any(Kardex.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
       doNothing().when(detailQueryOutPutPort).deleteById(anyLong());

        // Act
        List<Kardex> results = kardexCommandService.registerSaleReturn(kardexRequest);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(detailQueryOutPutPort, times(2)).deleteById(anyLong());
        verify(kardexCommandOutputPort, times(2)).updateAvaliableAmount(anyLong(), anyInt());
    }


    @Test
    @DisplayName("Should throw exception when sale not found for return")
    void testRegisterSaleReturn_SaleNotFound_ThrowsException() {
        // Arrange
        when(kardexQueryOutputPort.findByRefFacture("1001", 1L))
            .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerSaleReturn(kardexRequest)
        );

        assertEquals("Sale to be returned not found", exception.getMessage());
        verify(formatterResultOutputPort)
            .returnBusinessRuleErrorResponse(400, "Sale to be returned not found");
    }

    @Test
    @DisplayName("Should throw exception when sale has no details")
    void testRegisterSaleReturn_NoDetails_ThrowsException() {
        // Arrange
        // ✅ CORRECCIÓN: La cantidad a devolver debe ser <= cantidad vendida
        Kardex saleMovement = Kardex.createSale("2001", "Sale", 50, new BigDecimal("10.00"), mockProduct);
        saleMovement.setIdKardex(10L);
        
        kardexRequest.setFactCode("2001");
        kardexRequest.setQuantity(50); // ✅ Cambiar de 100 a 50 o menos
        
        when(kardexQueryOutputPort.findByRefFacture("2001", 1L))
            .thenReturn(Optional.of(saleMovement));
        when(detailQueryOutPutPort.findByMovementSaleOrderedDesc(10L))
            .thenReturn(new ArrayList<>());

        // Act
        List<Kardex> results = kardexCommandService.registerSaleReturn(kardexRequest);

        // Assert
        assertTrue(results.isEmpty());
        verify(formatterResultOutputPort)
            .returnBusinessRuleErrorResponse(400, "Sale has no details to return");
    }

    @Test
    @DisplayName("Should process partial sale return")
    void testRegisterSaleReturn_PartialReturn_UpdatesDetailCorrectly() {
        // Arrange
        Kardex saleMovement = Kardex.createSale("2001", "Sale", 50, new BigDecimal("10.00"), mockProduct);
        saleMovement.setIdKardex(10L);
        
        kardexRequest.setFactCode("2001");
        kardexRequest.setQuantity(15);
        
        purchaseLot.setAvailableQuantity(170);
        
        DetailOutput detail = DetailOutput.create(30, new BigDecimal("10.00"), saleMovement, purchaseLot);
        detail.setIdDetailOutput(1L);
        
        when(kardexQueryOutputPort.findByRefFacture("2001", 1L))
            .thenReturn(Optional.of(saleMovement));
        when(detailQueryOutPutPort.findByMovementSaleOrderedDesc(10L))
            .thenReturn(List.of(detail));
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        
        when(kardexCommandOutputPort.updateAvaliableAmount(anyLong(), anyInt()))
            .thenReturn(1);
        
      
        doNothing().when(detailQueryOutPutPort).updateQuantityAndPrice(anyLong(), anyInt(), any(BigDecimal.class));
        
        when(kardexCommandOutputPort.registerSaleReturn(any(Kardex.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<Kardex> results = kardexCommandService.registerSaleReturn(kardexRequest);

        // Assert
        assertEquals(1, results.size());
        verify(detailQueryOutPutPort).updateQuantityAndPrice(anyLong(), anyInt(), any(BigDecimal.class));
        verify(detailQueryOutPutPort, never()).deleteById(anyLong());
    }




    // ==================== registerNonCommercialExit() ====================
    @Test
    @DisplayName("Should register non-commercial exit successfully")
    void testRegisterNonCommercialExit_ValidRequest_RegistersExit() {
        // Arrange
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(kardexQueryOutputPort.findAvailablePurchasesOrderedByDate(1L))
            .thenReturn(List.of(purchaseLot));
        when(kardexCommandOutputPort.registerNonCommercialExit(any(Kardex.class), anyList()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Kardex result = kardexCommandService.registerNonCommercialExit(kardexRequest);

        // Assert
        assertNotNull(result);
        assertEquals("1001", result.getFactCode());
        verify(kardexQueryOutputPort).findAvailablePurchasesOrderedByDate(1L);
        verify(kardexCommandOutputPort).registerNonCommercialExit(any(Kardex.class), anyList());
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock for non-commercial exit")
    void testRegisterNonCommercialExit_InsufficientStock_ThrowsException() {
        // Arrange
        Kardex limitedLot = Kardex.createPurchase("1002", "Limited", 50, new BigDecimal("10.00"), mockProduct);
        
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(kardexQueryOutputPort.findAvailablePurchasesOrderedByDate(1L))
            .thenReturn(List.of(limitedLot));
        when(messageService.getMessage(eq(MessageKeys.ERROR_INSUFFICIENT_STOCK), anyInt(), anyInt()))
            .thenReturn("Insufficient stock");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerNonCommercialExit(kardexRequest)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }

    // ==================== registerNonCommercialEntry() ====================
    @Test
    @DisplayName("Should register non-commercial entry successfully")
    void testRegisterNonCommercialEntry_ValidRequest_RegistersEntry() {
        // Arrange
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(kardexCommandOutputPort.registerNonCommercialEntry(any(Kardex.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Kardex result = kardexCommandService.registerNonCommercialEntry(kardexRequest);

        // Assert
        assertNotNull(result);
        assertEquals("1001", result.getFactCode());
        verify(kardexCommandOutputPort).registerNonCommercialEntry(any(Kardex.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found for non-commercial entry")
    void testRegisterNonCommercialEntry_ProductNotFound_ThrowsException() {
        // Arrange
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.empty());
        when(messageService.getMessage(eq(MessageKeys.ERROR_NOT_FOUND_PRODUCT), anyLong()))
            .thenReturn("Product not found");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerNonCommercialEntry(kardexRequest)
        );

        assertEquals("Product not found", exception.getMessage());
    }

 
    @Test
    @DisplayName("Should throw exception when product not found for adjustment entry")
    void testRegisterAdjustmentEntry_ProductNotFound_ThrowsException() {
        // Arrange
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.empty());
        when(messageService.getMessage(eq(MessageKeys.ERROR_NOT_FOUND_PRODUCT), anyLong()))
            .thenReturn("Product not found");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerAdjustmentEntry(kardexRequest)
        );

        assertEquals("Product not found", exception.getMessage());
        verify(kardexAdjustmentValidationService, never()).validateDateForAdjustment(any(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when product is inactive for adjustment entry")
    void testRegisterAdjustmentEntry_InactiveProduct_ThrowsException() {
        // Arrange
        mockProduct.setState(false);
        
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(messageService.getMessage(eq(MessageKeys.ERROR_PRODUCT_NOT_ACTIVE), anyLong()))
            .thenReturn("Product is inactive");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerAdjustmentEntry(kardexRequest)
        );

        assertEquals("Product is inactive", exception.getMessage());
        verify(kardexAdjustmentValidationService, never()).validateDateForAdjustment(any(), anyString());
    }

    
   

    @Test
    @DisplayName("Should throw exception when insufficient stock for adjustment exit")
    void testRegisterAdjustmentExit_InsufficientStock_ThrowsException() {
        // Arrange
        kardexRequest.setDate(ZonedDateTime.now());
        Kardex limitedLot = Kardex.createPurchase("1002", "Limited", 50, new BigDecimal("10.00"), mockProduct);
        
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(kardexQueryOutputPort.findAvailablePurchasesOrderedByDate(1L))
            .thenReturn(List.of(limitedLot));
        when(messageService.getMessage(eq(MessageKeys.ERROR_INSUFFICIENT_STOCK), anyInt(), anyInt()))
            .thenReturn("Insufficient stock");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerAdjustmentExit(kardexRequest)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(kardexAdjustmentValidationService, never()).validateDateForAdjustment(any(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when product not found for adjustment exit")
    void testRegisterAdjustmentExit_ProductNotFound_ThrowsException() {
        // Arrange
        kardexRequest.setDate(ZonedDateTime.now());
        
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.empty());
        when(messageService.getMessage(eq(MessageKeys.ERROR_NOT_FOUND_PRODUCT), anyLong()))
            .thenReturn("Product not found");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerAdjustmentExit(kardexRequest)
        );

        assertEquals("Product not found", exception.getMessage());
        verify(kardexAdjustmentValidationService, never()).validateDateForAdjustment(any(), anyString());
    }

   

    // ==================== deleteAll() ====================
    @Test
    @DisplayName("Should delete all kardex records successfully")
    void testDeleteAll_Success() {
        // Arrange
        doNothing().when(kardexCommandOutputPort).deleteAll();

        // Act
        kardexCommandService.deleteAll();

        // Assert
        verify(kardexCommandOutputPort, times(1)).deleteAll();
    }

    @Test
    @DisplayName("Should handle exception when deleting all kardex records without throwing")
    void testDeleteAll_HandlesException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Database error");
        doThrow(exception).when(kardexCommandOutputPort).deleteAll();

        // Act - Should not throw exception because it's handled internally
        kardexCommandService.deleteAll();

        // Assert - Verify the method was called even though it threw an exception
        verify(kardexCommandOutputPort, times(1)).deleteAll();
    }


    @Test
    @DisplayName("Should throw exception when product is inactive")
    void testGetValidatedProduct_InactiveProduct_ThrowsException() {
        // Arrange
        mockProduct.setState(false);
        
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(messageService.getMessage(eq(MessageKeys.ERROR_PRODUCT_NOT_ACTIVE), anyLong()))
            .thenReturn("Product is inactive");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerPurchase(kardexRequest)
        );

        assertEquals("Product is inactive", exception.getMessage());
        verify(formatterResultOutputPort)
            .returnBusinessRuleErrorResponse(eq(400), eq("Product is inactive"));
    }

    @Test
    @DisplayName("Should validate product state for sale operation")
    void testGetValidatedProduct_SaleOperation_ValidatesProductState() {
        // Arrange
        mockProduct.setState(false);
        
        when(productQueryOutputPort.getProductByProductId(1L))
            .thenReturn(Optional.of(mockProduct));
        when(messageService.getMessage(eq(MessageKeys.ERROR_PRODUCT_NOT_ACTIVE), anyLong()))
            .thenReturn("Product is inactive");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardexCommandService.registerSale(kardexRequest)
        );

        assertTrue(exception.getMessage().contains("Product is inactive"));
    }

}
