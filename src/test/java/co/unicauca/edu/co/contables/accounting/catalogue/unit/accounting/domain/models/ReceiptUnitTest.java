package co.unicauca.edu.co.contables.accounting.catalogue.unit.accounting.domain.models;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.ProcessingStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.Receipt;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptUnitTest {

    private Receipt receipt;
    private ReceiptDetail detail1;
    private Map<String, AccountCatalogue> accountFinder;

    @BeforeEach
    void setUp() {
        detail1 = ReceiptDetail.builder()
                .id(1L)
                .invoiceCode("INV-001")
                .accountingAccount(200L)
                .amountPaid(BigDecimal.valueOf(1000)) // Coincide con totalAmount por defecto
                .build();

        receipt = Receipt.builder()
                .id(1L)
                .originalReceiptId(100L)
                .receiptCode("RC-12345")
                .enterpriseId("ENT-001")
                .receiptTypeId(1L)
                .thirdPartyId(50L)
                .paymentMethodId(1L)
                .paymentMethodAccount(100L)
                .status("ACTIVE")
                .issueDate(LocalDate.of(2024, 1, 15))
                .totalAmount(BigDecimal.valueOf(1000))
                .observations("Recibo de prueba")
                .ledgerAccountId(200L)
                .centerCostId(1L)
                .details(Arrays.asList(detail1))
                .processingStatus(ProcessingStatus.PENDING)
                .build();

        // Preparar finder de cuentas
        accountFinder = new HashMap<>();
        accountFinder.put("100", AccountCatalogue.builder()
                .id(100L)
                .code("11050501")
                .description("Bancos")
                .build());
        accountFinder.put("200", AccountCatalogue.builder()
                .id(200L)
                .code("21050501")
                .description("Cuentas por cobrar")
                .build());
    }

    @Test
    @DisplayName("Debe crear recibo correctamente")
    void testCreateReceiptSuccess() {
        // Arrange & Act
        Receipt newReceipt = Receipt.builder()
                .id(1L)
                .receiptCode("RC-12345")
                .enterpriseId("ENT-001")
                .thirdPartyId(50L)
                .totalAmount(BigDecimal.valueOf(1000))
                .build();

        // Assert
        assertNotNull(newReceipt);
        assertEquals(1L, newReceipt.getId());
        assertEquals("RC-12345", newReceipt.getReceiptCode());
        assertEquals("ENT-001", newReceipt.getEnterpriseId());
        assertEquals(BigDecimal.valueOf(1000), newReceipt.getTotalAmount());
    }

    @Test
    @DisplayName("Debe recuperar información del recibo")
    void testGetReceiptProperties() {
        // Arrange & Act
        // Setup ya prepara receipt

        // Assert
        assertEquals(1L, receipt.getId());
        assertEquals(100L, receipt.getOriginalReceiptId());
        assertEquals("RC-12345", receipt.getReceiptCode());
        assertEquals("ENT-001", receipt.getEnterpriseId());
        assertEquals(50L, receipt.getThirdPartyId());
        assertEquals(BigDecimal.valueOf(1000), receipt.getTotalAmount());
    }

    @Test
    @DisplayName("Debe modificar propiedades del recibo")
    void testSetReceiptProperties() {
        // Arrange
        Receipt newReceipt = Receipt.builder().build();

        // Act
        newReceipt.setId(1L);
        newReceipt.setReceiptCode("RC-12345");
        newReceipt.setEnterpriseId("ENT-001");
        newReceipt.setTotalAmount(BigDecimal.valueOf(1000));
        newReceipt.setStatus("ACTIVE");

        // Assert
        assertEquals(1L, newReceipt.getId());
        assertEquals("RC-12345", newReceipt.getReceiptCode());
        assertEquals(BigDecimal.valueOf(1000), newReceipt.getTotalAmount());
    }

    @Test
    @DisplayName("Debe generar asiento contable para recibo tipo ABONO A FACTURA")
    void testBuildAccountingEntryFromReceiptTypeAbono() {
        // Arrange - Los detalles ya están configurados en setUp
        receipt.setReceiptTypeId(1L); // ABONO A FACTURA
        receipt.setPaymentMethodAccount(100L); // Banco
        receipt.setLedgerAccountId(200L); // Cuentas por cobrar
        // totalAmount y details ya están balanceados en setUp

        // Act
        AccountingEntry entry = receipt.buildAccountingEntryFromReceipt(
                code -> accountFinder.get(code),
                "AE-2024-001"
        );

        // Assert
        assertNotNull(entry);
        assertEquals("AE-2024-001", entry.getCode());
        assertEquals(receipt.getIssueDate(), entry.getDate());
        assertEquals(2, entry.getMovements().size());
        // Validar que la partida doble está balanceada
        BigDecimal totalDebits = entry.getMovements().stream()
                .map(m -> m.getDebit())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredits = entry.getMovements().stream()
                .map(m -> m.getCredit())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalDebits.compareTo(totalCredits));
    }

    @Test
    @DisplayName("Debe generar asiento contable para recibo tipo INGRESO DIRECTO")
    void testBuildAccountingEntryFromReceiptTypeIncome() {
        // Arrange
        receipt.setReceiptTypeId(2L); // INGRESO DIRECTO
        receipt.setPaymentMethodAccount(100L); // Banco
        receipt.setLedgerAccountId(200L); // Ingresos
        receipt.setTotalAmount(BigDecimal.valueOf(1000));

        // Act
        AccountingEntry entry = receipt.buildAccountingEntryFromReceipt(
                code -> accountFinder.get(code),
                "AE-2024-001"
        );

        // Assert
        assertNotNull(entry);
        assertEquals("AE-2024-001", entry.getCode());
        assertEquals(2, entry.getMovements().size());
        // Validar partida doble
        BigDecimal totalDebits = entry.getMovements().stream()
                .map(m -> m.getDebit())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredits = entry.getMovements().stream()
                .map(m -> m.getCredit())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalDebits.compareTo(totalCredits));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando recibo tipo ABONO no tiene detalles")
    void testBuildAccountingEntryFromReceipt_AbonoWithoutDetails_ThrowsException() {
        // Arrange
        receipt.setReceiptTypeId(1L); // ABONO A FACTURA
        receipt.setDetails(new ArrayList<>()); // Sin detalles

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                receipt.buildAccountingEntryFromReceipt(
                        code -> accountFinder.get(code),
                        "AE-2024-001"
                )
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción con tipo de recibo desconocido")
    void testBuildAccountingEntryFromReceipt_UnknownType_ThrowsException() {
        // Arrange
        receipt.setReceiptTypeId(999L); // Tipo desconocido

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                receipt.buildAccountingEntryFromReceipt(
                        code -> accountFinder.get(code),
                        "AE-2024-001"
                )
        );
    }

    @Test
    @DisplayName("Debe mantener detalles del recibo")
    void testReceiptDetails() {
        // Arrange & Act
        // Setup ya prepara receipt con detalles

        // Assert
        assertNotNull(receipt.getDetails());
        assertEquals(1, receipt.getDetails().size());
        assertEquals("INV-001", receipt.getDetails().get(0).getInvoiceCode());
    }

    @Test
    @DisplayName("Debe validar que el recibo tenga monto total positivo")
    void testReceiptPositiveTotalAmount() {
        // Arrange & Act
        // Setup ya prepara receipt

        // Assert
        assertTrue(receipt.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Debe recuperar información de fecha del recibo")
    void testReceiptIssueDate() {
        // Arrange
        LocalDate expectedDate = LocalDate.of(2024, 1, 15);

        // Act
        // Setup ya prepara receipt

        // Assert
        assertEquals(expectedDate, receipt.getIssueDate());
    }

    @Test
    @DisplayName("Debe mantener información del método de pago")
    void testReceiptPaymentMethod() {
        // Arrange & Act
        // Setup ya prepara receipt

        // Assert
        assertEquals(1L, receipt.getPaymentMethodId());
        assertEquals(100L, receipt.getPaymentMethodAccount());
    }

    @Test
    @DisplayName("Debe mantener información del tercero")
    void testReceiptThirdPartyInfo() {
        // Arrange & Act
        // Setup ya prepara receipt

        // Assert
        assertEquals(50L, receipt.getThirdPartyId());
    }

    @Test
    @DisplayName("Debe mantener observaciones del recibo")
    void testReceiptObservations() {
        // Arrange
        String expectedObservations = "Recibo de prueba";

        // Act
        // Setup ya prepara receipt

        // Assert
        assertEquals(expectedObservations, receipt.getObservations());
        assertTrue(receipt.getObservations().contains("prueba"));
    }

    @Test
    @DisplayName("Debe validar que débitos y créditos sean iguales en asiento generado")
    void testBuildAccountingEntryDoubleEntry() {
        // Arrange
        receipt.setReceiptTypeId(1L); // ABONO A FACTURA
        receipt.setPaymentMethodAccount(100L); // Banco
        receipt.setLedgerAccountId(200L); // Cuentas por cobrar
        receipt.setTotalAmount(BigDecimal.valueOf(500));
        // Configurar details que sumen el totalAmount
        detail1.setAmountPaid(BigDecimal.valueOf(500));
        receipt.setDetails(Arrays.asList(detail1));

        // Act
        AccountingEntry entry = receipt.buildAccountingEntryFromReceipt(
                code -> accountFinder.get(code),
                "AE-2024-001"
        );

        BigDecimal totalDebit = entry.getMovements().stream()
                .map(m -> m.getDebit())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = entry.getMovements().stream()
                .map(m -> m.getCredit())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        assertEquals(totalDebit, totalCredit);
    }

    @Test
    @DisplayName("Debe mantener ID original del recibo para trazabilidad")
    void testReceiptOriginalId() {
        // Arrange & Act
        // Setup ya prepara receipt

        // Assert
        assertEquals(100L, receipt.getOriginalReceiptId());
        assertNotNull(receipt.getOriginalReceiptId());
    }

    @Test
    @DisplayName("Debe mantener código único del recibo")
    void testReceiptUniqueCode() {
        // Arrange
        Receipt receipt1 = Receipt.builder().receiptCode("RC-001").build();
        Receipt receipt2 = Receipt.builder().receiptCode("RC-002").build();

        // Act & Assert
        assertNotEquals(receipt1.getReceiptCode(), receipt2.getReceiptCode());
    }

    @Test
    @DisplayName("Debe mantener estado del recibo")
    void testReceiptStatus() {
        // Arrange & Act
        // Setup ya prepara receipt

        // Assert
        assertEquals("ACTIVE", receipt.getStatus());
    }

    @Test
    @DisplayName("Debe mantener estado de procesamiento del recibo")
    void testReceiptProcessingStatus() {
        // Arrange & Act
        // Setup ya prepara receipt

        // Assert
        assertEquals(ProcessingStatus.PENDING, receipt.getProcessingStatus());
    }

    @Test
    @DisplayName("Debe mantener información de centro de costo")
    void testReceiptCenterCostInfo() {
        // Arrange & Act
        // Setup ya prepara receipt

        // Assert
        assertEquals(1L, receipt.getCenterCostId());
    }

    @Test
    @DisplayName("Debe generar asiento con ID de documento origen correcto")
    void testBuildAccountingEntrySourceDocumentId() {
        // Arrange
        receipt.setReceiptTypeId(1L);
        receipt.setPaymentMethodAccount(100L);
        receipt.setLedgerAccountId(200L);
        receipt.setTotalAmount(BigDecimal.valueOf(250));
        // Configurar details que sumen el totalAmount
        detail1.setAmountPaid(BigDecimal.valueOf(250));
        receipt.setDetails(Arrays.asList(detail1));

        // Act
        AccountingEntry entry = receipt.buildAccountingEntryFromReceipt(
                code -> accountFinder.get(code),
                "AE-2024-001"
        );

        // Assert
        assertEquals(receipt.getId(), entry.getSourceDocumentId());
    }

    @Test
    @DisplayName("Debe generar asiento con tipo RECEIPT")
    void testBuildAccountingEntryType() {
        // Arrange
        receipt.setReceiptTypeId(1L);
        receipt.setPaymentMethodAccount(100L);
        receipt.setLedgerAccountId(200L);
        receipt.setTotalAmount(BigDecimal.valueOf(750));
        // Configurar details que sumen el totalAmount
        detail1.setAmountPaid(BigDecimal.valueOf(750));
        receipt.setDetails(Arrays.asList(detail1));

        // Act
        AccountingEntry entry = receipt.buildAccountingEntryFromReceipt(
                code -> accountFinder.get(code),
                "AE-2024-001"
        );

        // Assert
        assertEquals("RECEIPT", entry.getType());
    }

    @Test
    @DisplayName("Debe manejar múltiples detalles en recibo tipo ABONO")
    void testBuildAccountingEntryWithMultipleDetails() {
        // Arrange
        ReceiptDetail detail2 = ReceiptDetail.builder()
                .id(2L)
                .invoiceCode("INV-002")
                .accountingAccount(200L)
                .amountPaid(BigDecimal.valueOf(500))
                .build();

        receipt.setReceiptTypeId(1L);
        // Configurar detail1 para que la suma total coincida con totalAmount (1000)
        detail1.setAmountPaid(BigDecimal.valueOf(500));
        receipt.setDetails(Arrays.asList(detail1, detail2));

        // Act
        AccountingEntry entry = receipt.buildAccountingEntryFromReceipt(
                code -> accountFinder.get(code),
                "AE-2024-001"
        );

        // Assert
        assertNotNull(entry);
        assertEquals(3, entry.getMovements().size()); // 1 débito + 2 créditos
    }

    @Test
    @DisplayName("Debe incluir información de empresa en asiento generado")
    void testBuildAccountingEntryEnterpriseInfo() {
        // Arrange
        receipt.setReceiptTypeId(1L);
        receipt.setPaymentMethodAccount(100L);
        receipt.setLedgerAccountId(200L);
        receipt.setTotalAmount(BigDecimal.valueOf(450));
        // Configurar details que sumen el totalAmount
        detail1.setAmountPaid(BigDecimal.valueOf(450));
        receipt.setDetails(Arrays.asList(detail1));

        // Act
        AccountingEntry entry = receipt.buildAccountingEntryFromReceipt(
                code -> accountFinder.get(code),
                "AE-2024-001"
        );

        // Assert
        assertEquals("ENT-001", entry.getIdEnterprise());
    }

    @Test
    @DisplayName("Debe incluir centro de costo en asiento generado")
    void testBuildAccountingEntryCenterCost() {
        // Arrange
        receipt.setReceiptTypeId(1L);
        receipt.setPaymentMethodAccount(100L);
        receipt.setLedgerAccountId(200L);
        receipt.setTotalAmount(BigDecimal.valueOf(800));
        // Configurar details que sumen el totalAmount
        detail1.setAmountPaid(BigDecimal.valueOf(800));
        receipt.setDetails(Arrays.asList(detail1));

        // Act
        AccountingEntry entry = receipt.buildAccountingEntryFromReceipt(
                code -> accountFinder.get(code),
                "AE-2024-001"
        );

        // Assert
        // Configurar details que sumen el totalAmount
        detail1.setAmountPaid(BigDecimal.valueOf(300));
        receipt.setDetails(Arrays.asList(detail1));
        assertEquals(1L, entry.getCenterCostId());
    }

    @Test
    @DisplayName("Debe usar código de tercero en movimientos")
    void testBuildAccountingEntryWithThirdPartyId() {
        // Arrange
        receipt.setReceiptTypeId(1L);
        receipt.setPaymentMethodAccount(100L);
        receipt.setLedgerAccountId(200L);
        receipt.setTotalAmount(BigDecimal.valueOf(300));
        receipt.setThirdPartyId(50L);

        // Configurar details que sumen el totalAmount
        detail1.setAmountPaid(BigDecimal.valueOf(300));
        receipt.setDetails(Arrays.asList(detail1));
        // Act
        AccountingEntry entry = receipt.buildAccountingEntryFromReceipt(
                code -> accountFinder.get(code),
                "AE-2024-001"
        );

        // Assert
        assertTrue(entry.getMovements().stream()
                .allMatch(m -> m.getThirdPartyId().equals(50L)));
    }

    @Test
    @DisplayName("Debe generar código de descripción consistente")
    void testBuildAccountingEntryDescription() {
        // Arrange
        receipt.setReceiptTypeId(1L);
        receipt.setPaymentMethodAccount(100L);
        receipt.setLedgerAccountId(200L);
        receipt.setTotalAmount(BigDecimal.valueOf(600));
        receipt.setReceiptCode("RC-12345");
        // Configurar details que sumen el totalAmount
        detail1.setAmountPaid(BigDecimal.valueOf(600));
        receipt.setDetails(Arrays.asList(detail1));

        // Act
        AccountingEntry entry = receipt.buildAccountingEntryFromReceipt(
                code -> accountFinder.get(code),
                "AE-2024-001"
        );

        // Assert
        assertNotNull(entry.getDescription());
        assertTrue(entry.getDescription().contains("RC-12345"));
    }
}
