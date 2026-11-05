package co.unicauca.edu.co.contables.inventory.peps.unit.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Balance;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexReport;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.SaleDetail;

public class KardexReportUnitTest {
     private Product product;
    private Kardex purchaseMovement;
    private Kardex saleMovement;
    private List<Balance> balanceList;
    private List<SaleDetail> saleDetailsList;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setProductId(1L);
        product.setName("Mango");
        product.setState(true);


        purchaseMovement = Kardex.createPurchase(
            "1001",
            "Purchase details",
            100,
            new BigDecimal("10.00"),
            product
        );

        purchaseMovement.setIdKardex(1L);
        purchaseMovement.setDate(ZonedDateTime.now().minusDays(1));

        saleMovement = Kardex.createSale(
            "2001",
            "Sale details",
            50,
            new BigDecimal("10.00"),
            product
        );
        saleMovement.setIdKardex(2L);
        saleMovement.setDate(ZonedDateTime.now());

        balanceList = new ArrayList<>();
        balanceList.add(Balance.fromMovement(50, new BigDecimal("10.00")));
        balanceList.add(Balance.fromMovement(30, new BigDecimal("12.00")));


        saleDetailsList = new ArrayList<>();
        saleDetailsList.add(SaleDetail.fromBalance(
            Balance.fromMovement(30, new BigDecimal("10.00")), 30));
        saleDetailsList.add(SaleDetail.fromBalance(
            Balance.fromMovement(20, new BigDecimal("12.00")), 20));

    }


    @Test
    @DisplayName("create entry report successfully")
    void testCreateEntryReport_ValidMovement() {
        // Act
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Assert
        assertAll("Entry report validation",
            () -> assertEquals(1L, report.getIdKardex()),
            () -> assertEquals(purchaseMovement.getDate(), report.getDate()),
            () -> assertEquals("Purchase details", report.getDetail()),
            () -> assertEquals(100, report.getEntryQuantity()),
            () -> assertEquals(0, new BigDecimal("10.00").compareTo(report.getEntryUnitPrice())),
            () -> assertEquals(0, new BigDecimal("1000.00").compareTo(report.getEntryTotalPrice())),
            () -> assertEquals(2, report.getBalance().size())
        );
    }

    @Test
    @DisplayName("calculate entry total price correctly-success")
    void testCreateEntryReport_CalculatesTotalPrice() {
        // Arrange
        Kardex movement = Kardex.createPurchase("1002", "Purchase", 25, new BigDecimal("15.50"), product);
        movement.setDate(ZonedDateTime.now());
        BigDecimal expectedTotal = new BigDecimal("387.50");
        
        // Act
        KardexReport report = KardexReport.createEntryReport(movement, balanceList);
        
        // Assert
        assertEquals(0, expectedTotal.compareTo(report.getEntryTotalPrice()));
    }
    
    @Test
    @DisplayName("throw exception when movement is null")
    void testCreateEntryReport_NullMovement() {
        // Act y Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> KardexReport.createEntryReport(null, balanceList)
        );
        
        assertEquals("Movement cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when movement date is null")
    void testCreateEntryReport_NullDate() {
        // Arrange
        purchaseMovement.setDate(null);
        
        // Act y Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> KardexReport.createEntryReport(purchaseMovement, balanceList)
        );
        
        assertEquals("Movement date cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("handle empty balance list")
    void testCreateEntryReport_EmptyBalanceList() {
        // Arrange
        List<Balance> emptyList = new ArrayList<>();
        
        // Act
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, emptyList);
        
        // Assert
        assertNotNull(report.getBalance());
        assertTrue(report.getBalance().isEmpty());
    }
    

    @Test
    @DisplayName("create output report successfully")
    void testCreateOutputReport_ValidParameters() {
        // Act
        KardexReport report = KardexReport.createOutputReport(saleMovement, saleDetailsList, balanceList);
        
        // Assert
        assertAll("Output report validation",
            () -> assertEquals(2L, report.getIdKardex()),
            () -> assertEquals(saleMovement.getDate(), report.getDate()),
            () -> assertEquals("Sale details", report.getDetail()),
            () -> assertEquals(2, report.getOutputDetails().size()),
            () -> assertEquals(2, report.getBalance().size()),
            () -> assertNull(report.getEntryQuantity())
        );
    }
    
    @Test
    @DisplayName("copy sale details list")
    void testCreateOutputReport_CopiesSaleDetails_IndependentCopy() {
        // Act
        KardexReport report = KardexReport.createOutputReport(saleMovement, saleDetailsList, balanceList);
        
        // Modify original list
        saleDetailsList.clear();
        
        // Assert
        assertEquals(2, report.getOutputDetails().size());
    }
    
    @Test
    @DisplayName("create initial balance report")
    void testCreateInitialBalanceReport_ValidParameters() {
        // Arrange
        ZonedDateTime date = ZonedDateTime.now();
        
        // Act
        KardexReport report = KardexReport.createInitialBalanceReport(date, balanceList);
        
        // Assert
        assertAll("Initial balance report validation",
            () -> assertEquals(date, report.getDate()),
            () -> assertEquals("SALDO INICIAL", report.getDetail()),
            () -> assertEquals(2, report.getBalance().size()),
            () -> assertNull(report.getIdKardex()),
            () -> assertTrue(report.isInitialBalance())
        );
    }
    

    @Test
    @DisplayName("set entry data successfully")
    void testSetEntryData_ValidParameters_SetsData() {
        // Arrange
        KardexReport report = new KardexReport();
        int quantity = 50;
        BigDecimal unitPrice = new BigDecimal("20.00");
        
        // Act
        report.setEntryData(quantity, unitPrice);
        
        // Assert
        assertAll("Entry data validation",
            () -> assertEquals(50, report.getEntryQuantity()),
            () -> assertEquals(0, unitPrice.compareTo(report.getEntryUnitPrice())),
            () -> assertEquals(0, new BigDecimal("1000.00").compareTo(report.getEntryTotalPrice()))
        );
    }
    
    @Test
    @DisplayName("throw exception when entry quantity is zero")
    void testSetEntryData_ZeroQuantity() {
        // Arrange
        KardexReport report = new KardexReport();
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> report.setEntryData(0, new BigDecimal("10.00"))
        );
        
        assertEquals("Entry quantity must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when entry quantity is negative")
    void testSetEntryData_NegativeQuantity() {
        // Arrange
        KardexReport report = new KardexReport();
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> report.setEntryData(-10, new BigDecimal("10.00"))
        );
        
        assertEquals("Entry quantity must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when unit price is null")
    void testSetEntryData_NullUnitPrice() {
        // Arrange
        KardexReport report = new KardexReport();
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> report.setEntryData(50, null)
        );
        
        assertEquals("Unit price cannot be negative", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when unit price is negative")
    void testSetEntryData_NegativeUnitPrice() {
        // Arrange
        KardexReport report = new KardexReport();
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> report.setEntryData(50, new BigDecimal("-5.00"))
        );
        
        assertEquals("Unit price cannot be negative", exception.getMessage());
    }
    

    @Test
    @DisplayName("set output data successfully")
    void testSetOutputData_ValidDetails_SetsData() {
        // Arrange
        KardexReport report = new KardexReport();
        
        // Act
        report.setOutputData(saleDetailsList);
        
        // Assert
        assertEquals(2, report.getOutputDetails().size());
    }
    
    @Test
    @DisplayName("throw exception when output details is null")
    void testSetOutputData_NullDetails() {
        // Arrange
        KardexReport report = new KardexReport();
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> report.setOutputData(null)
        );
        
        assertEquals("Output details cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when output details is empty")
    void testSetOutputData_EmptyDetails() {
        // Arrange
        KardexReport report = new KardexReport();
        List<SaleDetail> emptyList = new ArrayList<>();
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> report.setOutputData(emptyList)
        );
        
        assertEquals("Output details cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("update balance successfully")
    void testUpdateBalance_ValidBalance_UpdatesData() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        List<Balance> newBalance = new ArrayList<>();
        newBalance.add(Balance.fromMovement(100, new BigDecimal("15.00")));
        
        // Act
        report.updateBalance(newBalance);
        
        // Assert
        assertEquals(1, report.getBalance().size());
        assertEquals(100, report.getTotalBalanceQuantity());
    }
    
   
    @Test
    @DisplayName("return true when is entry-return true")
    void testIsEntry_WithEntryQuantity() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act y Assert
        assertTrue(report.isEntry());
    }
    
    @Test
    @DisplayName("return false when is not entry")
    void testIsEntry_WithoutEntryQuantity() {
        // Arrange
        KardexReport report = KardexReport.createOutputReport(saleMovement, saleDetailsList, balanceList);
        
        // Act y Assert
        assertFalse(report.isEntry());
    }
    
    @Test
    @DisplayName("return true when is output")
    void testIsOutput_WithOutputDetails() {
        // Arrange
        KardexReport report = KardexReport.createOutputReport(saleMovement, saleDetailsList, balanceList);
        
        // Act y Assert
        assertTrue(report.isOutput());
    }
    
    @Test
    @DisplayName("return false when is not output")
    void testIsOutput_WithoutOutputDetails() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act y Assert
        assertFalse(report.isOutput());
    }
    
  
    @Test
    @DisplayName("return true for initial balance")
    void testIsInitialBalance_InitialBalanceReport() {
        // Arrange
        KardexReport report = KardexReport.createInitialBalanceReport(ZonedDateTime.now(), balanceList);
        
        // Act y Assert
        assertTrue(report.isInitialBalance());
    }
    
    @Test
    @DisplayName("return false when not initial balance")
    void testIsInitialBalance_EntryReport() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act y Assert
        assertFalse(report.isInitialBalance());
    }
    
  
    @Test
    @DisplayName("calculate output quantity correctly")
    void testGetOutputQuantity_WithDetails_ReturnsCorrectSum() {
        // Arrange
        KardexReport report = KardexReport.createOutputReport(saleMovement, saleDetailsList, balanceList);
        
        // Act
        int quantity = report.getOutputQuantity();
        
        // Assert
        assertEquals(50, quantity); // 30 + 20
    }
    
    @Test
    @DisplayName("return zero when no output details")
    void testGetOutputQuantity_NoDetails() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act & Assert
        assertEquals(0, report.getOutputQuantity());
    }
    
    @Test
    @DisplayName("calculate output total price correctly")
    void testGetOutputTotalPrice_WithDetails_ReturnsCorrectSum() {
        // Arrange
        KardexReport report = KardexReport.createOutputReport(saleMovement, saleDetailsList, balanceList);
        BigDecimal expectedTotal = new BigDecimal("540.00"); 
        
        // Act
        BigDecimal totalPrice = report.getOutputTotalPrice();
        
        // Assert
        assertEquals(0, expectedTotal.compareTo(totalPrice));
    }
    
    @Test
    @DisplayName("return zero when no output details")
    void testGetOutputTotalPrice_NoDetails() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act y Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getOutputTotalPrice()));
    }
    
   
    @Test
    @DisplayName("calculate average output price correctly")
    void testGetAverageOutputPrice_WithDetails() {
        // Arrange
        KardexReport report = KardexReport.createOutputReport(saleMovement, saleDetailsList, balanceList);
        BigDecimal expectedAverage = new BigDecimal("10.80"); 
        
        // Act
        BigDecimal averagePrice = report.getAverageOutputPrice();
        
        // Assert
        assertEquals(0, expectedAverage.compareTo(averagePrice));
    }
    
    @Test
    @DisplayName("return zero when no output quantity")
    void testGetAverageOutputPrice_NoQuantity() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act y Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getAverageOutputPrice()));
    }
    
    
    @Test
    @DisplayName("calculate total balance quantity correctly")
    void testGetTotalBalanceQuantity_WithBalances() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act
        int totalQuantity = report.getTotalBalanceQuantity();
        
        // Assert
        assertEquals(80, totalQuantity); // 50 + 30
    }
    
    @Test
    @DisplayName("return zero when no balances")
    void testGetTotalBalanceQuantity_NoBalances() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, new ArrayList<>());
        
        // Act y Assert
        assertEquals(0, report.getTotalBalanceQuantity());
    }
    

    @Test
    @DisplayName("calculate total balance value correctly")
    void testGetTotalBalanceValue_WithBalances() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        BigDecimal expectedTotal = new BigDecimal("860.00"); 
        
        // Act
        BigDecimal totalValue = report.getTotalBalanceValue();
        
        // Assert
        assertEquals(0, expectedTotal.compareTo(totalValue));
    }
    
    @Test
    @DisplayName("return zero when no balances")
    void testGetTotalBalanceValue_NoBalances() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, new ArrayList<>());
        
        // Act y Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalBalanceValue()));
    }
    

    @Test
    @DisplayName("calculate average balance price correctly")
    void testGetAverageBalancePrice_WithBalances() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        BigDecimal expectedAverage = new BigDecimal("10.75"); 
        
        // Act
        BigDecimal averagePrice = report.getAverageBalancePrice();
        
        // Assert
        assertEquals(0, expectedAverage.compareTo(averagePrice));
    }
    
    @Test
    @DisplayName("return zero when no balance quantity")
    void testGetAverageBalancePrice_NoQuantity() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, new ArrayList<>());
        
        // Act y Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getAverageBalancePrice()));
    }
    

    @Test
    @DisplayName("return true when has balance")
    void testHasBalance_WithBalances() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act y Assert
        assertTrue(report.hasBalance());
    }
    
    @Test
    @DisplayName("return false when no balances")
    void testHasBalance_NoBalances() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, new ArrayList<>());
        
        // Act y Assert
        assertFalse(report.hasBalance());
    }
    
    @Test
    @DisplayName("return false when balance list is null")
    void testHasBalance_NullBalanceList() {
        // Arrange
        KardexReport report = new KardexReport();
        report.setBalance(null);
        
        // Act y Assert
        assertFalse(report.hasBalance());
    }
    

    @Test
    @DisplayName("return unmodifiable output details list")
    void testGetOutputDetailsReadOnly_WithDetails_ReturnsUnmodifiableList() {
        // Arrange
        KardexReport report = KardexReport.createOutputReport(saleMovement, saleDetailsList, balanceList);
        
        // Act
        List<SaleDetail> readOnly = report.getOutputDetailsReadOnly();
        
        // Assert
        assertEquals(2, readOnly.size());
        assertThrows(UnsupportedOperationException.class,
            () -> readOnly.add(SaleDetail.fromBalance(Balance.fromMovement(10, new BigDecimal("10.00")), 10)));
    }
    
    @Test
    @DisplayName("return empty list when no output details")
    void testGetOutputDetailsReadOnly_NoDetails_ReturnsEmptyList() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act
        List<SaleDetail> readOnly = report.getOutputDetailsReadOnly();
        
        // Assert
        assertNotNull(readOnly);
        assertTrue(readOnly.isEmpty());
    }
    

    @Test
    @DisplayName("return unmodifiable balance list")
    void testGetBalanceReadOnly_WithBalances_ReturnsUnmodifiableList() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act
        List<Balance> readOnly = report.getBalanceReadOnly();
        
        // Assert
        assertEquals(2, readOnly.size());
        assertThrows(UnsupportedOperationException.class,
            () -> readOnly.add(Balance.fromMovement(10, new BigDecimal("10.00"))));
    }
    
    @Test
    @DisplayName("return empty list when no balances")
    void testGetBalanceReadOnly_NoBalances_ReturnsEmptyList() {
        // Arrange
        KardexReport report = new KardexReport();
        report.setBalance(null);
        
        // Act
        List<Balance> readOnly = report.getBalanceReadOnly();
        
        // Assert
        assertNotNull(readOnly);
        assertTrue(readOnly.isEmpty());
    }
    

    @Test
    @DisplayName("be equal when same instance")
    void testEquals_SameInstance() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act & Assert
        assertEquals(report, report);
    }
    
    @Test
    @DisplayName("be equal when same ID and date")
    void testEquals_SameIdAndDate() {
        // Arrange
        ZonedDateTime sameDate = ZonedDateTime.now();
        KardexReport report1 = KardexReport.builder()
            .idKardex(1L)
            .date(sameDate)
            .detail("Report 1")
            .build();
        
        KardexReport report2 = KardexReport.builder()
            .idKardex(1L)
            .date(sameDate)
            .detail("Report 2")
            .build();
        
        // Act y Assert
        assertEquals(report1, report2);
    }
    
    @Test
    @DisplayName("not be equal when different IDs-return false")
    void testEquals_DifferentIds() {
        // Arrange
        ZonedDateTime sameDate = ZonedDateTime.now();
        KardexReport report1 = KardexReport.builder()
            .idKardex(1L)
            .date(sameDate)
            .build();
        
        KardexReport report2 = KardexReport.builder()
            .idKardex(2L)
            .date(sameDate)
            .build();
        
        // Act y Assert
        assertNotEquals(report1, report2);
    }
    
    @Test
    @DisplayName("not be equal to null-return false")
    void testEquals_NullObject() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act y Assert
        assertNotEquals(report, null);
    }
    

    @Test
    @DisplayName("return same hashCode for equal objects")
    void testHashCode_EqualObjects_ReturnsSameHash() {
        // Arrange
        ZonedDateTime sameDate = ZonedDateTime.now();
        KardexReport report1 = KardexReport.builder()
            .idKardex(1L)
            .date(sameDate)
            .build();
        
        KardexReport report2 = KardexReport.builder()
            .idKardex(1L)
            .date(sameDate)
            .build();
        
        // Act y Assert
        assertEquals(report1.hashCode(), report2.hashCode());
    }
    
    @Test
    @DisplayName("return consistent hashCode")
    void testHashCode_MultipleInvocations() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act
        int hash1 = report.hashCode();
        int hash2 = report.hashCode();
        int hash3 = report.hashCode();
        
        // Assert
        assertAll("HashCode consistency",
            () -> assertEquals(hash1, hash2),
            () -> assertEquals(hash2, hash3)
        );
    }
    
    @Test
    @DisplayName("return formatted string with all fields")
    void testToString_ValidReport_ReturnsFormattedString() {
        // Arrange
        KardexReport report = KardexReport.createEntryReport(purchaseMovement, balanceList);
        
        // Act
        String result = report.toString();
        
        // Assert
        assertAll("ToString validation",
            () -> assertTrue(result.contains("id=1")),
            () -> assertTrue(result.contains("detail='Purchase details'")),
            () -> assertTrue(result.contains("balanceQty=80")),
            () -> assertTrue(result.contains("balanceValue=")),
            () -> assertTrue(result.startsWith("KardexReport{"))
        );
    }
    

    @Test
    @DisplayName("create report using builder")
    void testBuilder_ValidValues() {
        // Arrange y Act
        KardexReport report = KardexReport.builder()
            .idKardex(10L)
            .date(ZonedDateTime.now())
            .detail("Test detail")
            .entryQuantity(50)
            .entryUnitPrice(new BigDecimal("20.00"))
            .entryTotalPrice(new BigDecimal("1000.00"))
            .balance(balanceList)
            .build();
        
        // Assert
        assertAll("Builder validation",
            () -> assertEquals(10L, report.getIdKardex()),
            () -> assertEquals("Test detail", report.getDetail()),
            () -> assertEquals(50, report.getEntryQuantity()),
            () -> assertEquals(0, new BigDecimal("20.00").compareTo(report.getEntryUnitPrice()))
        );
    }
    
}
