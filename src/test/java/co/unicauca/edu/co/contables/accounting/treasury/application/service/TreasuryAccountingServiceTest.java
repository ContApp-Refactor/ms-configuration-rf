package co.unicauca.edu.co.contables.accounting.treasury.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountBalanceUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingEntryPersistenceOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.ITreasuryAccountingInputPort.AccountingEventContext;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentMethodProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAccountingResultPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAccountingResultPublisher.AccountingResultReply;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOffDetail;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentMethodData;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucherDetail;

@ExtendWith(MockitoExtension.class)
class TreasuryAccountingServiceTest {

    private static final String ENTERPRISE = "55433c17-b40f-45a7-ba17-1a8dc71086e8";
    private static final AccountingEventContext CONTEXT = new AccountingEventContext(
            "evt-1", "PAYMENT_VOUCHER_CREATED", "tenant", ENTERPRISE, "corr-1");

    @Mock
    private IAccountingSearchOutputPort accountingSearchOutputPort;
    @Mock
    private IAccountingEntryPersistenceOutputPort accountingEntryPersistencePort;
    @Mock
    private IAccountBalanceUpdateInputPort accountBalanceUpdatePort;
    @Mock
    private IAccountCatalogueSearchOutputPort accountCatalogueSearchPort;
    @Mock
    private IPaymentMethodProviderPort paymentMethodProviderPort;
    @Mock
    private ITreasuryAccountingResultPublisher resultPublisher;

    private TreasuryAccountingService service;

    @BeforeEach
    void setUp() {
        service = new TreasuryAccountingService(accountingSearchOutputPort,
                accountingEntryPersistencePort, accountBalanceUpdatePort,
                accountCatalogueSearchPort, paymentMethodProviderPort, resultPublisher);
    }

    @Test
    @DisplayName("Voucher accepted: debit payment method, credit each payable, reply accepted")
    void voucherAccepted() {
        when(accountingSearchOutputPort.existsBySourceDocumentIdAndType(55L, "PAYMENT_VOUCHER"))
                .thenReturn(false);
        when(paymentMethodProviderPort.findActive(1L, ENTERPRISE))
                .thenReturn(Optional.of(new PaymentMethodData(1L, false, 100L)));
        stubAccount(100L, 900L);
        stubAccount(200L, 901L);
        stubAccount(300L, 902L);
        // Fiel al adaptador real: save() devuelve OTRA instancia ya con el id asignado.
        when(accountingEntryPersistencePort.save(any())).thenAnswer(inv -> persisted(4242L, inv.getArgument(0)));

        PaymentVoucher voucher = voucher();
        voucher.getDetails().add(detail(11L, 300L, new BigDecimal("50")));
        voucher.setTotal(new BigDecimal("150"));

        service.postPaymentVoucher(voucher, CONTEXT);

        ArgumentCaptor<AccountingEntry> entryCaptor = ArgumentCaptor.forClass(AccountingEntry.class);
        verify(accountingEntryPersistencePort).save(entryCaptor.capture());
        verify(accountBalanceUpdatePort).updateBalancesFromAccountingEntry(any());

        AccountingEntry entry = entryCaptor.getValue();
        assertEquals("PAYMENT_VOUCHER", entry.getType());
        assertEquals(55L, entry.getSourceDocumentId());
        assertEquals(AccountingEntryStatus.ACTIVE, entry.getStatus());
        assertEquals(3, entry.getMovements().size());

        // El pasivo se DEBE al extinguirse: una linea por cada factura.
        AccountingMovement payable = entry.getMovements().get(0);
        assertEquals(901L, payable.getAccount());
        assertEquals(0, new BigDecimal("100").compareTo(payable.getDebit()));
        assertEquals(0, payable.getCredit().compareTo(BigDecimal.ZERO));

        // El dinero que sale de la cuenta del metodo de pago se ACREDITA por el total.
        AccountingMovement money = entry.getMovements().get(2);
        assertEquals(900L, money.getAccount());
        assertEquals(0, money.getDebit().compareTo(BigDecimal.ZERO));
        assertEquals(0, new BigDecimal("150").compareTo(money.getCredit()));

        // accounting_movements.third_party_id es NOT NULL: TODAS las patadas llevan
        // el tercero del documento, tambien la cuenta del metodo de pago.
        for (AccountingMovement movement : entry.getMovements()) {
            assertNotNull(movement.getThirdPartyId(), "third_party_id no puede ser null");
        }
        assertEquals(7L, money.getThirdPartyId());

        AccountingResultReply reply = captureReply();
        assertTrue(reply.accepted());
        assertEquals("CREATE", reply.operation());
        assertEquals("PAYMENT_VOUCHER", reply.documentType());
        assertEquals(55L, reply.documentId());
        // El id persistido debe viajar en la respuesta: si se descarta el retorno de
        // save(), el comprobante queda POSTED con accounting_entry_id null.
        assertEquals(4242L, reply.accountingEntryId());
        assertNull(reply.reason());
    }

