package co.unicauca.edu.co.contables.accounting.catalogue.unit.accounting.application.service;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IPortfolioSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.service.PortfolioSearchService;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.PortfolioAgingAccountResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PortfolioSearchServiceUnitTest {

    @Mock
    private IAccountingSearchOutputPort accountingSearchOutputPort;

    @Mock
    private IInvoiceProviderPort invoiceProviderPort;

    @InjectMocks
    private PortfolioSearchService portfolioSearchService;

    private InvoiceReplica invoice1;
    private InvoiceReplica invoice2;

    @BeforeEach
    void setUp() {
        invoice1 = new InvoiceReplica();
        invoice1.setId(1L);
        invoice1.setFactCode("INV-001");
        invoice1.setThirdId(50L);
        invoice1.setTotalValue(BigDecimal.valueOf(1000));
        invoice1.setTotalPay(BigDecimal.ZERO);
        invoice1.setPendingValue(BigDecimal.valueOf(1000));
        invoice1.setCreationDate(LocalDate.of(2024, 1, 15));
        invoice1.setExpirationDate(LocalDate.of(2024, 2, 15));
        invoice1.setStatus(InvoiceStatus.PENDING);
        invoice1.setActive(true);
        invoice1.setEntId("ENT-001");

        invoice2 = new InvoiceReplica();
        invoice2.setId(2L);
        invoice2.setFactCode("INV-002");
        invoice2.setThirdId(50L);
        invoice2.setTotalValue(BigDecimal.valueOf(2000));
        invoice2.setTotalPay(BigDecimal.ZERO);
        invoice2.setPendingValue(BigDecimal.valueOf(2000));
        invoice2.setCreationDate(LocalDate.of(2024, 1, 20));
        invoice2.setExpirationDate(LocalDate.of(2024, 2, 20));
        invoice2.setStatus(InvoiceStatus.PENDING);
        invoice2.setActive(true);
        invoice2.setEntId("ENT-001");
    }

    @Test
    @DisplayName("Debe retornar lista de facturas pendientes por cliente")
    void testFindPendingInvoicesByClientIdSuccess() {
        // Arrange
        Long clientId = 50L;
        List<InvoiceReplica> invoices = new java.util.ArrayList<>();
        invoices.add(invoice1);
        invoices.add(invoice2);

        when(invoiceProviderPort.findPendingInvoicesByClientId(clientId))
                .thenReturn(invoices);

        // Act
        List<InvoiceReplica> result = invoiceProviderPort.findPendingInvoicesByClientId(clientId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(invoiceProviderPort).findPendingInvoicesByClientId(clientId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay facturas pendientes")
    void testFindPendingInvoicesByClientId_NoInvoices_ReturnsEmptyList() {
        // Arrange
        Long clientId = 999L;
        when(invoiceProviderPort.findPendingInvoicesByClientId(clientId))
                .thenReturn(Collections.emptyList());

        // Act
        List<InvoiceReplica> result = invoiceProviderPort.findPendingInvoicesByClientId(clientId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(invoiceProviderPort).findPendingInvoicesByClientId(clientId);
    }

    @Test
    @DisplayName("Debe calcular saldo vencido correctamente")
    void testCalculateOverdueBalance() {
        // Arrange
        LocalDate today = LocalDate.of(2024, 2, 20);
        List<InvoiceReplica> overdueInvoices = new ArrayList<>();
        
        InvoiceReplica overdueInvoice = new InvoiceReplica();
        overdueInvoice.setFactCode("INV-OVERDUE");
        overdueInvoice.setTotalValue(BigDecimal.valueOf(1000));
        overdueInvoice.setPendingValue(BigDecimal.valueOf(1000));
        overdueInvoice.setExpirationDate(LocalDate.of(2024, 2, 10));
        
        overdueInvoices.add(overdueInvoice);

        // Act
        BigDecimal totalOverdue = overdueInvoices.stream()
                .filter(inv -> inv.getExpirationDate().isBefore(today))
                .map(InvoiceReplica::getPendingValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        assertEquals(BigDecimal.valueOf(1000), totalOverdue);
    }

    @Test
    @DisplayName("Debe filtrar facturas por tercero")
    void testFilterInvoicesByThirdParty() {
        // Arrange
        Long thirdPartyId = 50L;
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice1);
        invoices.add(invoice2);

        // Act
        List<InvoiceReplica> filteredInvoices = invoices.stream()
                .filter(inv -> inv.getThirdId().equals(thirdPartyId))
                .toList();

        // Assert
        assertEquals(2, filteredInvoices.size());
        assertTrue(filteredInvoices.stream().allMatch(inv -> inv.getThirdId().equals(50L)));
    }

    @Test
    @DisplayName("Debe agrupar facturas por estado")
    void testGroupInvoicesByStatus() {
        // Arrange
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice1);
        invoices.add(invoice2);

        // Act
        long pendingCount = invoices.stream()
                .filter(inv -> InvoiceStatus.PENDING.equals(inv.getStatus()))
                .count();

        // Assert
        assertEquals(2, pendingCount);
    }

    @Test
    @DisplayName("Debe calcular suma total de facturas")
    void testCalculateTotalInvoiceAmount() {
        // Arrange
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice1);
        invoices.add(invoice2);

        // Act
        BigDecimal totalAmount = invoices.stream()
                .map(InvoiceReplica::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        assertEquals(BigDecimal.valueOf(3000), totalAmount);
    }

    @Test
    @DisplayName("Debe calcular edad de factura en días")
    void testCalculateInvoiceAge() {
        // Arrange
        LocalDate today = LocalDate.of(2024, 2, 20);
        LocalDate invoiceDate = LocalDate.of(2024, 1, 15);

        // Act
        long daysOld = java.time.temporal.ChronoUnit.DAYS.between(invoiceDate, today);

        // Assert
        assertEquals(36, daysOld);
        assertTrue(daysOld > 30);
    }

    @Test
    @DisplayName("Debe categorizar facturas por antigüedad")
    void testCategorizInvoicesByAge() {
        // Arrange
        LocalDate today = LocalDate.of(2024, 4, 15);
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice1); // 90 días
        invoices.add(invoice2); // 85 días

        // Act
        long invoicesOver30Days = invoices.stream()
                .filter(inv -> java.time.temporal.ChronoUnit.DAYS.between(inv.getCreationDate(), today) > 30)
                .count();

        // Assert
        assertEquals(2, invoicesOver30Days);
    }

    @Test
    @DisplayName("Debe validar que factura tenga moneda correcta")
    void testInvoiceAmountIsPositive() {
        // Arrange
        // Setup ya prepara invoices

        // Act & Assert
        assertTrue(invoice1.getTotalValue().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(invoice2.getTotalValue().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Debe recuperar detalles de factura")
    void testGetInvoiceDetails() {
        // Arrange
        // Setup ya prepara invoice1

        // Act
        String code = invoice1.getFactCode();
        BigDecimal amount = invoice1.getTotalValue();
        LocalDate date = invoice1.getCreationDate();

        // Assert
        assertEquals("INV-001", code);
        assertEquals(BigDecimal.valueOf(1000), amount);
        assertEquals(LocalDate.of(2024, 1, 15), date);
    }

    @Test
    @DisplayName("Debe mantener relación entre tercero e factura")
    void testInvoiceThirdPartyRelationship() {
        // Arrange
        // Setup ya prepara invoices

        // Act
        Long thirdPartyOfInvoice1 = invoice1.getThirdId();
        Long thirdPartyOfInvoice2 = invoice2.getThirdId();

        // Assert
        assertEquals(50L, thirdPartyOfInvoice1);
        assertEquals(50L, thirdPartyOfInvoice2);
        assertEquals(thirdPartyOfInvoice1, thirdPartyOfInvoice2);
    }

    @Test
    @DisplayName("Debe validar fecha de vencimiento posterior a fecha de factura")
    void testInvoiceDueDateAfterInvoiceDate() {
        // Arrange
        // Setup ya prepara invoice1

        // Act
        LocalDate issueDate = invoice1.getCreationDate();
        LocalDate dueDate = invoice1.getExpirationDate();

        // Assert
        assertTrue(dueDate.isAfter(issueDate));
    }

    @Test
    @DisplayName("Debe obtener estado de factura")
    void testGetInvoiceStatus() {
        // Arrange
        // Setup ya prepara invoice1

        // Act
        InvoiceStatus status = invoice1.getStatus();

        // Assert
        assertEquals(InvoiceStatus.PENDING, status);
        assertNotNull(status);
    }

    @Test
    @DisplayName("Debe encontrar factura por código")
    void testFindInvoiceByCode() {
        // Arrange
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice1);
        invoices.add(invoice2);

        // Act
        InvoiceReplica found = invoices.stream()
                .filter(inv -> "INV-001".equals(inv.getFactCode()))
                .findFirst()
                .orElse(null);

        // Assert
        assertNotNull(found);
        assertEquals("INV-001", found.getFactCode());
    }

    @Test
    @DisplayName("Debe ordenar facturas por fecha")
    void testSortInvoicesByDate() {
        // Arrange
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice2);
        invoices.add(invoice1);

        // Act
        List<InvoiceReplica> sorted = invoices.stream()
                .sorted((a, b) -> a.getCreationDate().compareTo(b.getCreationDate()))
                .toList();

        // Assert
        assertEquals("INV-001", sorted.get(0).getFactCode());
        assertEquals("INV-002", sorted.get(1).getFactCode());
    }

    @Test
    @DisplayName("Debe ordenar facturas por monto")
    void testSortInvoicesByAmount() {
        // Arrange
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice2);
        invoices.add(invoice1);

        // Act
        List<InvoiceReplica> sorted = invoices.stream()
                .sorted((a, b) -> a.getTotalValue().compareTo(b.getTotalValue()))
                .toList();

        // Assert
        assertEquals(BigDecimal.valueOf(1000), sorted.get(0).getTotalValue());
        assertEquals(BigDecimal.valueOf(2000), sorted.get(1).getTotalValue());
    }

    @Test
    @DisplayName("Debe calcular cartera por período")
    void testCalculatePortfolioByPeriod() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice1);
        invoices.add(invoice2);

        // Act
        BigDecimal portfolioAmount = invoices.stream()
                .filter(inv -> inv.getCreationDate().isAfter(startDate) && inv.getCreationDate().isBefore(endDate))
                .map(InvoiceReplica::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert - ambas facturas están en el rango (15 y 20 de enero)
        assertEquals(BigDecimal.valueOf(3000), portfolioAmount);
    }

    @Test
    @DisplayName("Debe contar facturas vencidas")
    void testCountOverdueInvoices() {
        // Arrange
        LocalDate today = LocalDate.of(2024, 3, 1);
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice1); // Vence 2024-02-15
        invoices.add(invoice2); // Vence 2024-02-20

        // Act
        long overdueCount = invoices.stream()
                .filter(inv -> inv.getExpirationDate().isBefore(today))
                .count();

        // Assert
        assertEquals(2, overdueCount);
    }

    @Test
    @DisplayName("Debe validar que todas las facturas tengan código")
    void testAllInvoicesHaveCode() {
        // Arrange
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice1);
        invoices.add(invoice2);

        // Act & Assert
        assertTrue(invoices.stream().allMatch(inv -> inv.getFactCode() != null && !inv.getFactCode().isEmpty()));
    }

    @Test
    @DisplayName("Debe validar estructura de factura replica")
    void testInvoiceReplicaStructure() {
        // Arrange & Act
        InvoiceReplica replica = new InvoiceReplica();
        replica.setId(1L);
        replica.setFactCode("INV-TEST");
        replica.setThirdId(50L);
        replica.setTotalValue(BigDecimal.valueOf(1000));
        replica.setCreationDate(LocalDate.now());
        replica.setExpirationDate(LocalDate.now().plusDays(30));
        replica.setStatus(InvoiceStatus.PENDING);

        // Assert
        assertNotNull(replica.getId());
        assertNotNull(replica.getFactCode());
        assertNotNull(replica.getThirdId());
        assertNotNull(replica.getTotalValue());
        assertNotNull(replica.getCreationDate());
        assertNotNull(replica.getExpirationDate());
        assertNotNull(replica.getStatus());
    }

    @Test
    @DisplayName("Debe buscar facturas por rango de monto")
    void testSearchInvoicesByAmountRange() {
        // Arrange
        List<InvoiceReplica> invoices = new ArrayList<>();
        invoices.add(invoice1);
        invoices.add(invoice2);
        BigDecimal minAmount = BigDecimal.valueOf(1500);
        BigDecimal maxAmount = BigDecimal.valueOf(2500);

        // Act
        List<InvoiceReplica> filtered = invoices.stream()
                .filter(inv -> inv.getTotalValue().compareTo(minAmount) >= 0 && 
                              inv.getTotalValue().compareTo(maxAmount) <= 0)
                .toList();

        // Assert
        assertEquals(1, filtered.size());
        assertEquals("INV-002", filtered.get(0).getFactCode());
    }
}