    @Test
    @DisplayName("Unknown payment method replies rejected and never saves an entry")
    void voucherRejectedUnknownPaymentMethod() {
        when(accountingSearchOutputPort.existsBySourceDocumentIdAndType(55L, "PAYMENT_VOUCHER"))
                .thenReturn(false);
        when(paymentMethodProviderPort.findActive(1L, ENTERPRISE))
                .thenReturn(Optional.empty());

        service.postPaymentVoucher(voucher(), CONTEXT);

        verify(accountingEntryPersistencePort, never()).save(any());
        verify(accountBalanceUpdatePort, never()).updateBalancesFromAccountingEntry(any());
        AccountingResultReply reply = captureReply();
        assertFalse(reply.accepted());
        assertNotNull(reply.reason());
    }

    @Test
    @DisplayName("Unbalanced entry replies rejected instead of saving a broken draft")
    void voucherRejectedWhenEntryDoesNotClose() {
        when(accountingSearchOutputPort.existsBySourceDocumentIdAndType(55L, "PAYMENT_VOUCHER"))
                .thenReturn(false);
        when(paymentMethodProviderPort.findActive(1L, ENTERPRISE))
                .thenReturn(Optional.of(new PaymentMethodData(1L, false, 100L)));
        stubAccount(100L, 900L);
        stubAccount(200L, 901L);

        PaymentVoucher voucher = voucher();
        voucher.setTotal(new BigDecimal("999"));

        service.postPaymentVoucher(voucher, CONTEXT);

        verify(accountingEntryPersistencePort, never()).save(any());
        AccountingResultReply reply = captureReply();
        assertFalse(reply.accepted());
        assertTrue(reply.reason().contains("no cierra"));
    }

    @Test
    @DisplayName("Voucher whose details carry no supplier replies rejected instead of breaking NOT NULL")
    void voucherRejectedWithoutSupplier() {
        when(accountingSearchOutputPort.existsBySourceDocumentIdAndType(55L, "PAYMENT_VOUCHER"))
                .thenReturn(false);
        when(paymentMethodProviderPort.findActive(1L, ENTERPRISE))
                .thenReturn(Optional.of(new PaymentMethodData(1L, false, 100L)));
        stubAccount(100L, 900L);

        PaymentVoucher voucher = voucher();
        voucher.getDetails().get(0).setSupplierId(null);

        service.postPaymentVoucher(voucher, CONTEXT);

        verify(accountingEntryPersistencePort, never()).save(any());
        verify(accountBalanceUpdatePort, never()).updateBalancesFromAccountingEntry(any());
        AccountingResultReply reply = captureReply();
        assertFalse(reply.accepted());
        assertNotNull(reply.reason());
    }

    @Test
    @DisplayName("Duplicate event re-sends the accepted result without a second entry")
    void voucherIdempotentOnDuplicateEvent() {
        AccountingEntry existing = entry(77L);
        when(accountingSearchOutputPort.existsBySourceDocumentIdAndType(55L, "PAYMENT_VOUCHER"))
                .thenReturn(true);
        when(accountingSearchOutputPort.findBySourceDocumentIdAndType(55L, "PAYMENT_VOUCHER"))
                .thenReturn(Optional.of(existing));

        service.postPaymentVoucher(voucher(), CONTEXT);

        verify(accountingEntryPersistencePort, never()).save(any());
        AccountingResultReply reply = captureReply();
        assertTrue(reply.accepted());
        assertEquals(77L, reply.accountingEntryId());
    }

    @Test
    @DisplayName("Void reverses balances, voids the entry and replies accepted")
    void voidVoucher() {
        AccountingEntry existing = entry(77L);
        when(accountingSearchOutputPort.findBySourceDocumentIdAndType(55L, "PAYMENT_VOUCHER"))
                .thenReturn(Optional.of(existing));
        when(accountingEntryPersistencePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.voidPaymentVoucher(55L, CONTEXT);

        verify(accountBalanceUpdatePort).reverseBalancesFromAccountingEntry(existing);
        assertEquals(AccountingEntryStatus.VOIDED, existing.getStatus());
        AccountingResultReply reply = captureReply();
        assertTrue(reply.accepted());
        assertEquals("VOID", reply.operation());
        assertEquals(77L, reply.accountingEntryId());
    }

    @Test
    @DisplayName("Void without an accounting entry replies rejected")
    void voidVoucherWithoutEntry() {
        when(accountingSearchOutputPort.findBySourceDocumentIdAndType(55L, "PAYMENT_VOUCHER"))
                .thenReturn(Optional.empty());

        service.voidPaymentVoucher(55L, CONTEXT);

        verify(accountBalanceUpdatePort, never()).reverseBalancesFromAccountingEntry(any());
        AccountingResultReply reply = captureReply();
        assertFalse(reply.accepted());
        assertEquals("VOID", reply.operation());
    }

    @Test
    @DisplayName("Write-off accepted: debit each payable, credit the counterpart account")
    void writeOffAccepted() {
        when(accountingSearchOutputPort.existsBySourceDocumentIdAndType(9L, "PAYABLE_WRITEOFF"))
                .thenReturn(false);
        stubAccount(200L, 901L);
        stubAccount(400L, 903L);
        when(accountingEntryPersistencePort.save(any())).thenAnswer(inv -> persisted(4343L, inv.getArgument(0)));

        service.postPayableWriteOff(writeOff(), CONTEXT);

        ArgumentCaptor<AccountingEntry> entryCaptor = ArgumentCaptor.forClass(AccountingEntry.class);
        verify(accountingEntryPersistencePort).save(entryCaptor.capture());
        AccountingEntry entry = entryCaptor.getValue();
        assertEquals("PAYABLE_WRITEOFF", entry.getType());
        assertEquals(2, entry.getMovements().size());
        assertEquals(901L, entry.getMovements().get(0).getAccount());
        assertEquals(903L, entry.getMovements().get(1).getAccount());
        assertEquals(0, entry.getMovements().get(1).getCredit().compareTo(new BigDecimal("100")));

        // Mismo criterio que en el comprobante: ninguna patada puede quedar sin tercero.
        for (AccountingMovement movement : entry.getMovements()) {
            assertNotNull(movement.getThirdPartyId(), "third_party_id no puede ser null");
        }
        assertEquals(7L, entry.getMovements().get(1).getThirdPartyId());

        AccountingResultReply reply = captureReply();
        assertTrue(reply.accepted());
        assertEquals("PAYABLE_WRITEOFF", reply.documentType());
        assertEquals(4343L, reply.accountingEntryId());
    }

    @Test
    @DisplayName("Write-off without a counterpart account replies rejected")
    void writeOffRejectedWithoutCounterpart() {
        when(accountingSearchOutputPort.existsBySourceDocumentIdAndType(9L, "PAYABLE_WRITEOFF"))
                .thenReturn(false);
        stubAccount(400L, null);

        service.postPayableWriteOff(writeOff(), CONTEXT);

        verify(accountingEntryPersistencePort, never()).save(any());
        AccountingResultReply reply = captureReply();
        assertFalse(reply.accepted());
    }

    private AccountingResultReply captureReply() {
        ArgumentCaptor<AccountingResultReply> captor =
                ArgumentCaptor.forClass(AccountingResultReply.class);
        verify(resultPublisher).publish(captor.capture());
        return captor.getValue();
    }

    private void stubAccount(Long requestedId, Long resolvedId) {
        if (resolvedId == null) {
            when(accountCatalogueSearchPort.getAccountCatalogueById(requestedId, ENTERPRISE))
                    .thenThrow(new IllegalStateException("cuenta inexistente"));
            return;
        }
        AccountCatalogue account = new AccountCatalogue();
        account.setId(resolvedId);
        when(accountCatalogueSearchPort.getAccountCatalogueById(requestedId, ENTERPRISE))
                .thenReturn(account);
    }

    /**
     * Imita a {@code AccountingEntryPersistenceJpaAdapter.save}: mapea a entidad, persiste
     * y devuelve OTRA instancia de dominio con el id ya asignado. El objeto que entra
     * conserva id {@code null}, igual que en produccion.
     */
    private AccountingEntry persisted(Long id, AccountingEntry source) {
        AccountingEntry saved = AccountingEntry.builder()
                .code(source.getCode())
                .date(source.getDate())
                .description(source.getDescription())
                .status(source.getStatus())
                .sourceDocumentId(source.getSourceDocumentId())
                .type(source.getType())
                .idEnterprise(source.getIdEnterprise())
                .movements(source.getMovements())
                .build();
        saved.setId(id);
        return saved;
    }

    private AccountingEntry entry(Long id) {
        AccountingEntry entry = AccountingEntry.builder()
                .code("AC-TEST")
                .date(LocalDate.of(2026, 1, 1))
                .description("test")
                .status(AccountingEntryStatus.ACTIVE)
                .sourceDocumentId(55L)
                .type("PAYMENT_VOUCHER")
                .idEnterprise(ENTERPRISE)
                .movements(List.of())
                .build();
        entry.setId(id);
        return entry;
    }

    private PaymentVoucherDetail detail(Long invoiceId, Long payableAccountId, BigDecimal amount) {
        PaymentVoucherDetail detail = new PaymentVoucherDetail();
        detail.setInvoiceId(invoiceId);
        detail.setSupplierId(7L);
        detail.setInvoiceReference("FAC-" + invoiceId);
        detail.setPayableAccountId(payableAccountId);
        detail.setAmountPaid(amount);
        return detail;
    }

    private PaymentVoucher voucher() {
        PaymentVoucher voucher = new PaymentVoucher();
        voucher.setId(55L);
        voucher.setVoucherNumber("PV-1");
        voucher.setEnterpriseId(ENTERPRISE);
        voucher.setPaymentMethodId(1L);
        voucher.setIssueDate(LocalDate.of(2026, 1, 15));
        voucher.setTotal(new BigDecimal("100"));
        voucher.setDetails(new java.util.ArrayList<>(List.of(detail(10L, 200L, new BigDecimal("100")))));
        return voucher;
    }

    private PayableWriteOff writeOff() {
        PayableWriteOffDetail detail = new PayableWriteOffDetail();
        detail.setInvoiceId(10L);
        detail.setSupplierId(7L);
        detail.setPayableAccountId(200L);
        detail.setAmount(new BigDecimal("100"));

        PayableWriteOff writeOff = new PayableWriteOff();
        writeOff.setId(9L);
        writeOff.setEnterpriseId(ENTERPRISE);
        writeOff.setCounterpartAccountId(400L);
        writeOff.setTotal(new BigDecimal("100"));
        writeOff.setDetails(List.of(detail));
        return writeOff;
    }
}
